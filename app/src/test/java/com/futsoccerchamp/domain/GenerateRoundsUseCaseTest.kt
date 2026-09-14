package com.futsoccerchamp.domain

import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.usecase.GenerateRoundsUseCase
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateRoundsUseCaseTest {

    private val useCase = GenerateRoundsUseCase(Random(7))

    private fun teams(count: Int) = (1..count).map { Team(id = "t$it", name = "Time $it") }

    @Test
    fun `numero par de times gera turno completo sem folgas`() {
        val rounds = useCase(teams(4))

        assertEquals(3, rounds.size)
        rounds.forEach { assertEquals(2, it.matches.size) }
    }

    @Test
    fun `numero impar de times deixa um time de folga por rodada`() {
        val rounds = useCase(teams(5))

        assertEquals(5, rounds.size)
        rounds.forEach { assertEquals(2, it.matches.size) }
    }

    @Test
    fun `cada confronto acontece uma unica vez`() {
        val pairs = useCase(teams(6))
            .flatMap { round -> round.matches.map { setOf(it.homeTeamId, it.awayTeamId) } }

        assertEquals(15, pairs.size)
        assertEquals(15, pairs.toSet().size)
    }

    @Test
    fun `menos de dois times nao gera rodadas`() {
        assertTrue(useCase(teams(1)).isEmpty())
    }

    @Test
    fun `sorteios diferentes produzem calendarios diferentes`() {
        val first = GenerateRoundsUseCase(Random(1))(teams(8))
        val second = GenerateRoundsUseCase(Random(2))(teams(8))

        assertNotEquals(first.first().matches, second.first().matches)
    }

    @Test
    fun `todo time joga uma vez por rodada`() {
        useCase(teams(8)).forEach { round ->
            val ids = round.matches.flatMap { listOf(it.homeTeamId, it.awayTeamId) }
            assertEquals(ids.size, ids.toSet().size)
        }
    }
}
