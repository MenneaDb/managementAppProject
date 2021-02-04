package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {
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

    private fun setUpActionBar(title: String){
        setSupportActionBar(toolbar_task_list_activity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title = title
        }

        toolbar_task_list_activity.setNavigationOnClickListener{ onBackPressed() }
    }

    fun boardDetails(board: Board){
        hideProgressDialog()
        setUpActionBar(board.name) // display the title of each board the user select

    }
}