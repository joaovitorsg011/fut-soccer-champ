package com.futsoccerchamp.presentation.teams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.Avatar
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.common.PhotoField

@Composable
fun TeamsTab(
    teams: List<Team>,
    teamLimit: Int,
    playerCountOf: (String) -> Int,
    onOpenTeam: (Team) -> Unit,
    onEdit: (Team, String, String, String) -> Unit,
    onDelete: (Team) -> Unit,
    modifier: Modifier = Modifier
) {
    var teamToEdit by remember { mutableStateOf<Team?>(null) }
    var teamToDelete by remember { mutableStateOf<Team?>(null) }

    if (teams.isEmpty()) {
        EmptyState(
            title = "Nenhum time cadastrado",
            subtitle = "Use o botão + para adicionar os participantes do campeonato.",
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (teamLimit > 0) "${teams.size} de $teamLimit times" else "${teams.size} times",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(teams, key = { it.id }) { team ->
            TeamCard(
                team = team,
                playerCount = playerCountOf(team.id),
                onClick = { onOpenTeam(team) },
                onEdit = { teamToEdit = team },
                onDelete = { teamToDelete = team }
            )
        }
    }

    teamToEdit?.let { team ->
        TeamFormDialog(
            title = "Editar time",
            initialName = team.name,
            initialAbbreviation = team.abbreviation,
            initialLogo = team.logo,
            onDismiss = { teamToEdit = null },
            onConfirm = { name, abbreviation, logo ->
                onEdit(team, name, abbreviation, logo)
                teamToEdit = null
            }
        )
    }

    teamToDelete?.let { team ->
        ConfirmDialog(
            title = "Excluir time",
            message = "Excluir \"${team.name}\" remove também seus jogadores e as partidas em que ele aparece.",
            onConfirm = {
                onDelete(team)
                teamToDelete = null
            },
            onDismiss = { teamToDelete = null }
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    playerCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamBadge(team)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(team.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    listOfNotNull(
                        team.abbreviation.takeIf { it.isNotBlank() },
                        if (playerCount == 1) "1 jogador" else "$playerCount jogadores"
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar time") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir time") }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TeamBadge(team: Team, size: Int = 40) {
    Avatar(
        photo = team.logo,
        fallbackUrl = team.logoUrl,
        initials = team.abbreviation.ifBlank { team.name.take(1).uppercase() },
        size = size
    )
}

@Composable
fun TeamFormDialog(
    title: String,
    initialName: String = "",
    initialAbbreviation: String = "",
    initialLogo: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, abbreviation: String, logo: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var abbreviation by remember { mutableStateOf(initialAbbreviation) }
    var logo by remember { mutableStateOf(initialLogo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PhotoField(
                    photo = logo,
                    initials = abbreviation.ifBlank { name.take(1).uppercase() },
                    label = "Escudo do time",
                    onPhotoChange = { logo = it }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = abbreviation,
                    onValueChange = { if (it.length <= 3) abbreviation = it.uppercase() },
                    label = { Text("Sigla") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, abbreviation, logo) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
