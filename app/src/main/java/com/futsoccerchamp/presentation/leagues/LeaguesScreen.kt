package com.futsoccerchamp.presentation.leagues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.data.model.League
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.EntityCard
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.presentation.common.NameFormDialog
import com.futsoccerchamp.presentation.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesScreen(
    viewModel: LeaguesViewModel,
    readOnly: Boolean,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenLeague: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var leagueToEdit by remember { mutableStateOf<League?>(null) }
    var leagueToDelete by remember { mutableStateOf<League?>(null) }

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
                        Text(if (readOnly) "Todas as ligas" else "Minhas ligas")
                        if (readOnly) {
                            Text(
                                "Acesso de observação",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onThemeModeChange(themeMode.toggled()) }) {
                        Icon(themeMode.icon(), contentDescription = themeMode.toggled().label)
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.loading -> LoadingBox()
                state.leagues.isEmpty() -> EmptyState(
                    title = if (readOnly) "Nenhuma liga cadastrada" else "Você ainda não tem liga",
                    subtitle = if (readOnly) {
                        "Assim que um administrador criar a conta dele, a liga aparece aqui."
                    } else {
                        "Sua liga é criada junto com a conta. Fale com o responsável se ela não aparecer."
                    }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.leagues, key = { it.id }) { league ->
                        EntityCard(
                            title = league.name,
                            subtitle = listOfNotNull(
                                league.ownerName.takeIf { it.isNotBlank() }?.let { "Administrada por $it" },
                                league.description.takeIf { it.isNotBlank() }
                            ).joinToString(" · "),
                            icon = Icons.Default.Shield,
                            onClick = { onOpenLeague(league.id) },
                            editable = !readOnly,
                            onEdit = { leagueToEdit = league },
                            onDelete = { leagueToDelete = league }
                        )
                    }
                }
            }
        }
    }

    leagueToEdit?.let { league ->
        NameFormDialog(
            title = "Editar liga",
            nameLabel = "Nome da liga",
            nameIcon = Icons.Default.Shield,
            initialName = league.name,
            initialDescription = league.description,
            onDismiss = { leagueToEdit = null },
            onConfirm = { name, description ->
                viewModel.rename(league, name, description)
                leagueToEdit = null
            }
        )
    }

    leagueToDelete?.let { league ->
        ConfirmDialog(
            title = "Excluir liga",
            message = "Excluir \"${league.name}\" apaga torneios, temporadas, times, jogadores e partidas.",
            onConfirm = {
                viewModel.delete(league)
                leagueToDelete = null
            },
            onDismiss = { leagueToDelete = null }
        )
    }
}

private fun ThemeMode.icon(): ImageVector =
    if (this == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode
