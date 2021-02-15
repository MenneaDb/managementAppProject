package com.example.managementappproject.activities

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.Board
import com.example.managementappproject.models.Card
import com.example.managementappproject.models.Task
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*

class CardDetailsActivity : BaseActivity() {

    // global val to get the details from the intent of the TaskListActivity when a user select one of the card
    private lateinit var mBoardDetails: Board // we need a var to get the board we send here
    private var mTaskListPosition = -1 // we set both to -1 we know we don't have the real values
    private var mCardPosition = -1 // ( if it's real, it's always positive)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setUpActionBar()
        // we want to set this editText with the same name as the title
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length) // when the user click on it, we set the focus on ending of the length at the end of text
        // in order to trigger the update the details of the card
        btn_update_card_details.setOnClickListener {
            if (et_name_card_details.text.toString().isNotEmpty())
                updateCardDetails() // if it's not empty, update the card itself with this method
            else{
                Toast.makeText(this@CardDetailsActivity, "Enter a card name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // method to update taskList
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish() // close this activity
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

    // to inflate the delete_card menu on the UI
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // method to enable 
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
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

    // method to update cardDetails, only text for now
    private fun updateCardDetails(){
        // 1st we create a Card object and pass these arguments to the parameters required to create this object
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        )

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card // assign the details related to the specific card to our object
        // refresh data from database
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    // method to be able to delete a card
    private fun deleteCard(){
        // 1st we get the cardList with all the current cards
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        // remove the cardList selected by the user
        cardsList.removeAt(mCardPosition)
        // update the taskList
        val taskList: ArrayList<Task> = mBoardDetails.taskList // we get the current taskList
        // remove it
        taskList.removeAt(taskList.size - 1) // we eliminate from the UI the textView Add Card because we don't need it after we update the taskList(latest -> size -1 )
        // update taskList by passing the values to the cardList we prepared before
        taskList[mTaskListPosition].cards = cardsList
        // now we can update the database with the latest mBoardDetails
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }
    // the user can't delete the card by just clicking on the related icon, the app will ask confirmation 1st
    private fun alertDialogForDeleteCard(cardName: String){
        val builder = AlertDialog.Builder(this@CardDetailsActivity)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // create alert dialog
        val alertDialog: AlertDialog = builder.create()
        // set other dialog properties
        alertDialog.setCancelable(false) // user can't cancel it, only press yes or no
        alertDialog.show() // show dialog to UI
    }
}