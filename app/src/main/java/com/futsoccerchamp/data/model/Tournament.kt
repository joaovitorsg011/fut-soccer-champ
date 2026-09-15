package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Tournament(
    @DocumentId val id: String = "",
    val leagueId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val createdAt: Long = 0L
)
