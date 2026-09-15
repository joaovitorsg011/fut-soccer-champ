package com.futsoccerchamp.presentation.tournaments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.data.model.Tournament
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.EntityCard
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.presentation.common.NameFormDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(
    viewModel: TournamentsViewModel,
    readOnly: Boolean,
    onOpenTournament: (String) -> Unit,
    onOpenTeams: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var toEdit by remember { mutableStateOf<Tournament?>(null) }
    var toDelete by remember { mutableStateOf<Tournament?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.league?.name ?: "Liga")
                        Text("Torneios", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTeams) {
                        Icon(Icons.Default.Groups, contentDescription = "Times da liga")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!readOnly) {
                FloatingActionButton(onClick = { showCreate = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Criar torneio")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> LoadingBox()
                state.tournaments.isEmpty() -> EmptyState(
                    title = "Nenhum torneio",
                    subtitle = if (readOnly) {
                        "Esta liga ainda não criou torneios."
                    } else {
                        "Crie um torneio, como Brasileirão ou Copa, para organizar as temporadas."
                    }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tournaments, key = { it.id }) { tournament ->
                        EntityCard(
                            title = tournament.name,
                            subtitle = tournament.description,
                            icon = Icons.Default.EmojiEvents,
                            onClick = { onOpenTournament(tournament.id) },
                            editable = !readOnly,
                            onEdit = { toEdit = tournament },
                            onDelete = { toDelete = tournament }
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        NameFormDialog(
            title = "Novo torneio",
            nameLabel = "Nome do torneio",
            namePlaceholder = "Brasileirão",
            nameIcon = Icons.Default.EmojiEvents,
            onDismiss = { showCreate = false },
            onConfirm = { name, description ->
                viewModel.create(name, description)
                showCreate = false
            }
        )
    }

    toEdit?.let { tournament ->
        NameFormDialog(
            title = "Editar torneio",
            nameLabel = "Nome do torneio",
            nameIcon = Icons.Default.EmojiEvents,
            initialName = tournament.name,
            initialDescription = tournament.description,
            onDismiss = { toEdit = null },
            onConfirm = { name, description ->
                viewModel.update(tournament, name, description)
                toEdit = null
            }
        )
    }

    toDelete?.let { tournament ->
        ConfirmDialog(
            title = "Excluir torneio",
            message = "Excluir \"${tournament.name}\" apaga também suas temporadas, rodadas e partidas.",
            onConfirm = {
                viewModel.delete(tournament)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}
