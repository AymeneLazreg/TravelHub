package com.example.travelhub.features.travelshare.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val type: String = "", // "LIKE" ou "COMMENT"
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromUserProfileUrl: String = "",
    val toUserId: String = "", // Le propriétaire du post
    val postId: String = "",
    val postImageUrl: String = "",
    val commentText: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false
)