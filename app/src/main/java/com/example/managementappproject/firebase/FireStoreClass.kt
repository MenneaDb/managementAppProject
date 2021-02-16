package com.example.managementappproject.firebase

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import com.example.managementappproject.R
import com.example.managementappproject.activities.*
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.Card
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * I create this class in order to hold the logic related to FireStore in case I need to change it
 * for any reason in the future, everything will be storage here and it wll be more easy to find it.
 * I didn't create a collection inside the database because I will do it by creating the class that
 * will hold the parameter I need to push inside the database.
 *
 * Collection/Documents/Attributes --> We create everything here inside the project and it will be push
 *                                     inside the database.
 */
class FireStoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    /** create a new user to register him/her inside the FireStore database, not only in the authentication module(Firebase)
       I need a method to provide an entry for every user inside the database. I am going to create the collection inside
       FireStore with my logic here, in order to provide a uniqueId for each
       document(user) we have inside the database I need to create another method for that */
    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener{
                    activity.userRegisteredSuccess()
                }.addOnFailureListener{ e->
                    Log.e(activity.javaClass.simpleName,"Error writing document", e)
                }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId) // we need the document where the specific documentId was passed to this method
            .get()
            .addOnSuccessListener{
                document->
                Log.i(activity.javaClass.simpleName, document.toString())

                // we also need the ID of the document( of the board itself)
                val board = document.toObject(Board::class.java)!! // 1st we create a Board Object
                board.documentId = document.id // we get the unique identifier of the board we got
                activity.boardDetails(board) // we only pass the board
            }.addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document() // we don't specify to get auto random id
            .set(board, SetOptions.merge()) // if the data exists we want to merge it
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board created successfully.")

                    Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", exception)
            }
    }

    /** get board list from fireStore database --> MainActivity as the activity that call this function. We can use QUERY
       here because it's implemented by the fireStore database, not by the real time database and this a big difference
       between them */
    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
                .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
                .get()
                .addOnSuccessListener { document ->
                    Log.i(activity.javaClass.simpleName, document.documents.toString()) // display the snapshot of the document we get from the query that is ASSIGNED_TO us.
                    val boardList: ArrayList<Board> = ArrayList()
                    // go through the all document and add every board to the boardList
                    for (i in document.documents) {
                        val board = i.toObject(Board::class.java)!! // every object you have, make it as Board object and stored in this var
                        board.documentId = i.id
                        boardList.add(board)
                    }
                    // we need to populate the activity with the boardList we just created
                    activity.populateBoardsListToUI(boardList)

                }.addOnFailureListener {
                    e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
                }
    }

    // method to update the taskList and other activities as well
    fun addUpdateTaskList(activity: Activity, board: Board){
        // we use an hashMap to accept other values we can add later (key->String, value->Any)
        val taskListHashMap = HashMap<String, Any>()
        // assign values and store to it at the position we pass to it [Constants.TASK_LIST] the board.taskList
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        //create entry in the database
        mFireStore.collection(Constants.BOARDS)
                .document(board.documentId) // we pass the id that already exist and get the value related to it
                .update(taskListHashMap)
                .addOnSuccessListener {
                    Log.i(activity.javaClass.simpleName, "TaskList updated successfully.")
                    if (activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                    else if (activity is CardDetailsActivity)
                        activity.addUpdateTaskListSuccess()
                }.addOnFailureListener {
                    exception->
                if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if (activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating a board.", exception)
                }
    }

    // method that will take care of updating the user's profile data
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
            }

    }

    /** We create this method because we signed In the user but we didn't GET the data about him/her yet. We need to get
        the boards that the user is assigned to, or his/her tasks and so forth. We assign the signInActivity as parameter
        because I need to execute some code from that activity related to this method. It is similar to the registerUser()
        method but instead of set values we need to GET it. Inside the addOnSuccessListener we need to get the document
        related to getCurrentUserId() by using a lambda expression, once we have it we can make an Object from it. We need
        to specify for which class we want to use it -> I want to make an user Object from whatever is given to me from the
        document--> toObject(User::class.java) */
    fun loadUserData(activity: Activity, readBoardsList:  Boolean = false){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .get()
                .addOnSuccessListener{ document ->
                    Log.e(activity.javaClass.simpleName, document.toString())
                    // we want to  store the document we get and make it as Object of the User class.
                    val loggedInUser = document.toObject(User::class.java)!!
                    // now I can signIn the user and use a new method from signInActivity to execute it.
                    /* it will works accordingly with the activity that will call the method, in this way we don't have to
                           reuse the same code and do all these tasks here by reusing the user object we got from the database */
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList) // we only want to read the boards in the Main Activity, only  if is necessary
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                        // I want to know if I need to read the List or I shouldn't, this is why we use the boolean value
                    }
                }.addOnFailureListener{ e->
                // we need to hide the activity
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("signInUser","Error writing document", e)
        }
    }

    /** I created this method in order to return the unique Id (UID) provided by the Authentication side of the database and
       execute it inside the registerUser() method to use it inside the collection as "Document Id"  */

    /** Auto-LogIn for an existing user and send him/her directly to the main activity instead of the logIn screen once again,
    this is based in whatever there's the userId from the currentUser -> this is performed automatically in the back-end
    by Firebase(no token to download and store somewhere) and use it to go directly to the LogIn activity or the IntroActivity
    */

    fun getCurrentUserId(): String{

        // we store the instance of the current user inside this variable to check if the user is null or null
        val currentUser = FirebaseAuth.getInstance().currentUser
        // we create the variable that will return the value for this method(valid value, not null)
        var currentUserID = ""
        if (currentUser != null){
            /* if is not null, we assign the uniqueId to the new var. If is null tha var will be already empty and return
               an empty string the uid pf the user will allow us to GET the other info related to this specific user */
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    // get the membersList from the collection (assigned members)
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        // we want to get the users that have the Ids inside the assignedTo of an existing task
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList() // empty
                // we go through the documents we get from the database and add the users to the usersList
                for (i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                // we pass the list with the assignedTo users to the layout where I want to display the membersList
                if (activity is MembersActivity){
                    activity.setUpMembersList(usersList)
                } else if (activity is TaskListActivity){
                    activity.boardMembersDetailsList(usersList)
                }
            }.addOnFailureListener{  e ->
                    if (activity is MembersActivity){
                        activity.hideProgressDialog()
                    } else if (activity is TaskListActivity){
                        activity.hideProgressDialog()
                    }
                    Log.e(activity.javaClass.simpleName, "Error while creating a member's list", e)
            }
    }

    // method to get the member details
    fun getMemberDetails(activity: MembersActivity, email: String){
        // get data from database
        mFireStore.collection(Constants.USERS)
                .whereEqualTo(Constants.EMAIL, email) // check if the email exist in the user collection
                .get()
                .addOnSuccessListener {
                    // we add the existing documents
                    document ->
                    if (document.documents.size > 0) { // if there's at least 1 document
                        // we create a User object from the document - we only pass the position 0 because the email will be unique, a user can't have 2 profile with the same email
                        val user = document.documents[0].toObject(User::class.java)!!
                        activity.memberDetails(user) // still to implement the method
                    } else { // if there's no document
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar("No such member found")
                    }
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while getting user details", e)

                }
    }
    /** we need to add new members to the memberList of a Task( they are not assigned to the board), we need the member
    and the board of where we want to assign him/her we need a HashMap to update the board with the new member. 1st
    we need the board to assign something to it now that we assign it to the board, we need to update it to the database.
    assignedTo-HasMap is created because there is a var of the board inside the database, arrayList of string of Ids of
    users that are assigned to a task */
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        // update the database
        mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)// we only want to update the board where the user has entered a new member to it
                .update(assignedToHashMap) // it is required an hashMap because this is the type we have related to the var in the database
                .addOnSuccessListener{
                    // if the update went well
                    activity.memberAssignSuccess(user) // user that we get when we call the main method, here we call the method add a member to the list
                }
                .addOnFailureListener{
                    e->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
                }
    }

}