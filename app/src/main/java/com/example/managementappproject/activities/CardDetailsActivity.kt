package com.example.managementappproject.activities

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.managementappproject.R
import com.example.managementappproject.dialogs.LabelColorListDialog
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
    private var mTaskListPosition: Int = -1 // we set both to -1 we know we don't have the real values
    private var mCardPosition: Int = -1 // ( if it's real, it's always positive)
    private var mSelectedColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setUpActionBar()

        // we want to set this editText with the same name as the title
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length) // when the user click on it, we set the focus on ending of the length at the end of text

        // trigger the selection of a color to label color
        tv_select_label_color.setOnClickListener{
            labelColorsListDialog()
        }

        // in order to trigger the update the details of the card
        btn_update_card_details.setOnClickListener {
            if (et_name_card_details.text.toString().isNotEmpty()) {
                updateCardDetails() // if it's not empty, update the card itself with this method
            } else {
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

    // method to implement the delete_menu( if we want to implement a menu with 1 or more items we always need to use this method
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // implement features to happen when user click the delete_menu
        when(item.itemId){
            R.id.action_delete_card -> {
                // 1st we pass to the user the dialog to ask again the user is sure about deleting this card in particular
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // method to get the intent data
    private fun getIntentData(){

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1) // if the value we get is empty, default value -1
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_DETAIL)){ // if the intent has a board we store to the global var we created for it
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))
        builder.setIcon(R.drawable.ic_alert_dialog_32dp)

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

    // method to actually trigger our dialog, we need to provide color, list and title
    private fun labelColorsListDialog(){
        // prepare color, list and title
        val colorsList: ArrayList<String> = colorsList()
        // prepare listDialog
        val listDialog = object : LabelColorListDialog(
                this@CardDetailsActivity,
                colorsList,
                resources.getString(R.string.str_select_label_color)
        ){
            // to do once the user select an item
            override fun onItemSelected(color: String) {
                // get the color and set is as selected
                mSelectedColor = color
                // set the color as new background of the label
                setColor()
            }
        }
        // we need to show the list
        listDialog.show()
    }

    // method to return a colors list
    private fun colorsList(): ArrayList<String> {
        // 1st we create a list of colors (empty)
        val colorsList: ArrayList<String> = ArrayList()
        // add colors to the list(we can make it either shorter or longer)
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    // method to set the colors
    private fun setColor(){
        // we hide the text of the view to only see the color
        tv_select_label_color.text = ""
        // we change the background of it
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
}