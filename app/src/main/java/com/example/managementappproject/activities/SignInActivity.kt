package com.example.managementappproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.managementappproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

@Suppress("DEPRECATION")
class SignInActivity : BaseActivity() {

    // We can either set auth inside the onCreate or at the moment when we actually use the method
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        btn_signIn.setOnClickListener {
            signInRegisteredUser()
        }

        setUpActionBar()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_singIn_activity)

        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
            actionbar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_singIn_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /* I need a method which allow the user to signIn. After setting the method we can link
       it to the btn signIn in order to execute it */
    private fun signInRegisteredUser(){
        val email: String = et_signIn_email.text.toString().trim { it <= ' ' }
        val password: String = et_signIn_password.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        // once the task is complete we need to remove the progress dialog from UI
                        hideProgressDialog()
                        if (task.isSuccessful) {
                            Toast.makeText(
                                    this@SignInActivity,
                                    "You have successfully signed in",
                                    Toast.LENGTH_LONG
                            ).show()

                            startActivity(Intent(this@SignInActivity, MainActivity::class.java ))
                        } else {
                            Toast.makeText(this@SignInActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                            ).show()
                        }
                    }
        }
    }

    // I need to validate the form
    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter an email address")
            false
        } else if(TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter a password")
            false
        }else {
                true
            }
        }
    }
