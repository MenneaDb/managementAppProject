package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.TaskListItemsAdapter
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.Task
import com.example.managementappproject.utils.Constants
import com.google.firebase.firestore.remote.FirestoreChannel
import kotlinx.android.synthetic.main.activity_task_list.*
import javax.security.auth.login.LoginException

class TaskListActivity : BaseActivity() {

    // we need to get the boardDetails inside this activity
    private lateinit var mBoardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        // now we can catch the data from the intent to pass to this activity
        var boardDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity, boardDocumentId)
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


}