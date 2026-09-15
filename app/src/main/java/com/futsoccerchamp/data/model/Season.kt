package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Season(
    @DocumentId val id: String = "",
    val leagueId: String = "",
    val ownerId: String = "",
    val tournamentId: String = "",
    val label: String = "",
    val teamIds: List<String> = emptyList(),
    val createdAt: Long = 0L
)
