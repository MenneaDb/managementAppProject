package com.example.managementappproject.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.CardMemberListItemsAdapter
import com.example.managementappproject.dialogs.LabelColorListDialog
import com.example.managementappproject.dialogs.MemberListDialog
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.*
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    // global val to get the details from the intent of the TaskListActivity when a user select one of the card
    private lateinit var mBoardDetails: Board // we need a var to get the board we send here
    private var mTaskListPosition: Int = -1 // we set both to -1 we know we don't have the real values
    private var mCardPosition: Int = -1 // ( if it's real, it's always positive)
    private var mSelectedColor: String = ""
    // we need to catch and store the membersList from the intent
    private lateinit var mMembersDetailList: ArrayList<User>
    // new var to store the dueDate
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setUpActionBar()

        // we want to set this editText with the same name as the title
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length) // when the user click on it, we set the focus on ending of the length at the end of text

        // keep color selected on the UI
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        // if is not empty we set the color
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        // trigger the selection of a color to label color
        tv_select_label_color.setOnClickListener{
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog() // we call the method we just prepared
        }

        setUpSelectedMembersList() // we call it here because it should be visible at the start

        // display the dueDate if it was already set before(set it in database)
        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        // if this condition is true, we know we had a value in the database ( 0 is set as default value, if it's different means that there was a valid value)
        if (mSelectedDueDateMilliSeconds > 0){
            // display this existing value as text - date format object to start
            val simpleDateFormat = SimpleDateFormat("dd//MM/yyyy", Locale.ENGLISH)
            // use this simple date format in order to format the mSelectedDueDateMilliSeconds to a readable format(that fits the pattern we set) by using a Date() object
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            // use the date we get as readable format to show it in our textView
            tv_select_due_date.text = selectedDate // assign the date to the textView and show it to the user if we have stored it before in the database
        }
        // when the user click this txtView, we want the DatePickerDialog to appear
        tv_select_due_date.setOnClickListener{
            showDataPicker()
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
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){ // catch the membersList
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    // method to do something when the user click on the select members view
    private fun membersListDialog() {
        // we need to know which members are assigned to us the card
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        // we check if there are already members assigned to this class
        if (cardAssignedMembersList.size > 0) {
            for (i in mMembersDetailList.indices) { // go through all the indices we have
                for (j in cardAssignedMembersList) { // go through all the members we have
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices){ // if none is on the list of people assignedTo the card, none is selected
                mMembersDetailList[i].selected = false
            }
        }

        // actually display the dialog we need to create an Object
        val listDialog = object: MemberListDialog(
                this,
                mMembersDetailList,
                resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                // check the action (selected or unselected)
                if (action == Constants.SELECT){
                    // check if the user if the user.id that is passed to the method(param) is inside of this assignedTo
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){
                        // if it doesn't contain the user we want to assign him/het to the assignedTo list
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                } else { // otherwise
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    // check through the member and find the one we remove and set it to false
                    for (i in mMembersDetailList.indices){
                        if (mMembersDetailList[i].id == user.id){
                            // set to false because if we deleted him/her from the list he/she's not selected anymore
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setUpSelectedMembersList() // refresh and update - selected and unselected members
            }
        }
        listDialog.show()
    }

    // method to update cardDetails, only text for now
    private fun updateCardDetails(){
        // 1st we create a Card object and pass these arguments to the parameters required to create this object
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor, // store the color in the database as well
            mSelectedDueDateMilliSeconds // now the card will get this additional info
        )

        // update the taskList
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1) // remove the add Card btn from the list because we don't want this part to be added to the database(it was added before through methods we used).

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
                resources.getString(R.string.str_select_label_color),
                mSelectedColor
        ){
            // to do once the user select an item
            override fun onItemSelected(color: String) {
                // get the color and set is as selected
                mSelectedColor = color
                // set the color as new background of the label
                setColor()
            }
        }
        // we need to display the list
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

    // use the adapter created for the RecyclerView to select member for a Card
    private fun setUpSelectedMembersList(){
        // 1st we need a var to get the card with the assigned users of it
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        // we need to get the selected users
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices){ // check all people are assigned to the board
            for (j in cardAssignedMembersList){ // are they the same we have assigned to the card itself
                if (mMembersDetailList[i].id == j){ // if this is the case get id and image from these users and store them inside the selectedMember card
                    val selectedMember = SelectedMembers( // we create an object of SelectedMembers
                            mMembersDetailList[i].id,
                            mMembersDetailList[i].image
                    )

                    // now we can get these users and add them to the list we created before
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            // we set the recyclerView we prepared with a GridLayout, we specify the context and how many items can be displayed to each other on this view
            rv_selected_members_list.layoutManager = GridLayoutManager(
                    this@CardDetailsActivity, 6
            )
            // create an adapter object that can be assigned to this recyclerView - context and the list we need to show
            val adapter = CardMemberListItemsAdapter(this@CardDetailsActivity, selectedMembersList, true) // we assigned the var assignedMember as condition(true) to let the user see the members assigned to the card
            // we set the adapter we just prepared as the adapter
            rv_selected_members_list.adapter = adapter
            // we add an onClickListener to the adapter
            adapter.setOnClickListener( // we pass an object of the adapter related to this method
                    object: CardMemberListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            membersListDialog() // display the membersListDialog as event to this click
                        }
                    }
            )
        } else {
            // if the list size is = 0
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }

    // method that allow us to show the date picker - when the user click the related textView, this method will be called.
    private fun showDataPicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) // returns the value of the given calendar
        val month = c.get(Calendar.MONTH) // get the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // get the Day
        val dpd = DatePickerDialog(this, { view, year, monthOfYear, dayOfMonth ->

            val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth" // 01, 02 ... 09, 10, 11..

            val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}" // same here 01, 02

            val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year" // get what we have prepared
            // populate this view when we set the date
            tv_select_due_date.text = selectedDate
            // we need to format it
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            // we parse a string into a date
            val theDate = sdf.parse(selectedDate)
            // we store it inside the var we created ( time is returning a Long value as the var)
            mSelectedDueDateMilliSeconds = theDate!!.time
        },
        year,
        month,
        day
        )
        dpd.show() // show the DatePickerDialog that contains all the logic to get the dueDate
    }
}