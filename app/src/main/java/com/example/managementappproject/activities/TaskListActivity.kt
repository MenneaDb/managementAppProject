package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.TaskListItemsAdapter
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.Task
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

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
        FireStoreClass().getBoardDetails(this, boardDocumentId)
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
        board.taskList.add(addTaskList)

        rv_task_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_task_list.setHasFixedSize(true)

        // create adapter and assign it to rv_task_list
        val adapter = TaskListItemsAdapter(this, board.taskList)
        rv_task_list.adapter = adapter

    }

    // method that add or update a taskList
    fun addUpdateTaskListSuccess(){
        // get board details
        FireStoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }
}