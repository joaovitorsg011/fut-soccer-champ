package com.futsoccerchamp.domain.usecase

import com.futsoccerchamp.data.model.Team
import kotlin.random.Random

data class GeneratedMatch(val homeTeamId: String, val awayTeamId: String)

data class GeneratedRound(val number: Int, val matches: List<GeneratedMatch>)

class GenerateRoundsUseCase(private val random: Random = Random.Default) {

    operator fun invoke(teams: List<Team>): List<GeneratedRound> {
        if (teams.size < 2) return emptyList()

        val ids = teams.map { it.id }.shuffled(random).toMutableList()
        if (ids.size % 2 != 0) ids.add(BYE)

        val total = ids.size
        val roundsCount = total - 1
        val half = total / 2

        return (0 until roundsCount).map { round ->
            val pairs = (0 until half).mapNotNull { i ->
                val home = ids[i]
                val away = ids[total - 1 - i]
                if (home == BYE || away == BYE) {
                    null
                } else if (round % 2 == 0) {
                    GeneratedMatch(home, away)
                } else {

                    GeneratedMatch(away, home)
                }
            }

            ids.add(1, ids.removeAt(ids.lastIndex))
            GeneratedRound(number = round + 1, matches = pairs)
        }
    }

    private companion object {
        const val BYE = "__bye__"
    }
}
