package com.futsoccerchamp.presentation.matches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.Avatar
import com.futsoccerchamp.presentation.teams.TeamBadge

@Composable
fun MatchResultDialog(
    match: Match,
    homeTeam: Team?,
    awayTeam: Team?,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    onClearResult: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (homeGoals: Int, awayGoals: Int, goals: Map<String, Int>, saves: Map<String, Int>) -> Unit
) {
    val goals = remember { mutableStateMapOf<String, Int>().apply { putAll(match.goals) } }
    val saves = remember { mutableStateMapOf<String, Int>().apply { putAll(match.saves) } }
    var selectedTeamId by remember { mutableStateOf(match.homeTeamId) }

    val homeGoals = homePlayers.sumOf { goals[it.id] ?: 0 }
    val awayGoals = awayPlayers.sumOf { goals[it.id] ?: 0 }

    val selectedPlayers = if (selectedTeamId == match.homeTeamId) homePlayers else awayPlayers
    val selectedTeam = if (selectedTeamId == match.homeTeamId) homeTeam else awayTeam

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resultado da partida") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Scoreboard(
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    homeGoals = homeGoals,
                    awayGoals = awayGoals,
                    selectedTeamId = selectedTeamId,
                    onSelect = { selectedTeamId = it }
                )

                HorizontalDivider()

                Text(
                    selectedTeam?.name.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (selectedPlayers.isEmpty()) {
                    Text(
                        "Este time ainda não tem jogadores cadastrados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    selectedPlayers.forEach { player ->
                        PlayerStatRow(
                            player = player,
                            value = if (player.isGoalkeeper) saves[player.id] ?: 0 else goals[player.id] ?: 0,
                            onChange = { value ->
                                if (player.isGoalkeeper) saves[player.id] = value else goals[player.id] = value
                            }
                        )
                    }
                }
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
private fun Scoreboard(
    homeTeam: Team?,
    awayTeam: Team?,
    homeGoals: Int,
    awayGoals: Int,
    selectedTeamId: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectableTeam(
            team = homeTeam,
            selected = homeTeam?.id == selectedTeamId,
            onClick = { homeTeam?.id?.let(onSelect) },
            modifier = Modifier.weight(1f)
        )

        Text(
            "$homeGoals  $awayGoals",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        SelectableTeam(
            team = awayTeam,
            selected = awayTeam?.id == selectedTeamId,
            onClick = { awayTeam?.id?.let(onSelect) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectableTeam(
    team: Team?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "teamBorder"
    )

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .border(width = if (selected) 3.dp else 1.dp, color = borderColor, shape = CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            team?.let { TeamBadge(it, size = 48) }
        }
        Text(
            team?.name.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun PlayerStatRow(player: Player, value: Int, onChange: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (value > 0) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(photo = player.photo, initials = player.name.take(2).uppercase(), size = 36)

            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(player.name, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (player.isGoalkeeper) Icons.Default.SportsHandball else Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        if (player.isGoalkeeper) "Defesas" else "Gols",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = { onChange((value - 1).coerceAtLeast(0)) },
                enabled = value > 0,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.size(18.dp))
            }
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )
            IconButton(onClick = { onChange(value + 1) }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(18.dp))
            }
        }
    }
}
