package com.example.managementappproject.firebase

import android.app.Activity
import android.util.Log
import com.example.managementappproject.activities.MainActivity
import com.example.managementappproject.activities.SignInActivity
import com.example.managementappproject.activities.SignUpActivity
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
class FirestoreClass {

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
                }.addOnFailureListener{
                    e->
                    Log.e(activity.javaClass.simpleName,"Error writing document")
                }
    }

    /** We create this method because we signed In the user but we didn't GET the data about him/her yet. We need to get
        the boards that the user is assigned to, or his/her tasks and so forth. We assign the signInActivity as parameter
        because I need to execute some code from that activity related to this method. It is similar to the registerUser()
        method but instead of set values we need to GET it. Inside the addOnSuccessListener we need to get the document
        related to getCurrentUserId() by using a lambda expression, once we have it we can make an Object from it. We need
        to specify for which class we want to use it -> I want to make an user Object from whatever is given to me from the
        document--> toObject(User::class.java) */
    fun signInUser(activity: Activity){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .get()
                .addOnSuccessListener{ document ->
                    // we want to  store the document we get and make it as Object of the User class.
                    val loggedInUser = document.toObject(User::class.java)
                    // now I can signIn the user and use a new method from signInActivity to execute it.
                    if (loggedInUser != null) {
                        // it will works accordingly with the activity that will call this method
                        when (activity) {
                            is SignInActivity -> {
                                activity.signInSuccess(loggedInUser)
                            }
                            is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser)}
                        }
                    }
                }.addOnFailureListener{
                        e->
                // we need to hide the activity
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("signInUser","Error writing document")
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

}