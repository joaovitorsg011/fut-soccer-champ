package com.futsoccerchamp.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futsoccerchamp.R
import com.futsoccerchamp.presentation.common.AppTextField
import com.futsoccerchamp.presentation.common.AuthBackground
import com.futsoccerchamp.presentation.common.dismissKeyboardOnTap

@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToSignUp: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthScaffold(title = "Bem-vindo de volta", subtitle = "Entre para gerenciar seus campeonatos") {
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail",
            placeholder = "voce@email.com",
            leadingIcon = Icons.Outlined.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Senha",
            imeAction = ImeAction.Done
        )

        ErrorText(state.error)

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Entrar",
            loading = state.loading,
            onClick = { viewModel.signIn(email, password) }
        )
        TextButton(onClick = onNavigateToSignUp, modifier = Modifier.fillMaxWidth()) {
            Text("Criar conta e cadastrar minha liga")
        }
    }
}

@Composable
fun SignUpScreen(viewModel: AuthViewModel, onNavigateToLogin: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var leagueName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    AuthScaffold(
        title = "Criar conta",
        subtitle = "Sua conta nasce junto com a sua liga"
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Seu nome",
            leadingIcon = Icons.Outlined.Person,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = leagueName,
            onValueChange = { leagueName = it },
            label = "Nome da liga",
            placeholder = "Fut Soccer Brasil",
            supportingText = "Ela agrupa seus torneios, times e jogadores",
            leadingIcon = Icons.Outlined.Shield,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail",
            leadingIcon = Icons.Outlined.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Senha",
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmar senha",
            imeAction = ImeAction.Done
        )

        ErrorText(state.error)

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Criar conta e liga",
            loading = state.loading,
            onClick = { viewModel.signUp(name, leagueName, email, password, confirmPassword) }
        )
        TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Já tenho conta. Entrar")
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .dismissKeyboardOnTap()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_fsb),
                contentDescription = null,
                modifier = Modifier.size(120.dp).padding(bottom = 20.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = Icons.Outlined.Lock,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar senha" else "Mostrar senha"
                )
            }
        }
    )
}

@Composable
private fun PrimaryButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(14.dp),
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorText(error: String?) {
    if (error == null) return
    Text(
        error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
    )
}
