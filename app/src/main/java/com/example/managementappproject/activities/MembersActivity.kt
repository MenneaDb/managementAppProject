package com.example.managementappproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.MemberListItemsAdapter
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*

class MembersActivity : BaseActivity() {

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
        // user come to this activity and 1st we show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))
        // we call the method from the FireStoreClass, we can't pass this var without specifying the arrayList assignedTo
        FireStoreClass().getAssignedMembersListDetails(this@MembersActivity, mBoardDetails.assignedTo)


    }

    // this activity need to know that it has to use the MemberListItemsAdapter
    fun setUpMembersList(list: ArrayList<User>){
        hideProgressDialog()
        // assign to the RecyclerView to the right Adapter
        rv_members_list.layoutManager = LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)
        // create an adapter to assign the one we prepared here
        val adapter = MemberListItemsAdapter(this@MembersActivity, list) // list that is loaded to the method(attribute)
        rv_members_list.adapter = adapter
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