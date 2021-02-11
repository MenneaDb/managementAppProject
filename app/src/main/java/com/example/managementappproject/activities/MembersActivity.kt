package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.managementappproject.R
import com.example.managementappproject.models.Board
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*

class MembersActivity : AppCompatActivity() {

    // to catch and store the extra details from the intent
    private lateinit var mBoardDetails: Board



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        // we initialize it only if the intent has some extra info in order to store them.
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        setUpActionBar()
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_members_activity)
        val actionBar = supportActionBar
        if (actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title = resources.getString(R.string.members)
        }

        toolbar_members_activity.setNavigationOnClickListener { onBackPressed() }
    }
}