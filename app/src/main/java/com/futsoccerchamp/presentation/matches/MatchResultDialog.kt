package com.futsoccerchamp.presentation.matches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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

private enum class Attempt { NONE, SCORED, MISSED }

private const val PENALTIES_PER_TEAM = 5

@Composable
fun MatchResultDialog(
    match: Match,
    homeTeam: Team?,
    awayTeam: Team?,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    onClearResult: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (
        homeGoals: Int,
        awayGoals: Int,
        goals: Map<String, Int>,
        misses: Map<String, Int>,
        saves: Map<String, Int>
    ) -> Unit
) {
    val attempts = remember {
        mutableStateMapOf<String, Attempt>().apply {
            match.goals.keys.forEach { put(it, Attempt.SCORED) }
            match.misses.keys.forEach { put(it, Attempt.MISSED) }
        }
    }

    fun defaultKeeper(players: List<Player>) =
        (match.saves.keys.firstOrNull { id -> players.any { it.id == id } }
            ?: players.firstOrNull { it.isGoalkeeper }?.id
            ?: players.firstOrNull()?.id).orEmpty()

    var homeKeeperId by remember { mutableStateOf(defaultKeeper(homePlayers)) }
    var awayKeeperId by remember { mutableStateOf(defaultKeeper(awayPlayers)) }

    var selectedTeamId by remember { mutableStateOf(match.homeTeamId) }

    val homeShooters = homePlayers
    val awayShooters = awayPlayers

    fun count(players: List<Player>, attempt: Attempt) =
        players.count { attempts[it.id] == attempt }

    val homeGoals = count(homeShooters, Attempt.SCORED)
    val awayGoals = count(awayShooters, Attempt.SCORED)
    val homeMisses = count(homeShooters, Attempt.MISSED)
    val awayMisses = count(awayShooters, Attempt.MISSED)
    val homeTaken = homeGoals + homeMisses
    val awayTaken = awayGoals + awayMisses

    val showingHome = selectedTeamId == match.homeTeamId
    val shooters = if (showingHome) homeShooters else awayShooters
    val keepers = if (showingHome) homePlayers else awayPlayers
    val keeperId = if (showingHome) homeKeeperId else awayKeeperId
    val savesForKeeper = if (showingHome) awayMisses else homeMisses
    val selectedTeam = if (showingHome) homeTeam else awayTeam
    val taken = if (showingHome) homeTaken else awayTaken
    val remaining = PENALTIES_PER_TEAM - taken

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Disputa de pênaltis") },
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

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        selectedTeam?.name.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (remaining > 0) {
                            "$taken de $PENALTIES_PER_TEAM cobranças registradas"
                        } else {
                            "Série encerrada: $PENALTIES_PER_TEAM cobranças registradas"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (remaining > 0) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                if (shooters.isEmpty()) {
                    Text(
                        "Cadastre jogadores para registrar as cobranças.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    shooters.forEach { player ->
                        val attempt = attempts[player.id] ?: Attempt.NONE
                        ShooterRow(
                            player = player,
                            attempt = attempt,
                            enabled = attempt != Attempt.NONE || remaining > 0,
                            onChange = { attempts[player.id] = it }
                        )
                    }
                }

                HorizontalDivider()
                KeeperSection(
                    keepers = keepers,
                    selectedId = keeperId,
                    saves = savesForKeeper,
                    onSelect = { if (showingHome) homeKeeperId = it else awayKeeperId = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val goals = attempts.filterValues { it == Attempt.SCORED }.mapValues { 1 }
                    val misses = attempts.filterValues { it == Attempt.MISSED }.mapValues { 1 }
                    val saves = buildMap {
                        if (homeKeeperId.isNotBlank() && awayMisses > 0) put(homeKeeperId, awayMisses)
                        if (awayKeeperId.isNotBlank() && homeMisses > 0) put(awayKeeperId, homeMisses)
                    }
                    onConfirm(homeGoals, awayGoals, goals, misses, saves)
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
private fun ShooterRow(
    player: Player,
    attempt: Attempt,
    enabled: Boolean,
    onChange: (Attempt) -> Unit
) {
    val background = when (attempt) {
        Attempt.SCORED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        Attempt.MISSED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        Attempt.NONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(photo = player.photo, initials = player.name.take(2).uppercase(), size = 36)

            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(player.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    when {
                        attempt == Attempt.SCORED -> "Converteu"
                        attempt == Attempt.MISSED -> "Perdeu"
                        !enabled -> "Fora da série"
                        else -> "Não cobrou"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AttemptButton(
                icon = Icons.Default.Check,
                description = "Converteu",
                selected = attempt == Attempt.SCORED,
                enabled = enabled,
                color = MaterialTheme.colorScheme.primary,
                onClick = { onChange(if (attempt == Attempt.SCORED) Attempt.NONE else Attempt.SCORED) }
            )
            AttemptButton(
                icon = Icons.Default.Close,
                description = "Perdeu",
                selected = attempt == Attempt.MISSED,
                enabled = enabled,
                color = MaterialTheme.colorScheme.error,
                onClick = { onChange(if (attempt == Attempt.MISSED) Attempt.NONE else Attempt.MISSED) }
            )
        }
    }
}

@Composable
private fun AttemptButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val tint = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        enabled -> color
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Surface(
        shape = CircleShape,
        color = if (selected) color else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(start = 6.dp)
            .size(36.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeeperSection(
    keepers: List<Player>,
    selectedId: String,
    saves: Int,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quem defendeu", style = MaterialTheme.typography.labelLarge)
        Text(
            "Qualquer jogador pode ir ao gol. As defesas vão para quem estiver selecionado.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (keepers.isEmpty()) {
            Text(
                "Este time ainda não tem jogadores cadastrados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            return@Column
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            keepers.forEach { keeper ->
                FilterChip(
                    selected = keeper.id == selectedId,
                    onClick = { onSelect(keeper.id) },
                    label = { Text(keeper.name) },
                    leadingIcon = if (keeper.isGoalkeeper) {
                        { Icon(Icons.Default.SportsHandball, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else {
                        null
                    }
                )
            }
        }

        Text(
            "$saves ${if (saves == 1) "defesa" else "defesas"} nesta partida, a partir das cobranças perdidas pelo adversário",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
