package com.example.managementappproject.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.managementappproject.R
import com.example.managementappproject.firebase.FireStoreClass
import com.example.managementappproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

/** I need this class to be able to use all of the functionality of AppCompactActivity
    but at the same time  of the BaseActivity as well, which contains features that
    I have created (error displayed, progress dialog) */
@Suppress("DEPRECATION")
class SignUpActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
    }
    /** We are registering the user on 2 levels(Auth & FireStore database). This is why we create a similar
     * methods inside this class (registerUser()). We need to make sure that every time a user is registered we
     * need to call the registerUser() method from the FireStore class */
    fun userRegisteredSuccess(){
        Toast.makeText(this, "you have successfully registered",
        Toast.LENGTH_LONG).show()
        hideProgressDialog() // I removed it from the registerUser() method because we need to declare it only once.

        FirebaseAuth.getInstance().signOut()
        finish()
    }

    /** set action bar with android's default method setSupportActionBar + back arrow to IntroActivity */
    private fun setupActionBar(){
        setSupportActionBar(toolbar_singUp_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        }

        toolbar_singUp_activity.setNavigationOnClickListener { onBackPressed() }

        // Click event for sign-up button
        btn_signUp.setOnClickListener {
            registerUser()
        }

    }

    /** when the use press the signUp btn from SignUp activity can call this method */
    private fun registerUser(){
        val name: String = et_signUp_name.text.toString().trim { it <= ' '}
        val email: String = et_signUp_email.text.toString().trim {it <= ' '}
        val password: String = et_signUp_password.text.toString().trim { it <= ' '}

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        // we don't need to signOut from here anymore but I need to create a new user.
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        // now we can pass the user to the registerUser method of the FireStore class
                        FireStoreClass().registerUser(this@SignUpActivity, user)
                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }

    /** method to validate a form, I want to check if the user enter values or not.
       I a need a method to register the user in order to use this method and check
       if the values are passed from the user. */
    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }else -> {
                true
            }
        }
    }
}