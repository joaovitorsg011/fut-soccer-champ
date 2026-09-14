package com.futsoccerchamp.domain.usecase

import com.futsoccerchamp.data.model.Team

/** Um confronto gerado automaticamente, ainda sem data definida. */
data class GeneratedMatch(val homeTeamId: String, val awayTeamId: String)

/** Rodada gerada automaticamente com seus confrontos. */
data class GeneratedRound(val number: Int, val matches: List<GeneratedMatch>)

/**
 * Gera o calendário de turno único usando o método round-robin (algoritmo do círculo).
 * Com número ímpar de times, um deles folga a cada rodada.
 */
class GenerateRoundsUseCase {

    operator fun invoke(teams: List<Team>): List<GeneratedRound> {
        if (teams.size < 2) return emptyList()

        // Com número ímpar adiciona-se um "bye" para emparelhar todos os times.
        val ids = teams.map { it.id }.toMutableList()
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
                    // Alterna o mando de campo para equilibrar jogos em casa e fora.
                    GeneratedMatch(away, home)
                }
            }
            // Rotaciona todos os times, mantendo o primeiro fixo.
            ids.add(1, ids.removeAt(ids.lastIndex))
            GeneratedRound(number = round + 1, matches = pairs)
        }
    }

    private companion object {
        const val BYE = "__bye__"
    }
}
