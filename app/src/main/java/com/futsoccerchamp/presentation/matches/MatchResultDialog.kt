package com.futsoccerchamp.presentation.matches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.Avatar
import com.futsoccerchamp.presentation.teams.TeamBadge

private enum class Attempt { NONE, SCORED, MISSED }

private const val PENALTIES_PER_TEAM = 5

@OptIn(ExperimentalMaterial3Api::class)
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

    var homeKeeperId by remember(homePlayers) { mutableStateOf(defaultKeeper(homePlayers)) }
    var awayKeeperId by remember(awayPlayers) { mutableStateOf(defaultKeeper(awayPlayers)) }
    var selectedTeamId by remember { mutableStateOf(match.homeTeamId) }

    fun count(players: List<Player>, attempt: Attempt) = players.count { attempts[it.id] == attempt }

    val homeGoals = count(homePlayers, Attempt.SCORED)
    val awayGoals = count(awayPlayers, Attempt.SCORED)
    val homeMisses = count(homePlayers, Attempt.MISSED)
    val awayMisses = count(awayPlayers, Attempt.MISSED)

    val showingHome = selectedTeamId == match.homeTeamId
    val squad = if (showingHome) homePlayers else awayPlayers
    val keeperId = if (showingHome) homeKeeperId else awayKeeperId
    val savesForKeeper = if (showingHome) awayMisses else homeMisses
    val selectedTeam = if (showingHome) homeTeam else awayTeam
    val taken = if (showingHome) homeGoals + homeMisses else awayGoals + awayMisses
    val remaining = PENALTIES_PER_TEAM - taken

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Disputa de pênaltis") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    },
                    actions = {
                        if (match.finished) {
                            TextButton(
                                onClick = {
                                    onClearResult()
                                    onDismiss()
                                }
                            ) { Text("Limpar") }
                        }
                    }
                )
            },
            bottomBar = {
                Surface(tonalElevation = 3.dp) {
                    Column(Modifier.padding(16.dp)) {
                        KeeperSection(
                            squad = squad,
                            selectedId = keeperId,
                            saves = savesForKeeper,
                            teamName = selectedTeam?.name.orEmpty(),
                            onSelect = { if (showingHome) homeKeeperId = it else awayKeeperId = it }
                        )

                        Button(
                            onClick = {
                                val goals = attempts.filterValues { it == Attempt.SCORED }.mapValues { 1 }
                                val misses = attempts.filterValues { it == Attempt.MISSED }.mapValues { 1 }
                                val saves = buildMap {
                                    if (homeKeeperId.isNotBlank() && awayMisses > 0) put(homeKeeperId, awayMisses)
                                    if (awayKeeperId.isNotBlank() && homeMisses > 0) put(awayKeeperId, homeMisses)
                                }
                                onConfirm(homeGoals, awayGoals, goals, misses, saves)
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Text("Salvar resultado")
                        }
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
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
                    if (remaining > 0) {
                        "${selectedTeam?.name.orEmpty()} · $taken de $PENALTIES_PER_TEAM cobranças"
                    } else {
                        "${selectedTeam?.name.orEmpty()} · série encerrada"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (remaining > 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                if (squad.isEmpty()) {
                    Text(
                        "Este time não tem jogadores nesta temporada. Cadastre o elenco em Times.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp, end = 16.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(squad, key = { it.id }) { player ->
                            val attempt = attempts[player.id] ?: Attempt.NONE
                            ShooterRow(
                                player = player,
                                attempt = attempt,
                                enabled = attempt != Attempt.NONE || remaining > 0,
                                onChange = { attempts[player.id] = it }
                            )
                        }
                    }
                }
            }
        }
    }
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "teamBorder"
    )

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(width = 3.dp, color = borderColor, shape = CircleShape)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            team?.let { TeamBadge(it, size = 50) }
        }
        Text(
            team?.name.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            if (selected) "Escalando" else "Tocar para escalar",
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun ShooterRow(player: Player, attempt: Attempt, enabled: Boolean, onChange: (Attempt) -> Unit) {
    val background = when (attempt) {
        Attempt.SCORED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        Attempt.MISSED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        Attempt.NONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Surface(shape = RoundedCornerShape(14.dp), color = background, modifier = Modifier.fillMaxWidth()) {
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
    icon: ImageVector,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    color: Color,
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
        modifier = Modifier.padding(start = 6.dp).size(38.dp).clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun KeeperSection(
    squad: List<Player>,
    selectedId: String,
    saves: Int,
    teamName: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.SportsHandball,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Quem defendeu pelo $teamName",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        if (squad.isEmpty()) {
            Text(
                "Sem jogadores cadastrados neste time.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
            return@Column
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            squad.forEach { player ->
                FilterChip(
                    selected = player.id == selectedId,
                    onClick = { onSelect(player.id) },
                    label = { Text(player.name) },
                    leadingIcon = if (player.isGoalkeeper) {
                        {
                            Icon(
                                Icons.Default.SportsHandball,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }

        Text(
            "$saves ${if (saves == 1) "defesa" else "defesas"} nesta partida",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
