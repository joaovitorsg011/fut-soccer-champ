package com.futsoccerchamp.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.domain.model.Standing
import com.futsoccerchamp.presentation.common.EmptyState

private data class StatisticItem(
    val label: String,
    val value: String,
    val detail: String,
    val icon: ImageVector,
    val wide: Boolean = false
)

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
    val pending = matches.size - finished.size

    val biggestWin = finished.maxByOrNull { kotlin.math.abs(it.homeGoals!! - it.awayGoals!!) }
    val biggestWinValue = biggestWin?.let { "${it.homeGoals} x ${it.awayGoals}" } ?: "—"
    val biggestWinDetail = biggestWin?.let {
        "${teamOf(it.homeTeamId)?.name.orEmpty()} contra ${teamOf(it.awayTeamId)?.name.orEmpty()}"
    } ?: "Nenhuma partida encerrada"

    val played = standings.filter { it.played > 0 }
    val bestAttack = played.maxByOrNull { it.goalsFor }
    val bestDefense = played.minByOrNull { it.goalsAgainst }

    val items = listOf(
        StatisticItem(
            label = "Partidas realizadas",
            value = finished.size.toString(),
            detail = if (pending > 0) "$pending ainda por disputar" else "Todas as partidas encerradas",
            icon = Icons.Default.Stadium
        ),
        StatisticItem(
            label = "Gols marcados",
            value = totalGoals.toString(),
            detail = "Em ${finished.size} partidas",
            icon = Icons.Default.SportsSoccer
        ),
        StatisticItem(
            label = "Média de gols",
            value = average,
            detail = "Por partida disputada",
            icon = Icons.Default.TrendingUp
        ),
        StatisticItem(
            label = "Times em disputa",
            value = standings.size.toString(),
            detail = "Participantes do campeonato",
            icon = Icons.Default.Groups
        ),
        StatisticItem(
            label = "Maior goleada",
            value = biggestWinValue,
            detail = biggestWinDetail,
            icon = Icons.Default.LocalFireDepartment,
            wide = true
        ),
        StatisticItem(
            label = "Melhor ataque",
            value = bestAttack?.goalsFor?.toString() ?: "—",
            detail = bestAttack?.teamName ?: "Sem dados",
            icon = Icons.Default.SportsSoccer
        ),
        StatisticItem(
            label = "Melhor defesa",
            value = bestDefense?.goalsAgainst?.toString() ?: "—",
            detail = bestDefense?.teamName ?: "Sem dados",
            icon = Icons.Default.Shield
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, span = { item -> GridItemSpan(if (item.wide) 2 else 1) }) { item ->
            StatisticCard(item)
        }
    }
}

@Composable
private fun StatisticCard(item: StatisticItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Text(
                item.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                item.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
