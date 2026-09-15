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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
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
import com.futsoccerchamp.data.model.Tournament
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.EntityCard
import com.futsoccerchamp.presentation.common.LoadingBox
import com.futsoccerchamp.presentation.common.NameFormDialog
import com.futsoccerchamp.presentation.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueScreen(
    viewModel: TournamentsViewModel,
    readOnly: Boolean,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenTournament: (String) -> Unit,
    onOpenTeams: () -> Unit,
    onSwitchLeague: () -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showCreate by remember { mutableStateOf(false) }
    var toEdit by remember { mutableStateOf<Tournament?>(null) }
    var toDelete by remember { mutableStateOf<Tournament?>(null) }
    var showEditLeague by remember { mutableStateOf(false) }

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
                    Text(state.league?.name ?: "Liga", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (readOnly) "Acesso de observação" else "${state.tournaments.size} torneios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    label = { Text("Torneios") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                    label = { Text("Times e elencos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenTeams()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                if (!readOnly) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                        label = { Text("Dados da liga") },
                        selected = false,
                        onClick = {
                            showEditLeague = true
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(themeMode.icon(), contentDescription = null) },
                    label = { Text(themeMode.toggled().label) },
                    selected = false,
                    onClick = { onThemeModeChange(themeMode.toggled()) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                    label = { Text(if (readOnly) "Todas as ligas" else "Minhas ligas") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSwitchLeague()
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
                    title = {
                        Column {
                            Text(state.league?.name ?: "Liga")
                            Text("Torneios", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menu")
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

    if (showEditLeague) {
        val league = state.league
        NameFormDialog(
            title = "Dados da liga",
            nameLabel = "Nome da liga",
            nameIcon = Icons.Default.Shield,
            initialName = league?.name.orEmpty(),
            initialDescription = league?.description.orEmpty(),
            onDismiss = { showEditLeague = false },
            onConfirm = { name, description ->
                viewModel.updateLeague(name, description)
                showEditLeague = false
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

private fun ThemeMode.icon(): ImageVector =
    if (this == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode
