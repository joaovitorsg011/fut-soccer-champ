package com.futsoccerchamp.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.model.Standing
import com.futsoccerchamp.presentation.common.EmptyState

/** Estatísticas gerais derivadas das partidas já encerradas (RF27). */
@Composable
fun StatisticsSection(
    standings: List<Standing>,
    matches: List<Match>,
    teamOf: (String) -> Team?,
    modifier: Modifier = Modifier
) {
    val finished = matches.filter { it.finished && it.homeGoals != null && it.awayGoals != null }

    if (finished.isEmpty()) {
        EmptyState(
            title = "Sem estatísticas ainda",
            subtitle = "Registre ao menos um resultado para ver os números do campeonato.",
            modifier = modifier
        )
        return
    }

    val totalGoals = finished.sumOf { it.homeGoals!! + it.awayGoals!! }
    val average = "%.2f".format(totalGoals.toDouble() / finished.size)

    val biggestWin = finished.maxByOrNull { kotlin.math.abs(it.homeGoals!! - it.awayGoals!!) }
    val biggestWinText = biggestWin?.let {
        val home = teamOf(it.homeTeamId)?.name.orEmpty()
        val away = teamOf(it.awayTeamId)?.name.orEmpty()
        "$home ${it.homeGoals} x ${it.awayGoals} $away"
    } ?: "—"

    val bestAttack = standings.maxByOrNull { it.goalsFor }
    val bestDefense = standings.filter { it.played > 0 }.minByOrNull { it.goalsAgainst }

    val items = listOf(
        "Partidas realizadas" to finished.size.toString(),
        "Gols marcados" to totalGoals.toString(),
        "Média de gols por partida" to average,
        "Maior goleada" to biggestWinText,
        "Melhor ataque" to (bestAttack?.let { "${it.teamName} (${it.goalsFor} gols)" } ?: "—"),
        "Melhor defesa" to (bestDefense?.let { "${it.teamName} (${it.goalsAgainst} gols)" } ?: "—")
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items.size) { index ->
            val (label, value) = items[index]
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(value, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
