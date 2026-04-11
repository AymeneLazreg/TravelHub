package com.example.travelhub.features.travelshare.model

import com.google.firebase.Timestamp

data class Comment(
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val locationName: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Timestamp = Timestamp.now(),
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val comments: List<Comment> = emptyList() // Liste d'objets Comment
)