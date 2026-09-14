package com.futsoccerchamp.domain

import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.usecase.GenerateRoundsUseCase
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawCheckTest {

    private fun teams(count: Int) = (1..count).map { Team(id = "t$it", name = "Time $it") }

    @Test
    fun `28 times geram 27 rodadas de 14 jogos sem repetir confronto`() {
        val rounds = GenerateRoundsUseCase(Random(99))(teams(28))

        assertEquals(27, rounds.size)
        rounds.forEach { assertEquals(14, it.matches.size) }

        val pairs = rounds.flatMap { r -> r.matches.map { setOf(it.homeTeamId, it.awayTeamId) } }
        assertEquals(378, pairs.size)
        assertEquals(378, pairs.toSet().size)
    }

    @Test
    fun `cada time joga exatamente uma vez por rodada`() {
        GenerateRoundsUseCase(Random(5))(teams(20)).forEach { round ->
            val ids = round.matches.flatMap { listOf(it.homeTeamId, it.awayTeamId) }
            assertEquals(20, ids.size)
            assertEquals(20, ids.toSet().size)
        }
    }

    @Test
    fun `cada time enfrenta todos os outros exatamente uma vez`() {
        val rounds = GenerateRoundsUseCase(Random(3))(teams(10))
        val opponents = mutableMapOf<String, MutableList<String>>()

        rounds.flatMap { it.matches }.forEach { match ->
            opponents.getOrPut(match.homeTeamId) { mutableListOf() }.add(match.awayTeamId)
            opponents.getOrPut(match.awayTeamId) { mutableListOf() }.add(match.homeTeamId)
        }

        assertEquals(10, opponents.size)
        opponents.forEach { (team, list) ->
            assertEquals(9, list.size)
            assertEquals(9, list.toSet().size)
            assertTrue(team !in list)
        }
    }

    @Test
    fun `mando de campo fica equilibrado entre os times`() {
        val rounds = GenerateRoundsUseCase(Random(11))(teams(16))
        val home = mutableMapOf<String, Int>()

        rounds.flatMap { it.matches }.forEach { home[it.homeTeamId] = (home[it.homeTeamId] ?: 0) + 1 }

        home.values.forEach { assertTrue(it in 6..9) }
    }

    @Test
    fun `numero impar deixa um time diferente de folga a cada rodada`() {
        val rounds = GenerateRoundsUseCase(Random(21))(teams(7))
        val resting = rounds.map { round ->
            val playing = round.matches.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet()
            (1..7).map { "t$it" }.first { it !in playing }
        }

        assertEquals(7, rounds.size)
        assertEquals(7, resting.toSet().size)
    }
}
