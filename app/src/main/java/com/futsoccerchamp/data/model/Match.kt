package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Match(
    @DocumentId val id: String = "",
    val seasonId: String = "",
    val ownerId: String = "",
    val roundId: String = "",
    val homeTeamId: String = "",
    val awayTeamId: String = "",
    val homeGoals: Int? = null,
    val awayGoals: Int? = null,
    val date: String = "",
    val time: String = "",
    val place: String = "",
    val finished: Boolean = false,
    val goals: Map<String, Int> = emptyMap(),
    val misses: Map<String, Int> = emptyMap(),
    val saves: Map<String, Int> = emptyMap()
)
