package com.futsoccerchamp.presentation.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.PlayerPosition
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.AppTextField
import com.futsoccerchamp.presentation.common.Avatar
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.dismissKeyboardOnTap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    team: Team,
    players: List<Player>,
    readOnly: Boolean = false,
    subtitle: String = "Elenco",
    header: (@Composable () -> Unit)? = null,
    statsOf: ((String) -> Triple<Int, Int, Int>)? = null,
    onAdd: (name: String, number: String, position: String) -> Unit,
    onEdit: (Player, String, String, String) -> Unit,
    onDelete: (Player) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var playerToEdit by remember { mutableStateOf<Player?>(null) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(team.name)
                        Text(subtitle, style = MaterialTheme.typography.labelSmall)
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
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar jogador")
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (players.isEmpty() && header == null) {
                EmptyState(
                    title = "Nenhum jogador no elenco",
                    subtitle = "Adicione os jogadores para registrar gols e defesas nas partidas."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    header?.let { item { it() } }

                    items(players, key = { it.id }) { player ->
                        PlayerCard(
                            player = player,
                            editable = !readOnly,
                            stats = statsOf?.invoke(player.id),
                            onEdit = { playerToEdit = player },
                            onDelete = { playerToDelete = player }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        PlayerFormDialog(
            title = "Novo jogador",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, number, position ->
                onAdd(name, number, position)
                showAddDialog = false
            }
        )
    }

    playerToEdit?.let { player ->
        PlayerFormDialog(
            title = "Editar jogador",
            initialName = player.name,
            initialNumber = player.number.takeIf { it > 0 }?.toString().orEmpty(),
            initialPosition = player.position,
            onDismiss = { playerToEdit = null },
            onConfirm = { name, number, position ->
                onEdit(player, name, number, position)
                playerToEdit = null
            }
        )
    }

    playerToDelete?.let { player ->
        ConfirmDialog(
            title = "Excluir jogador",
            message = "\"${player.name}\" será removido do elenco.",
            onConfirm = {
                onDelete(player)
                playerToDelete = null
            },
            onDismiss = { playerToDelete = null }
        )
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    editable: Boolean,
    stats: Triple<Int, Int, Int>? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(photo = player.photo, initials = player.name.take(2).uppercase(), size = 48)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(player.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    listOfNotNull(
                        player.number.takeIf { it > 0 }?.let { "Camisa $it" },
                        player.positionLabel.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                stats?.let { (goals, saves, misses) ->
                    Text(
                        "$goals gols · $saves defesas · $misses erros",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (editable) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar jogador") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir jogador") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerFormDialog(
    title: String,
    initialName: String = "",
    initialNumber: String = "",
    initialPosition: String = PlayerPosition.FORWARD.name,
    onDismiss: () -> Unit,
    onConfirm: (name: String, number: String, position: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var number by remember { mutableStateOf(initialNumber) }
    var position by remember { mutableStateOf(initialPosition) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.dismissKeyboardOnTap().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome",
                    leadingIcon = Icons.Outlined.Person
                )
                AppTextField(
                    value = number,
                    onValueChange = { input -> number = input.filter { it.isDigit() }.take(2) },
                    label = "Número da camisa",
                    leadingIcon = Icons.Outlined.Tag,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    AppTextField(
                        value = PlayerPosition.valueOf(position).label,
                        onValueChange = {},
                        label = "Posição",
                        leadingIcon = Icons.Outlined.SportsSoccer,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        PlayerPosition.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    position = option.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, number, position) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
