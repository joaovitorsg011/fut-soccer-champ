package com.futsoccerchamp.presentation.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState

@Composable
fun TeamsTab(
    teams: List<Team>,
    teamLimit: Int,
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

    Box(modifier.fillMaxSize()) {
        LazyColumn(
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
                    onEdit = { teamToEdit = team },
                    onDelete = { teamToDelete = team }
                )
            }
        }
    }

    teamToEdit?.let { team ->
        TeamFormDialog(
            title = "Editar time",
            initialName = team.name,
            initialAbbreviation = team.abbreviation,
            initialLogoUrl = team.logoUrl,
            onDismiss = { teamToEdit = null },
            onConfirm = { name, abbreviation, logoUrl ->
                onEdit(team, name, abbreviation, logoUrl)
                teamToEdit = null
            }
        )
    }

    teamToDelete?.let { team ->
        ConfirmDialog(
            title = "Excluir time",
            message = "Excluir \"${team.name}\" remove também as partidas em que ele aparece.",
            onConfirm = {
                onDelete(team)
                teamToDelete = null
            },
            onDismiss = { teamToDelete = null }
        )
    }
}

@Composable
private fun TeamCard(team: Team, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamBadge(team)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(team.name, style = MaterialTheme.typography.titleMedium)
                if (team.abbreviation.isNotBlank()) {
                    Text(
                        team.abbreviation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar time") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir time") }
        }
    }
}

@Composable
fun TeamBadge(team: Team, size: Int = 40) {
    if (team.logoUrl.isNotBlank()) {
        AsyncImage(
            model = team.logoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp).clip(CircleShape)
        )
        return
    }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(size.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                team.abbreviation.take(3).ifBlank { team.name.take(1).uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun TeamFormDialog(
    title: String,
    initialName: String = "",
    initialAbbreviation: String = "",
    initialLogoUrl: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, abbreviation: String, logoUrl: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var abbreviation by remember { mutableStateOf(initialAbbreviation) }
    var logoUrl by remember { mutableStateOf(initialLogoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    label = { Text("Sigla (3 letras)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL do escudo (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, abbreviation, logoUrl) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
