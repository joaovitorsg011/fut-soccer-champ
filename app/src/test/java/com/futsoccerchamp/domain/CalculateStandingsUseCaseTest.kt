package com.futsoccerchamp.domain

import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.usecase.CalculateStandingsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateStandingsUseCaseTest {

    private val useCase = CalculateStandingsUseCase()

    private val corinthians = Team(id = "cor", name = "Corinthians", abbreviation = "COR")
    private val palmeiras = Team(id = "pal", name = "Palmeiras", abbreviation = "PAL")
    private val santos = Team(id = "san", name = "Santos", abbreviation = "SAN")

    private fun match(home: String, away: String, homeGoals: Int, awayGoals: Int) = Match(
        id = "$home-$away",
        homeTeamId = home,
        awayTeamId = away,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
        finished = true
    )

    @Test
    fun `vitoria vale tres pontos e soma os gols corretamente`() {
        val standings = useCase(
            teams = listOf(corinthians, palmeiras),
            matches = listOf(match("cor", "pal", 3, 1))
        )

        val winner = standings.first { it.teamId == "cor" }
        assertEquals(3, winner.points)
        assertEquals(1, winner.played)
        assertEquals(1, winner.wins)
        assertEquals(3, winner.goalsFor)
        assertEquals(1, winner.goalsAgainst)
        assertEquals(2, winner.goalDifference)

        val loser = standings.first { it.teamId == "pal" }
        assertEquals(0, loser.points)
        assertEquals(1, loser.losses)
        assertEquals(-2, loser.goalDifference)
    }

    @Test
    fun `empate vale um ponto para cada time`() {
        val standings = useCase(
            teams = listOf(corinthians, palmeiras),
            matches = listOf(match("cor", "pal", 1, 1))
        )

        standings.forEach {
            assertEquals(1, it.points)
            assertEquals(1, it.draws)
            assertEquals(0, it.goalDifference)
        }
    }

    @Test
    fun `partidas sem resultado nao entram na conta`() {
        val standings = useCase(
            teams = listOf(corinthians, palmeiras),
            matches = listOf(Match(id = "m1", homeTeamId = "cor", awayTeamId = "pal"))
        )

        standings.forEach {
            assertEquals(0, it.played)
            assertEquals(0, it.points)
        }
    }

    @Test
    fun `desempata por saldo de gols quando pontos e vitorias empatam`() {
        val standings = useCase(
            teams = listOf(corinthians, palmeiras, santos),
            matches = listOf(
                match("cor", "san", 3, 0),
                match("pal", "san", 2, 0)
            )
        )

        assertEquals(listOf("cor", "pal", "san"), standings.map { it.teamId })
    }

    @Test
    fun `desempata por gols marcados quando saldo tambem empata`() {
        val standings = useCase(
            teams = listOf(corinthians, palmeiras, santos),
            matches = listOf(
                match("cor", "san", 3, 1),
                match("pal", "san", 2, 0)
            )
        )

        assertEquals(listOf("cor", "pal", "san"), standings.map { it.teamId })
    }

    @Test
    fun `aproveitamento e calculado sobre os pontos disputados`() {
        val standings = useCase(
            teams = listOf(corinthians, palmeiras),
            matches = listOf(match("cor", "pal", 1, 0))
        )

        assertEquals(100, standings.first { it.teamId == "cor" }.efficiency)
        assertEquals(0, standings.first { it.teamId == "pal" }.efficiency)
    }
}
