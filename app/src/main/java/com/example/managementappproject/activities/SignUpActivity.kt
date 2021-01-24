package com.example.managementappproject.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.managementappproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

/* I need this class to be able to use all of the functionality of AppCompactActivity
*  but at the same time  of the BaseActivity as well, which contains features that
*  I have created (error displayed, progress dialog) */
@Suppress("DEPRECATION")
class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()
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

        btn_signUp.setOnClickListener {
            registerUser()
        }
    }

    // when the use press the signUp btn from SignUp activity can call this method
    private fun registerUser(){
        val name: String = et_signUp_name.text.toString().trim { it <= ' '}
        val email: String = et_signUp_email.text.toString().trim {it <= ' '}
        val password: String = et_signUp_password.text.toString().trim { it <= ' '}

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        Toast.makeText(
                            this, "$name you have " +
                                    "successfully registered the email " +
                                    "address $registeredEmail",
                            Toast.LENGTH_LONG
                        ).show()
                        FirebaseAuth.getInstance().signOut()
                        finish()
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

    /* method to validate a form, I want to check if the user enter values or not.
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