package com.futsoccerchamp.domain.usecase

import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.model.Standing

/**
 * Calcula a classificação a partir das partidas encerradas.
 *
 * A tabela nunca é persistida: ela é sempre derivada dos resultados,
 * o que evita divergência entre o placar e a pontuação (RNF06).
 *
 * Pontuação: vitória 3, empate 1, derrota 0 (RF21).
 * Desempate: pontos, vitórias, saldo de gols, gols marcados (RF23).
 */
class CalculateStandingsUseCase {

    operator fun invoke(teams: List<Team>, matches: List<Match>): List<Standing> {
        val table = teams.associate { team ->
            team.id to Standing(
                teamId = team.id,
                teamName = team.name,
                abbreviation = team.abbreviation,
                logoUrl = team.logoUrl
            )
        }.toMutableMap()

        matches.asSequence()
            .filter { it.finished && it.homeGoals != null && it.awayGoals != null }
            .forEach { match ->
                val home = table[match.homeTeamId]
                val away = table[match.awayTeamId]
                if (home == null || away == null) return@forEach

                val homeGoals = match.homeGoals!!
                val awayGoals = match.awayGoals!!

                table[match.homeTeamId] = home.applyResult(homeGoals, awayGoals)
                table[match.awayTeamId] = away.applyResult(awayGoals, homeGoals)
            }

        return table.values.sortedWith(
            compareByDescending<Standing> { it.points }
                .thenByDescending { it.wins }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { it.teamName }
        )
    }

    private fun Standing.applyResult(scored: Int, conceded: Int): Standing = copy(
        played = played + 1,
        wins = wins + if (scored > conceded) 1 else 0,
        draws = draws + if (scored == conceded) 1 else 0,
        losses = losses + if (scored < conceded) 1 else 0,
        points = points + when {
            scored > conceded -> 3
            scored == conceded -> 1
            else -> 0
        },
        goalsFor = goalsFor + scored,
        goalsAgainst = goalsAgainst + conceded
    )
}
