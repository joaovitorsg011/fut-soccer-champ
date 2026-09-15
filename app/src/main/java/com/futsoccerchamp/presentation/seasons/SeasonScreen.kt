package com.futsoccerchamp.presentation.seasons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.presentation.rankings.RankingsTab
import com.futsoccerchamp.presentation.rounds.RoundsTab
import com.futsoccerchamp.presentation.standings.StandingsTab
import com.futsoccerchamp.presentation.statistics.StatisticsSection
import com.futsoccerchamp.presentation.teams.TeamBadge
import com.futsoccerchamp.presentation.teams.TeamsTab
import kotlinx.coroutines.launch

private enum class Section(val label: String, val icon: ImageVector) {
    STANDINGS("Classificação", Icons.Default.Leaderboard),
    ROUNDS("Rodadas e partidas", Icons.Default.SportsSoccer),
    RANKINGS("Artilharia e defesas", Icons.Default.EmojiEvents),
    TEAMS("Participantes", Icons.Default.Groups),
    STATISTICS("Estatísticas", Icons.Default.QueryStats)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonScreen(
    viewModel: SeasonViewModel,
    readOnly: Boolean,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var section by remember { mutableStateOf(Section.STANDINGS) }
    var confirmRestart by remember { mutableStateOf(false) }
    var showParticipants by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        state.season?.label ?: "Temporada",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        if (readOnly) "Acesso de observação" else "${state.teams.size} times participantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()

                Section.entries.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                        selected = item == section,
                        onClick = {
                            section = item
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                if (!readOnly) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                        label = { Text("Reiniciar temporada") },
                        selected = false,
                        onClick = {
                            confirmRestart = true
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(section.label)
                            state.season?.label?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when {
                    state.loading -> LoadingBox()
                    section == Section.STANDINGS -> StandingsTab(standings = state.standings)
                    section == Section.ROUNDS -> RoundsTab(
                        rounds = state.rounds,
                        teams = state.teams,
                        matchesOf = state::matchesOfRound,
                        teamOf = state::team,
                        playersOf = state::playersOf,
                        drawLocked = state.drawLocked || readOnly,
                        readOnly = readOnly,
                        onEditMatch = viewModel::updateMatch,
                        onRegisterResult = viewModel::registerResult,
                        onClearResult = viewModel::clearResult,
                        onGenerateRounds = viewModel::generateAllRounds
                    )
                    section == Section.RANKINGS -> RankingsTab(
                        scorers = state.scorers,
                        goalkeepers = state.goalkeepers,
                        missers = state.missers
                    )
                    section == Section.TEAMS -> ParticipantsTab(
                        state = state,
                        readOnly = readOnly,
                        onEditParticipants = { showParticipants = true }
                    )
                    else -> StatisticsSection(
                        standings = state.standings,
                        matches = state.matches,
                        teamOf = state::team
                    )
                }
            }
        }
    }

    if (showParticipants) {
        ParticipantsDialog(
            leagueTeams = state.leagueTeams,
            selectedIds = state.season?.teamIds.orEmpty(),
            onDismiss = { showParticipants = false },
            onConfirm = { ids ->
                viewModel.updateParticipants(ids)
                showParticipants = false
            }
        )
    }

    if (confirmRestart) {
        ConfirmDialog(
            title = "Reiniciar temporada",
            message = "Todas as rodadas, partidas e placares serão apagados. Os times participantes continuam os mesmos.",
            confirmLabel = "Reiniciar",
            onConfirm = {
                viewModel.restartSeason()
                confirmRestart = false
            },
            onDismiss = { confirmRestart = false }
        )
    }
}

@Composable
private fun ParticipantsTab(
    state: SeasonUiState,
    readOnly: Boolean,
    onEditParticipants: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        if (!readOnly) {
            Button(
                onClick = onEditParticipants,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Icon(Icons.Default.Groups, contentDescription = null)
                Text("Escolher participantes", modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (state.teams.isEmpty()) {
            EmptyState(
                title = "Nenhum participante",
                subtitle = if (readOnly) {
                    "Esta temporada ainda não tem times definidos."
                } else {
                    "Escolha quais times da liga vão disputar esta temporada."
                }
            )
            return@Column
        }

        TeamsTab(
            teams = state.teams,
            teamLimit = 0,
            readOnly = true,
            playerCountOf = { teamId -> state.playersOf(teamId).size },
            onOpenTeam = {},
            onEdit = { _, _, _, _ -> },
            onDelete = {}
        )
    }
}

@Composable
private fun ParticipantsDialog(
    leagueTeams: List<Team>,
    selectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(selectedIds) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Participantes da temporada") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (leagueTeams.isEmpty()) {
                    Text(
                        "A liga ainda não tem times. Cadastre-os em Times e elencos e volte aqui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "${selected.size} de ${leagueTeams.size} times selecionados",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    leagueTeams.forEach { team ->
                        val checked = team.id in selected
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    if (checked) selected.remove(team.id) else selected.add(team.id)
                                }
                            )
                            TeamBadge(team, size = 28)
                            Text(
                                team.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected.toList()) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
