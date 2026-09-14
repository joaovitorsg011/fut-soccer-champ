package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Championship(
    @DocumentId val id: String = "",
    val name: String = "",
    val season: String = "",
    val teamLimit: Int = 0,
    val description: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L
)
