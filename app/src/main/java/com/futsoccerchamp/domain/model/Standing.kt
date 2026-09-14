package com.futsoccerchamp.domain.model

data class Standing(
    val teamId: String,
    val teamName: String,
    val abbreviation: String,
    val logoUrl: String = "",
    val points: Int = 0,
    val played: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst

    val efficiency: Int
        get() = if (played == 0) 0 else (points * 100) / (played * 3)
}
