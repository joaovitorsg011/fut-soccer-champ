package com.futsoccerchamp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.UserProfile
import com.futsoccerchamp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

data class SessionState(
    val resolved: Boolean = false,
    val userId: String? = null,
    val profile: UserProfile? = null
) {
    val isRoot: Boolean get() = profile?.isRoot == true
}

class AuthViewModel(
    private val repository: AuthRepository = FirebaseModule.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _session = MutableStateFlow(SessionState())
    val session: StateFlow<SessionState> = _session.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authState().collect { user ->
                val profile = user?.uid?.let { uid ->
                    runCatching { repository.profile(uid) }.getOrNull()
                }
                _session.value = SessionState(resolved = true, userId = user?.uid, profile = profile)
            }
        }
    }

    fun signIn(email: String, password: String) {
        val validation = validate(email = email, password = password)
        if (validation != null) {
            _uiState.value = AuthUiState(error = validation)
            return
        }
        run { repository.signIn(email, password) }
    }

    fun signUp(name: String, leagueName: String, email: String, password: String, confirmPassword: String) {
        val validation = when {
            name.isBlank() -> "Informe seu nome."
            leagueName.isBlank() -> "Informe o nome da liga."
            password != confirmPassword -> "As senhas não coincidem."
            else -> validate(email = email, password = password)
        }
        if (validation != null) {
            _uiState.value = AuthUiState(error = validation)
            return
        }
        run { repository.signUp(name, email, password, leagueName) }
    }

    fun signOut() = repository.signOut()

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
        error.message?.contains("already in use", true) == true -> "Este e-mail já está cadastrado."
        error.message?.contains("network", true) == true -> "Sem conexão com a internet."
        else -> error.message ?: "Não foi possível concluir a operação."
    }
}
