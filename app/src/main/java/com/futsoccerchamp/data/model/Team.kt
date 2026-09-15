package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Team(
    @DocumentId val id: String = "",
    val leagueId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val abbreviation: String = "",
    val logoUrl: String = "",
    val logo: String = "",
    val createdAt: Long = 0L
)
