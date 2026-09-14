package com.futsoccerchamp.presentation.rankings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.domain.model.PlayerRanking
import com.futsoccerchamp.presentation.common.Avatar
import com.futsoccerchamp.presentation.common.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsTab(
    scorers: List<PlayerRanking>,
    goalkeepers: List<PlayerRanking>,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableIntStateOf(0) }
    val showingScorers = selected == 0
    val ranking = if (showingScorers) scorers else goalkeepers

    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selected) {
            Tab(selected = showingScorers, onClick = { selected = 0 }, text = { Text("Artilharia") })
            Tab(selected = !showingScorers, onClick = { selected = 1 }, text = { Text("Defesas") })
        }

        Box(Modifier.fillMaxSize()) {
            if (ranking.isEmpty()) {
                EmptyState(
                    title = if (showingScorers) "Nenhum gol registrado" else "Nenhuma defesa registrada",
                    subtitle = "Cadastre os jogadores e registre os resultados das partidas."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(ranking, key = { _, item -> item.playerId }) { index, item ->
                        RankingCard(
                            position = index + 1,
                            ranking = item,
                            unit = if (showingScorers) "gols" else "defesas"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingCard(position: Int, ranking: PlayerRanking, unit: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                position.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )
            Avatar(
                photo = ranking.photo,
                initials = ranking.playerName.take(2).uppercase(),
                size = 44
            )
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(ranking.playerName, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOfNotNull(
                        ranking.teamName.takeIf { it.isNotBlank() },
                        ranking.number.takeIf { it > 0 }?.let { "Camisa $it" }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    ranking.value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$unit · ${ranking.average}/jogo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
