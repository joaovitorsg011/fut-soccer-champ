package com.futsoccerchamp.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NameFormDialog(
    title: String,
    nameLabel: String,
    namePlaceholder: String = "",
    nameIcon: ImageVector? = null,
    initialName: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

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
                    label = nameLabel,
                    placeholder = namePlaceholder.takeIf { it.isNotBlank() },
                    leadingIcon = nameIcon
                )
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descrição",
                    placeholder = "Opcional",
                    singleLine = false
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, description) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
