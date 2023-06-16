package com.capstone.sanggoroe.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val uid: String = "",
    val username: String = "",
    val title: String = "",
    val content: String = "",
    val image: String = "",
    val skills: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var jobID: Int? = null
): Parcelable