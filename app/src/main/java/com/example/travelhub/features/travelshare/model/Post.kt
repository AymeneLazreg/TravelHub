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
    val category: String = "", // ex: nature, musée, rue
    val tags: List<String> = emptyList(),
    val timestamp: Timestamp = Timestamp.now(),
    val likesCount: Long = 0,
    val likedBy: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val groupName: String? = null,
    val groupId: String = "",

    // --- INDISPENSABLE POUR L'ITINÉRAIRE ET LA VUE CARTE
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)