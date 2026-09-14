package com.futsoccerchamp.presentation.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.AppTextField
import com.futsoccerchamp.presentation.common.Avatar
import com.futsoccerchamp.presentation.common.dismissKeyboardOnTap

@Composable
fun MatchResultDialog(
    match: Match,
    homeTeam: Team?,
    awayTeam: Team?,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    onClearResult: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (homeGoals: String, awayGoals: String, goals: Map<String, Int>, saves: Map<String, Int>) -> Unit
) {
    var homeGoals by remember { mutableStateOf(match.homeGoals?.toString().orEmpty()) }
    var awayGoals by remember { mutableStateOf(match.awayGoals?.toString().orEmpty()) }
    val goals = remember { mutableStateMapOf<String, Int>().apply { putAll(match.goals) } }
    val saves = remember { mutableStateMapOf<String, Int>().apply { putAll(match.saves) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resultado da partida") },
        text = {
            Column(
                modifier = Modifier
                    .dismissKeyboardOnTap()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScoreRow(
                    homeName = homeTeam?.name.orEmpty(),
                    awayName = awayTeam?.name.orEmpty(),
                    homeGoals = homeGoals,
                    awayGoals = awayGoals,
                    onHomeChange = { homeGoals = it },
                    onAwayChange = { awayGoals = it }
                )

                TeamStatsSection(
                    teamName = homeTeam?.name.orEmpty(),
                    players = homePlayers,
                    goals = goals,
                    saves = saves,
                    expectedGoals = homeGoals.toIntOrNull() ?: 0
                )

                TeamStatsSection(
                    teamName = awayTeam?.name.orEmpty(),
                    players = awayPlayers,
                    goals = goals,
                    saves = saves,
                    expectedGoals = awayGoals.toIntOrNull() ?: 0
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        homeGoals,
                        awayGoals,
                        goals.filterValues { it > 0 },
                        saves.filterValues { it > 0 }
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            if (match.finished) {
                TextButton(
                    onClick = {
                        onClearResult()
                        onDismiss()
                    }
                ) { Text("Limpar placar") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}

@Composable
private fun ScoreRow(
    homeName: String,
    awayName: String,
    homeGoals: String,
    awayGoals: String,
    onHomeChange: (String) -> Unit,
    onAwayChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTextField(
            value = homeGoals,
            onValueChange = { input -> onHomeChange(input.filter { it.isDigit() }.take(2)) },
            label = homeName.ifBlank { "Mandante" },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Text("x", style = MaterialTheme.typography.titleMedium)
        AppTextField(
            value = awayGoals,
            onValueChange = { input -> onAwayChange(input.filter { it.isDigit() }.take(2)) },
            label = awayName.ifBlank { "Visitante" },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TeamStatsSection(
    teamName: String,
    players: List<Player>,
    goals: MutableMap<String, Int>,
    saves: MutableMap<String, Int>,
    expectedGoals: Int
) {
    if (players.isEmpty()) return

    val assigned = players.sumOf { goals[it.id] ?: 0 }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(teamName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            "Gols atribuídos: $assigned de $expectedGoals",
            style = MaterialTheme.typography.labelSmall,
            color = if (assigned > expectedGoals) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        players.forEach { player ->
            PlayerStatRow(
                player = player,
                goals = goals[player.id] ?: 0,
                saves = saves[player.id] ?: 0,
                onGoalsChange = { goals[player.id] = it },
                onSavesChange = { saves[player.id] = it }
            )
        }
    }
}

@Composable
private fun PlayerStatRow(
    player: Player,
    goals: Int,
    saves: Int,
    onGoalsChange: (Int) -> Unit,
    onSavesChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(photo = player.photo, initials = player.name.take(2).uppercase(), size = 32)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(player.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                player.positionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Counter(
            label = if (player.isGoalkeeper) "Defesas" else "Gols",
            value = if (player.isGoalkeeper) saves else goals,
            onChange = if (player.isGoalkeeper) onSavesChange else onGoalsChange
        )
    }
}

@Composable
private fun Counter(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onChange((value - 1).coerceAtLeast(0)) },
                enabled = value > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.size(16.dp))
            }
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )
            IconButton(onClick = { onChange(value + 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(16.dp))
            }
        }
    }
}
