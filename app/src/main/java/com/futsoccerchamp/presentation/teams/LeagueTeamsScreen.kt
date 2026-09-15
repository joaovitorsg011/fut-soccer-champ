package com.futsoccerchamp.presentation.teams

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.presentation.players.PlayersScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueTeamsScreen(
    viewModel: LeagueTeamsViewModel,
    readOnly: Boolean,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTeamDialog by remember { mutableStateOf(false) }
    var openedTeamId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    val openedTeam = openedTeamId?.let { id -> state.teams.firstOrNull { it.id == id } }

    if (openedTeam != null) {
        PlayersScreen(
            team = openedTeam,
            players = state.playersOf(openedTeam.id),
            readOnly = readOnly,
            onAdd = { name, number, position ->
                viewModel.addPlayer(openedTeam.id, name, number, position)
            },
            onEdit = viewModel::updatePlayer,
            onDelete = viewModel::deletePlayer,
            onBack = { openedTeamId = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Times da liga")
                        Text(
                            "${state.teams.size} times · ${state.players.size} jogadores",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!readOnly) {
                FloatingActionButton(onClick = { showTeamDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar time")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (state.loading) {
                LoadingBox()
            } else {
                TeamsTab(
                    teams = state.teams,
                    teamLimit = 0,
                    readOnly = readOnly,
                    playerCountOf = state::playerCountOf,
                    onOpenTeam = { openedTeamId = it.id },
                    onEdit = viewModel::updateTeam,
                    onDelete = viewModel::deleteTeam
                )
            }
        }
    }

    if (showTeamDialog) {
        TeamFormDialog(
            title = "Novo time",
            onDismiss = { showTeamDialog = false },
            onConfirm = { name, abbreviation, logo ->
                viewModel.addTeam(name, abbreviation, logo)
                showTeamDialog = false
            }
        )
    }
}
