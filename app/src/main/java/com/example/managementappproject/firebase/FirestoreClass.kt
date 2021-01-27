package com.example.managementappproject.firebase

import com.google.firebase.firestore.FirebaseFirestore

/**
 * I create this class in order to hold the logic related to FireStore in case I need to change it
 * for any reason in the future, everything will be storage here and it wll be more easy to find it.
 * I didn't create a collection inside the database because I will do it by creating the class that
 * will hold the parameter I need to push inside the database.
 */
class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    /* create a new user to register him/her inside the FireStore database, not only in the authentication module(Firebase)
       I need a method to provide an entry for every user inside the database */




}