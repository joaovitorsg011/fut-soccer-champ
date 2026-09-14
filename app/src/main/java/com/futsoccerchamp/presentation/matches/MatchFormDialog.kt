package com.futsoccerchamp.presentation.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.AppTextField
import com.futsoccerchamp.presentation.common.dismissKeyboardOnTap

@Composable
fun MatchFormDialog(
    title: String,
    teams: List<Team>,
    initialHomeTeamId: String = "",
    initialAwayTeamId: String = "",
    initialDate: String = "",
    initialTime: String = "",
    initialPlace: String = "",
    onDismiss: () -> Unit,
    onConfirm: (homeTeamId: String, awayTeamId: String, date: String, time: String, place: String) -> Unit
) {
    var homeTeamId by remember { mutableStateOf(initialHomeTeamId) }
    var awayTeamId by remember { mutableStateOf(initialAwayTeamId) }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var place by remember { mutableStateOf(initialPlace) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.dismissKeyboardOnTap(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeamPicker("Mandante", teams, homeTeamId) { homeTeamId = it }
                TeamPicker("Visitante", teams, awayTeamId) { awayTeamId = it }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = "Data",
                        placeholder = "20/09/2026",
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = "Hora",
                        placeholder = "16:00",
                        modifier = Modifier.weight(1f)
                    )
                }
                AppTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = "Local",
                    placeholder = "Opcional",
                    leadingIcon = Icons.Outlined.Place
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(homeTeamId, awayTeamId, date, time, place) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamPicker(
    label: String,
    teams: List<Team>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = teams.firstOrNull { it.id == selectedId }?.name.orEmpty()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        AppTextField(
            value = selectedName,
            onValueChange = {},
            label = label,
            leadingIcon = Icons.Outlined.Shield,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            teams.forEach { team ->
                DropdownMenuItem(
                    text = { Text(team.name) },
                    onClick = {
                        onSelect(team.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
