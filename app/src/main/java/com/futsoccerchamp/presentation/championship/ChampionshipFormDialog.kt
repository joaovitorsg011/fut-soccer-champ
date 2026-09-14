package com.futsoccerchamp.presentation.championship

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.presentation.common.AppTextField
import com.futsoccerchamp.presentation.common.dismissKeyboardOnTap

@Composable
fun ChampionshipFormDialog(
    title: String,
    initialName: String = "",
    initialSeason: String = "",
    initialTeamLimit: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, season: String, teamLimit: String, description: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var season by rememberSaveable { mutableStateOf(initialSeason) }
    var teamLimit by rememberSaveable { mutableStateOf(initialTeamLimit) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.dismissKeyboardOnTap(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome",
                    placeholder = "Liga Universitária",
                    leadingIcon = Icons.Outlined.EmojiEvents
                )
                AppTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = "Temporada",
                    placeholder = "2026",
                    leadingIcon = Icons.Outlined.CalendarMonth
                )
                AppTextField(
                    value = teamLimit,
                    onValueChange = { input -> teamLimit = input.filter { it.isDigit() }.take(2) },
                    label = "Quantidade de times",
                    leadingIcon = Icons.Outlined.Groups,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descrição",
                    placeholder = "Opcional",
                    leadingIcon = Icons.Outlined.Notes,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, season, teamLimit, description) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
