package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class League(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val createdAt: Long = 0L
)
