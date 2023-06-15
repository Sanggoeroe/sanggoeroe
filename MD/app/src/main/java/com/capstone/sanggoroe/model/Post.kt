package com.capstone.sanggoroe.model

data class Post(
    val uid: String = "",
    val username: String = "",
    val title: String = "",
    val content: String = "",
    val image: String = "",
    val skills: String = "",
    val timestamp: Long = System.currentTimeMillis()
)