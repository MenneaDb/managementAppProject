package com.example.managementappproject.models

data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    // image as string because we just need to pass the reference of the image inside the storage
    val image: String = "",
    val mobile: Long = 0,
    // we create a token to know the that is specific for each user
    val fcmToken: String = ""
)
