package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

enum class PlayerPosition(val label: String) {
    GOALKEEPER("Goleiro"),
    DEFENDER("Defensor"),
    MIDFIELDER("Meio-campista"),
    FORWARD("Atacante")
}

data class Player(
    @DocumentId val id: String = "",
    val championshipId: String = "",
    val teamId: String = "",
    val name: String = "",
    val number: Int = 0,
    val position: String = PlayerPosition.FORWARD.name,
    val photo: String = "",
    val createdAt: Long = 0L
) {
    val positionLabel: String
        get() = runCatching { PlayerPosition.valueOf(position).label }.getOrDefault("")

    val isGoalkeeper: Boolean get() = position == PlayerPosition.GOALKEEPER.name
}
