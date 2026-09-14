package com.futsoccerchamp.presentation.championship

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.EmojiEvents as EmojiEventsIcon
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.players.PlayersScreen
import com.futsoccerchamp.presentation.rankings.RankingsTab
import com.futsoccerchamp.presentation.rounds.RoundsTab
import com.futsoccerchamp.presentation.standings.StandingsTab
import com.futsoccerchamp.presentation.statistics.StatisticsSection
import com.futsoccerchamp.presentation.teams.TeamFormDialog
import com.futsoccerchamp.presentation.teams.TeamsTab
import com.futsoccerchamp.presentation.theme.ThemeMode
import kotlinx.coroutines.launch

private enum class Section(val label: String, val icon: ImageVector) {
    STANDINGS("Classificação", Icons.Default.Leaderboard),
    TEAMS("Times", Icons.Default.Groups),
    ROUNDS("Rodadas e partidas", Icons.Default.SportsSoccer),
    RANKINGS("Artilharia e defesas", Icons.Default.EmojiEventsIcon),
    STATISTICS("Estatísticas", Icons.Default.QueryStats)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionshipScreen(
    viewModel: ChampionshipViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBackToChampionships: () -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var section by remember { mutableStateOf(Section.STANDINGS) }
    var showTeamDialog by remember { mutableStateOf(false) }
    var showEditChampionship by remember { mutableStateOf(false) }
    var openedTeamId by remember { mutableStateOf<String?>(null) }
    var confirmRestart by remember { mutableStateOf(false) }

    val openedTeam: Team? = openedTeamId?.let { state.team(it) }

    if (openedTeam != null) {
        PlayersScreen(
            team = openedTeam,
            players = state.playersOf(openedTeam.id),
            onAdd = { name, number, position ->
                viewModel.addPlayer(openedTeam.id, name, number, position)
            },
            onEdit = viewModel::updatePlayer,
            onDelete = viewModel::deletePlayer,
            onBack = { openedTeamId = null }
        )
        return
    }

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
                        state.championship?.name ?: "Campeonato",
                        style = MaterialTheme.typography.titleLarge
                    )
                    state.championship?.season?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            "Temporada $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("Editar campeonato") },
                    selected = false,
                    onClick = {
                        showEditChampionship = true
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(themeMode.icon(), contentDescription = null) },
                    label = { Text(themeMode.toggled().label) },
                    selected = false,
                    onClick = { onThemeModeChange(themeMode.toggled()) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                    label = { Text("Reiniciar competição") },
                    selected = false,
                    onClick = {
                        confirmRestart = true
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    label = { Text("Meus campeonatos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onBackToChampionships()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    label = { Text("Sair da conta") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(section.label) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                when (section) {
                    Section.TEAMS -> FloatingActionButton(onClick = { showTeamDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar time")
                    }
                    else -> Unit
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when {
                    state.loading -> LoadingBox()
                    section == Section.STANDINGS -> StandingsTab(standings = state.standings)
                    section == Section.TEAMS -> TeamsTab(
                        teams = state.teams,
                        teamLimit = state.championship?.teamLimit ?: 0,
                        playerCountOf = state::playerCountOf,
                        onOpenTeam = { openedTeamId = it.id },
                        onEdit = viewModel::updateTeam,
                        onDelete = viewModel::deleteTeam
                    )
                    section == Section.ROUNDS -> RoundsTab(
                        rounds = state.rounds,
                        teams = state.teams,
                        matchesOf = state::matchesOfRound,
                        teamOf = state::team,
                        playersOf = state::playersOf,
                        drawLocked = state.drawLocked,
                        onEditMatch = viewModel::updateMatch,
                        onRegisterResult = viewModel::registerResult,
                        onClearResult = viewModel::clearResult,
                        onGenerateRounds = viewModel::generateAllRounds
                    )
                    section == Section.RANKINGS -> RankingsTab(
                        scorers = state.scorers,
                        goalkeepers = state.goalkeepers
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

    if (confirmRestart) {
        ConfirmDialog(
            title = "Reiniciar competição",
            message = "Todas as rodadas, partidas e placares serão apagados. Os times e seus jogadores são mantidos, e a tabela poderá ser sorteada novamente.",
            confirmLabel = "Reiniciar",
            onConfirm = {
                viewModel.restartCompetition()
                confirmRestart = false
            },
            onDismiss = { confirmRestart = false }
        )
    }

    if (showTeamDialog) {
        TeamFormDialog(
            title = "Novo time",
            onDismiss = { showTeamDialog = false },
            onConfirm = { name, abbreviation, logoUrl ->
                viewModel.addTeam(name, abbreviation, logoUrl)
                showTeamDialog = false
            }
        )
    }

    if (showEditChampionship) {
        val championship = state.championship
        ChampionshipFormDialog(
            title = "Editar campeonato",
            initialName = championship?.name.orEmpty(),
            initialSeason = championship?.season.orEmpty(),
            initialTeamLimit = championship?.teamLimit?.takeIf { it > 0 }?.toString().orEmpty(),
            initialDescription = championship?.description.orEmpty(),
            onDismiss = { showEditChampionship = false },
            onConfirm = { name, season, teamLimit, description ->
                viewModel.updateChampionship(name, season, teamLimit, description)
                showEditChampionship = false
            }
        )
    }
}

private fun ThemeMode.icon(): ImageVector =
    if (this == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode
