package com.futsoccerchamp.domain.model

data class PlayerRanking(
    val playerId: String,
    val playerName: String,
    val teamName: String,
    val photo: String,
    val number: Int,
    val value: Int,
    val matches: Int
) {
    val average: String
        get() = if (matches == 0) "0,00" else "%.2f".format(value.toDouble() / matches)
}
