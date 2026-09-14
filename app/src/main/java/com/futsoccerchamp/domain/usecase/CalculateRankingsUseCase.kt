package com.futsoccerchamp.domain.usecase

import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.model.PlayerRanking

class CalculateRankingsUseCase {

    fun topScorers(players: List<Player>, teams: List<Team>, matches: List<Match>): List<PlayerRanking> =
        rank(players, teams, matches) { it.goals }

    fun topGoalkeepers(players: List<Player>, teams: List<Team>, matches: List<Match>): List<PlayerRanking> =
        rank(players.filter { it.isGoalkeeper }, teams, matches) { it.saves }

    private fun rank(
        players: List<Player>,
        teams: List<Team>,
        matches: List<Match>,
        selector: (Match) -> Map<String, Int>
    ): List<PlayerRanking> {
        val finished = matches.filter { it.finished }
        val teamNames = teams.associate { it.id to it.name }

        return players.mapNotNull { player ->
            val entries = finished.mapNotNull { match -> selector(match)[player.id]?.takeIf { it > 0 } }
            val total = entries.sum()
            if (total == 0) return@mapNotNull null

            PlayerRanking(
                playerId = player.id,
                playerName = player.name,
                teamName = teamNames[player.teamId].orEmpty(),
                photo = player.photo,
                number = player.number,
                value = total,
                matches = entries.size
            )
        }.sortedWith(
            compareByDescending<PlayerRanking> { it.value }
                .thenBy { it.matches }
                .thenBy { it.playerName }
        )
    }
}
