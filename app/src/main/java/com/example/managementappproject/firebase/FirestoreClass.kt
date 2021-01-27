package com.example.managementappproject.firebase

import android.util.Log
import com.example.managementappproject.activities.SignUpActivity
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * I create this class in order to hold the logic related to FireStore in case I need to change it
 * for any reason in the future, everything will be storage here and it wll be more easy to find it.
 * I didn't create a collection inside the database because I will do it by creating the class that
 * will hold the parameter I need to push inside the database.
 *
 * Collection/Documents/Attributes --> We create everything here inside the project and it will be push
 *                                     inside the database.
 */
class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    /* create a new user to register him/her inside the FireStore database, not only in the authentication module(Firebase)
       I need a method to provide an entry for every user inside the database. I am going to create the collection inside
       FireStore with my logic here, in order to provide a uniqueId for each
       document(user) we have inside the database I need to create another method for that */
    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener{
                    activity.userRegisteredSuccess()
                }.addOnFailureListener{
                    e->
                    Log.e(activity.javaClass.simpleName,"Error")
                }
    }

    /* I created this method in order to return the unique Id (UID) provided by the Authentication side of the database and
       execute it inside the registerUser() method to use it inside the collection as "Document Id"  */
    fun getCurrentUserId(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }




}