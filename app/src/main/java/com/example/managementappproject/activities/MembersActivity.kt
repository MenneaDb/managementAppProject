 package com.example.managementappproject.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.MemberListItemsAdapter
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {

    // to catch and store the extra details from the intent
    private lateinit var mBoardDetails: Board
    // use it to add a list of member on the UI
    private lateinit var mAssignedMembersList: ArrayList<User>



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
        // we store the list we pass as attribute to the var we just created, in this way we can use it thorough the whole class
        mAssignedMembersList = list
        hideProgressDialog()
        // assign to the RecyclerView to the right Adapter
        rv_members_list.layoutManager = LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)
        // create an adapter to assign the one we prepared here
        val adapter = MemberListItemsAdapter(this@MembersActivity, list) // list that is loaded to the method(attribute)
        rv_members_list.adapter = adapter
    }

    // method to get the member details from the database
    fun memberDetails(user: User){
    // we get the user that will be added to the members and add it to mBoardDetails
        mBoardDetails.assignedTo.add(user.id) // we add the user.id to the assignedTo(arrayList: String) -> the board now is assigned to more members, at least one more than it has before
        FireStoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user) // update the database about the new member that is assigned
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
    // whenever we create a menu we need 2 methods. the 2nd is necessary if we want an event to happen
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflate the menu
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // add onClick event
        when(item.itemId){
            R.id.action_add_member -> { // if the user click this View
                dialogSearchMember()
                return true // because all went good we can return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // method to create the Dialog
    private fun dialogSearchMember(){
        // 1st we create a dialog
        val dialog = Dialog(this@MembersActivity)
        // we specify which dialog I want to display
        dialog.setContentView(R.layout.dialog_search_member)
        // we need to add the onClickListener for both buttons of the view
        dialog.tv_add.setOnClickListener{
            // 1st we need to get the email
            val email = dialog.et_email_search_member.text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this@MembersActivity, email)
            }else{
                Toast.makeText(this@MembersActivity, "Please enter members email address", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show() // we need to make sure that the dialog is shown at some point
    }

    /* new method to add the member we get from the method memberDetails to the mAssignedMembersList
       (we just set it and assigned it to the existing list for now) - this method is called only when
        the assigning of new members is successful */
    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        // we want to make sure that the user is passed to this method is added to the mAssignedMembersList
        mAssignedMembersList.add(user)
        // set up the memberList
        setUpMembersList(mAssignedMembersList) // we need to use this method to update the UI of the MembersList when a new member is added to it

    }
}