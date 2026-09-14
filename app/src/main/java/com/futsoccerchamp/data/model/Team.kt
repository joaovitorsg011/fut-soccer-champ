package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Team(
    @DocumentId val id: String = "",
    val championshipId: String = "",
    val name: String = "",
    val abbreviation: String = "",
    val logoUrl: String = "",
    val logo: String = "",
    val createdAt: Long = 0L
)
