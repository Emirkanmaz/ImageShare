package com.emirkanmaz.imageshare.model

data class Post(
    val email: String = "",
    val imageUrl: String = "",
    val comment: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)
