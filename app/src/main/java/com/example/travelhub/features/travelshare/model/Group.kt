package com.example.travelhub.features.travelshare.model

data class Group(
    val id: String = "",
    val name: String = "",
    val adminId: String = "",
    val members: List<String> = emptyList(),
    val imageUrl: String = ""
)