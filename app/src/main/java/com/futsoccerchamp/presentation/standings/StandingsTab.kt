package com.futsoccerchamp.presentation.standings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.domain.model.Standing
import com.futsoccerchamp.presentation.common.EmptyState

private val COLUMNS = listOf("P", "J", "V", "E", "D", "GP", "GC", "SG")

@Composable
fun StandingsTab(
    standings: List<Standing>,
    modifier: Modifier = Modifier
) {
    if (standings.isEmpty()) {
        EmptyState(
            title = "Sem classificação",
            subtitle = "Cadastre times e registre resultados para a tabela ser calculada.",
            modifier = modifier
        )
        return
    }

    val scrollState = rememberScrollState()

    Column(modifier) {
        StandingsHeader(scrollState)
        HorizontalDivider()
        LazyColumn {
            itemsIndexed(standings, key = { _, item -> item.teamId }) { index, standing ->
                StandingsRow(index + 1, standing, scrollState)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun StandingsHeader(scrollState: androidx.compose.foundation.ScrollState) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )
            Text(
                "Time",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                COLUMNS.forEach { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandingsRow(
    position: Int,
    standing: Standing,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val values = listOf(
        standing.points,
        standing.played,
        standing.wins,
        standing.draws,
        standing.losses,
        standing.goalsFor,
        standing.goalsAgainst,
        standing.goalDifference
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            position.toString(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(32.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(standing.teamName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(
                "${standing.efficiency}% de aproveitamento",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            values.forEachIndexed { index, value ->
                val text = if (index == values.lastIndex && value > 0) "+$value" else value.toString()
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(28.dp)
                )
            }
        }
    }
}
