package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.managementappproject.R
import com.example.managementappproject.models.Board
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*

class CardDetailsActivity : AppCompatActivity() {

    // global val to get the details from the intent of the TaskListActivity when a user select one of the card
    private lateinit var mBoardDetails: Board // we need a var to get the board we send here
    private var mTaskListPosition = -1 // we set both to -1 we know we don't have the real values
    private var mCardPosition = -1 // ( if it's real, it's always positive)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setUpActionBar()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_card_details_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            // we get the board from the getIntentData, now we can load the title
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name // load the name with the path to get it
        }

        toolbar_card_details_activity.setNavigationOnClickListener{ onBackPressed() }
    }

    // method to get the intent data
    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAIL)){ // if the intent has a board we store to the global var we created for it
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1) // if the value we get is empty, default value -1
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
    }
}