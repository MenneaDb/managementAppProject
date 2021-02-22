 package com.example.managementappproject.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

 class MembersActivity : BaseActivity() {

    // to catch and store the extra details from the intent
    private lateinit var mBoardDetails: Board
    // use it to add a list of member on the UI
    private lateinit var mAssignedMembersList: ArrayList<User>
    // to check if the were made any changes or not and I don't want to reload the activity
    private var anyChangeMade: Boolean = false



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

    override fun onBackPressed() {
        if (anyChangeMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    /** new method to add the member we get from the method memberDetails to the mAssignedMembersList
       (we just set it and assigned it to the existing list for now) - this method is called only when
        the assigning of new members is successful */
    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        // we want to make sure that the user is passed to this method is added to the mAssignedMembersList
        mAssignedMembersList.add(user)
        // here we can check if something changed in the membersList because in this is method is where is performed this task
        anyChangeMade = true
        // set up the memberList
        setUpMembersList(mAssignedMembersList) // we need to use this method to update the UI of the MembersList when a new member is added to it

        // when a member is added, we want to send the notification
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    // when a user is added as member of a board, he/she should be notify
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String): AsyncTask<Any, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }
        override fun doInBackground(vararg params: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null
            try{
                val url = URL(Constants.FCM_BASE_URL)
                // open the connection
                connection = url.openConnection() as HttpURLConnection
                // set settings for it
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST" // sending data over to the server in order to let the server to notify the user's application that should trigger something in respond to this
                // set request properties of that connection(always pretty much the same)
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8") // type of data the characters will be
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}" // we need to pass this info to the FCM_AUTHORIZATION KEY( we set the key as the server key)
                )

                connection.useCaches = false // we don't want to use any caches so we set false (default is true)

                // prepare output stream of data
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                // add info to data object
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName") //we use the board name when we want to write a title of the notification
                // we also need the message, we pass the name of the creator of the board(it is the first of the list that's why the position 0)
                dataObject.put(Constants.FCM_KEY_MESSAGE, "You have been assigned to the Board by ${mAssignedMembersList[0].name}")
                // now we can add to the Json request the dataObject we just created
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject) //FCM_KEY_DATA to be the dataObject
                // pass also the token
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                // something can go wrong, we need to get the http result
                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK){ // code with a 2 are rather positive, 3 is more of redirect thing, everything with a 4 didn't work
                // if this is true we want a input stream that reads from this connection
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream)) // pass these info to the reader to execute it
                    // make a string from whatever is given to us
                    val sb = StringBuilder()
                    // get details line by line
                    var line: String?
                    try {
                        while (reader.readLine().also {line=it} != null){// called as there are things to read
                            sb.append(line+"\n")
                    }
                }catch (e: IOException){
                    e.printStackTrace()
                    } finally {
                        try {
                            // close the connection
                            inputStream.close()
                        } catch (e: IOException){
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    // if we didn't get an OK result
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException){
                result = "Connection Timeout"
            } catch (e: Exception){
                result = "Error: " + e.message
            }finally {
                connection?.disconnect() // if there's a connection, disconnect from it
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            hideProgressDialog()
            // to know what kind of json we get there
            Log.e("JSON Response Result", result)
        }

    }
}