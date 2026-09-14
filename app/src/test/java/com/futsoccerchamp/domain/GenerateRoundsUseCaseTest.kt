package com.futsoccerchamp.domain

import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.usecase.GenerateRoundsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateRoundsUseCaseTest {

    private val useCase = GenerateRoundsUseCase()

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
}
