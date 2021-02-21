package com.example.managementappproject.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.TaskListItemsAdapter
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.Card
import com.example.managementappproject.models.Task
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import com.google.firebase.firestore.remote.FirestoreChannel
import kotlinx.android.synthetic.main.activity_task_list.*
import javax.security.auth.login.LoginException

class TaskListActivity : BaseActivity() {

    // we need to get the boardDetails inside this activity
    private lateinit var mBoardDetails: Board
    // we need to use the mBoardDocumentId out of the onCreate, we make it global and use it when we call the onActivityResultFunction
    private lateinit var mBoardDocumentId: String
    // we need it to implement the memberList to select them on a specific card of taskList -  we change it to public var to be accessed from multiple spots, not only here
    lateinit var mAssignedMemberDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        // now we can catch the data from the intent to pass to this activity
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // if the conditions are true, RELOAD the whole details
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){ // we add a "or" condition to refresh the detail of the board(either from newMember activity or going back from CardDetailsActivity)
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
        } else {
            Log.e("Cancelled", "Cancelled")
        }

        /** We could create more request in order to always get the latest update from the taskList UI.
         *  This will always give the latest update but it will also create many more request on the database.
         *  The way we use here is to optimize the app , if we want a better user experience we should load the update
         *  each time we came back to the taskList UI.
         *
         *  override onResume() {
         *  showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
            super.onResume()
            } */
    }

    /** method to move into the CardDetailsActivity UI, we need to specify the position of which list we want to
        work with and the position of which card of that list we want to work with because we want to load details
        about this specific card and not about any card. We can enable the click event to move to the other activity
        from the adapter(where we work with the card view behaviours) */
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails) // we send the board
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMemberDetailList) // pass the member list to the CardDetailsActivity
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE) // we want to get these info at the CardDetailsActivity
    }



    // method to inflate the new menu we just created
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                // pass extra details with the intent to the other activity
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails) // this only works because mBoardDetails is of type Board that is Parcelable( we are passing a whole object because it's made for a string and we can get extra info from it)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE) // we need to catch it inside the other activity
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_task_list_activity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title = mBoardDetails.name // we set the title with the name we get from the board
        }

        toolbar_task_list_activity.setNavigationOnClickListener{ onBackPressed() }
    }

    fun boardDetails(board: Board){
        //inside this method we already get a board, we can set is as the actual board we are working with - It is not necessary to set up the actionbar anymore
        mBoardDetails = board
        hideProgressDialog()
        setUpActionBar() // we don't have to specify the title anymore, we get it directly from the board

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this@TaskListActivity, mBoardDetails.assignedTo) // the people who are assigned to are members of the board
        // pass the info from the TaskListActivity over to the CardDetailsListActivity(we need to know the members as well there)
    }

    // method that add or update a taskList
    fun addUpdateTaskListSuccess(){
        // when we call the related method we start a ProgressDialog, when the task is successful we need to hide it and create a new one
        hideProgressDialog()
        // we are hiding the 1st after tha task is loaded successfully but now we have to get more info from FireStore(2 processes, 2 different progressDialog)
        showProgressDialog(resources.getString(R.string.please_wait))
        // get board details
        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    // method that takes care of creating a taskList
    fun createTaskList(taskListName: String){
        Log.i("Task List Name", taskListName)
        // create the task, we need to pass a title and who creates it
        val task = Task(taskListName, FireStoreClass().getCurrentUserId())
        // we need to update the board and let it know that something has been happening on its taskList
        mBoardDetails.taskList.add(0, task) // when we create a taskList we can add that info at the index 0 and pass the task(new taskList element)
        // we need to remove the tv_add_task_list from the UI(we remove  the last entry with taskList.size - 1
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        // user need to wait
        showProgressDialog(resources.getString(R.string.please_wait))
        // now we can pass the board that we just created to the addUpdateTaskList() method
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    // method to update and EDIT the taskList, we need to know the current name and the task we need to update
    fun updateTaskList(position: Int, listName: String, model: Task){
        // we need to create the task and know who created it
        val task = Task(listName, model.createdBy)
        // check the taskList at the position given
        mBoardDetails.taskList[position] = task
        // remove the old one
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    // method to delete an existing task( we set the position as parameter to know at which position we need to delete something)
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails) // update the whole board after we delete a taskList from it
    }

    fun addCardToTaskList(position: Int, cardName: String){
        // remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        // create a new array and we assign the getCurrentUserId that will give us the assigned users(Card class var - all the people that the card is assigned to)
        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FireStoreClass().getCurrentUserId())

        /* create a card based on the cardName we get from the attribute we pass to this method and getTheCurrentUser who created
           it and store to this var to have it always available. we also need the cardAssigned UsersList(with just the creator in it) */
        val card = Card(cardName, FireStoreClass().getCurrentUserId(), cardAssignedUsersList)

        // we create a cardList to get the position of the card inside the taskList
        val cardList = mBoardDetails.taskList[position].cards

        // now we can use this cardList and add the card we just created( if there's nothing, will be the 1st card, this won't be the next card.
        cardList.add(card)

        // now we can create a Task object and we pass arguments to its parameters
        val task = Task(mBoardDetails.taskList[position].title, mBoardDetails.taskList[position].createdBy, cardList)

        // now we can assign the task we just created as the task for the current position (updated task with latest cardsList)
        mBoardDetails.taskList[position] = task

        //show progress and update the taskList, by getting the board(parent of task) we get the task(parent of the card) and the card(all updated together not individually)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    // method to get the board member details
    fun boardMembersDetailsList(list: ArrayList<User>) {
        mAssignedMemberDetailList = list // we assign it to the list that is pass to us
        hideProgressDialog() // we show it before we called the method and we need to hide it

        // here we setup the actionbar but we also need to load all of the tasks
        val addTaskList = Task(resources.getString(R.string.add_list))
        // now we can add this taskLst to our taskList
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager = LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)

        rv_task_list.setHasFixedSize(true)

        // create adapter and assign it to rv_task_list
        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter
    }

    // method to handel the moving of the cards - we pass the position and the card we want to change if this is method is called
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        //we need to remove the 1st card of the list that is the Add Card view( we don't want to move that)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        // set the card with the position inside the task list as the card is passed to the method
        mBoardDetails.taskList[taskListPosition].cards = cards
        // we update the database about the new details of the taskList
         showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    // we use it when we startActivity for Result when the user press to Member and go back in the taskList menu
    companion object{
        const val MEMBERS_REQUEST_CODE : Int = 13
        // new const for getResult to don't update constantly the board
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }
}