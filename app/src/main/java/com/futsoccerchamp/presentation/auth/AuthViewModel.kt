package com.futsoccerchamp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = FirebaseModule.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val userId: StateFlow<String?> = repository.authState()
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.currentUser?.uid)

    fun signIn(email: String, password: String) {
        val validation = validate(email = email, password = password)
        if (validation != null) {
            _uiState.value = AuthUiState(error = validation)
            return
        }
        run { repository.signIn(email, password) }
    }

    fun signOut() = repository.signOut()

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun validate(email: String, password: String): String? = when {
        email.isBlank() -> "Informe o e-mail."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "E-mail inválido."
        password.length < 6 -> "A senha deve ter ao menos 6 caracteres."
        else -> null
    }

    private fun run(block: suspend () -> Result<Unit>) {
        _uiState.value = AuthUiState(loading = true)
        viewModelScope.launch {
            val result = block()
            _uiState.value = AuthUiState(
                loading = false,
                error = result.exceptionOrNull()?.let { translate(it) }
            )
        }
    }

    private fun translate(error: Throwable): String = when {
        error.message?.contains("password is invalid", true) == true ||
            error.message?.contains("credential is incorrect", true) == true -> "E-mail ou senha incorretos."
        error.message?.contains("no user record", true) == true -> "Conta não encontrada."
        error.message?.contains("network", true) == true -> "Sem conexão com a internet."
        else -> error.message ?: "Não foi possível concluir a operação."
    }
}
