package com.futsoccerchamp.presentation.seasons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.data.model.Season
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.AppTextField
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.EntityCard
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.presentation.common.dismissKeyboardOnTap
import com.futsoccerchamp.presentation.teams.TeamBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonsScreen(
    viewModel: SeasonsViewModel,
    readOnly: Boolean,
    onOpenSeason: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var toEdit by remember { mutableStateOf<Season?>(null) }
    var toDelete by remember { mutableStateOf<Season?>(null) }

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
                        Text(state.tournament?.name ?: "Torneio")
                        Text("Temporadas", style = MaterialTheme.typography.labelSmall)
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
                FloatingActionButton(onClick = { showCreate = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Criar temporada")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> LoadingBox()
                state.seasons.isEmpty() -> EmptyState(
                    title = "Nenhuma temporada",
                    subtitle = if (readOnly) {
                        "Este torneio ainda não tem temporadas."
                    } else {
                        "Crie a temporada e escolha quais times da liga vão disputá-la."
                    }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.seasons, key = { it.id }) { season ->
                        EntityCard(
                            title = season.label,
                            subtitle = if (season.teamIds.isEmpty()) {
                                "Sem participantes definidos"
                            } else {
                                "${season.teamIds.size} times participantes"
                            },
                            icon = Icons.Default.CalendarMonth,
                            onClick = { onOpenSeason(season.id) },
                            editable = !readOnly,
                            onEdit = { toEdit = season },
                            onDelete = { toDelete = season }
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        SeasonFormDialog(
            title = "Nova temporada",
            teams = state.teams,
            onDismiss = { showCreate = false },
            onConfirm = { label, teamIds ->
                viewModel.create(label, teamIds)
                showCreate = false
            }
        )
    }

    toEdit?.let { season ->
        SeasonFormDialog(
            title = "Editar temporada",
            teams = state.teams,
            initialLabel = season.label,
            initialTeamIds = season.teamIds,
            onDismiss = { toEdit = null },
            onConfirm = { label, teamIds ->
                viewModel.update(season, label, teamIds)
                toEdit = null
            }
        )
    }

    toDelete?.let { season ->
        ConfirmDialog(
            title = "Excluir temporada",
            message = "Excluir \"${season.label}\" apaga suas rodadas, partidas e a classificação.",
            onConfirm = {
                viewModel.delete(season)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun SeasonFormDialog(
    title: String,
    teams: List<Team>,
    initialLabel: String = "",
    initialTeamIds: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (label: String, teamIds: List<String>) -> Unit
) {
    var label by remember { mutableStateOf(initialLabel) }
    val selected = remember { mutableStateListOf<String>().apply { addAll(initialTeamIds) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .dismissKeyboardOnTap()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = "Temporada",
                    placeholder = "2026",
                    leadingIcon = Icons.Default.CalendarMonth
                )

                HorizontalDivider()

                Text(
                    "Times participantes (${selected.size} de ${teams.size})",
                    style = MaterialTheme.typography.labelLarge
                )

                if (teams.isEmpty()) {
                    Text(
                        "A liga ainda não tem times. Crie a temporada agora e vincule os participantes depois, em Times e elencos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    teams.forEach { team ->
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
        confirmButton = {
            TextButton(onClick = { onConfirm(label, selected.toList()) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
