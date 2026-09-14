package com.futsoccerchamp.domain

import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.PlayerPosition
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.usecase.CalculateRankingsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateRankingsUseCaseTest {

    private val useCase = CalculateRankingsUseCase()

    private val teams = listOf(
        Team(id = "cor", name = "Corinthians"),
        Team(id = "pal", name = "Palmeiras")
    )

    private val players = listOf(
        Player(id = "p1", teamId = "cor", name = "João", position = PlayerPosition.FORWARD.name),
        Player(id = "p2", teamId = "cor", name = "Pedro", position = PlayerPosition.MIDFIELDER.name),
        Player(id = "g1", teamId = "cor", name = "Cássio", position = PlayerPosition.GOALKEEPER.name),
        Player(id = "g2", teamId = "pal", name = "Weverton", position = PlayerPosition.GOALKEEPER.name)
    )

    private fun match(
        id: String,
        goals: Map<String, Int>,
        saves: Map<String, Int>,
        misses: Map<String, Int> = emptyMap()
    ) = Match(
        id = id,
        homeTeamId = "cor",
        awayTeamId = "pal",
        homeGoals = goals.values.sum(),
        awayGoals = 0,
        finished = true,
        goals = goals,
        misses = misses,
        saves = saves
    )

    @Test
    fun `soma os gols de todas as partidas encerradas`() {
        val ranking = useCase.topScorers(
            players,
            teams,
            listOf(
                match("m1", mapOf("p1" to 2, "p2" to 1), emptyMap()),
                match("m2", mapOf("p1" to 1), emptyMap())
            )
        )

        assertEquals(listOf("p1", "p2"), ranking.map { it.playerId })
        assertEquals(3, ranking.first().value)
        assertEquals(2, ranking.first().matches)
    }

    @Test
    fun `partidas nao encerradas ficam fora do ranking`() {
        val pending = match("m1", mapOf("p1" to 3), emptyMap()).copy(finished = false)

        assertTrue(useCase.topScorers(players, teams, listOf(pending)).isEmpty())
    }

    @Test
    fun `ranking de defesas aceita qualquer jogador que tenha ido ao gol`() {
        val ranking = useCase.topGoalkeepers(
            players,
            teams,
            listOf(match("m1", emptyMap(), mapOf("g2" to 7, "g1" to 4, "p1" to 2)))
        )

        assertEquals(listOf("g2", "g1", "p1"), ranking.map { it.playerId })
    }

    @Test
    fun `ranking de erros soma as cobrancas perdidas`() {
        val ranking = useCase.mostMisses(
            players,
            teams,
            listOf(
                match("m1", mapOf("p1" to 1), emptyMap(), misses = mapOf("p2" to 1)),
                match("m2", emptyMap(), emptyMap(), misses = mapOf("p2" to 1, "p1" to 1))
            )
        )

        assertEquals(listOf("p2", "p1"), ranking.map { it.playerId })
        assertEquals(2, ranking.first().value)
    }

    @Test
    fun `menos partidas desempata quem tem o mesmo total`() {
        val ranking = useCase.topScorers(
            players,
            teams,
            listOf(
                match("m1", mapOf("p1" to 1, "p2" to 2), emptyMap()),
                match("m2", mapOf("p1" to 1), emptyMap())
            )
        )

        assertEquals(listOf("p2", "p1"), ranking.map { it.playerId })
    }

    @Test
    fun `nome do time acompanha cada jogador do ranking`() {
        val ranking = useCase.topGoalkeepers(players, teams, listOf(match("m1", emptyMap(), mapOf("g2" to 3))))

        assertEquals("Palmeiras", ranking.first().teamName)
    }
}
