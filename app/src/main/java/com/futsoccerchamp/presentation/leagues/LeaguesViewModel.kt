package com.futsoccerchamp.presentation.leagues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.League
import com.futsoccerchamp.data.repository.LeagueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class LeaguesUiState(
    val loading: Boolean = true,
    val leagues: List<League> = emptyList(),
    val error: String? = null
)

class LeaguesViewModel(
    private val ownerId: String,
    private val readOnly: Boolean,
    private val repository: LeagueRepository = FirebaseModule.leagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaguesUiState())
    val uiState: StateFlow<LeaguesUiState> = _uiState.asStateFlow()

    init {
        val source = if (readOnly) repository.observeAll() else repository.observeByOwner(ownerId)
        viewModelScope.launch {
            source
                .catch { error -> _uiState.value = LeaguesUiState(loading = false, error = error.message) }
                .collect { list ->
                    _uiState.value = LeaguesUiState(
                        loading = false,
                        leagues = list.sortedByDescending { it.createdAt }
                    )
                }
        }
    }

    fun rename(league: League, name: String, description: String) {
        if (name.isBlank()) return showError("Informe o nome da liga.")
        viewModelScope.launch {
            repository.update(league.copy(name = name.trim(), description = description.trim()))
                .onFailure { showError(it.message) }
        }
    }

    fun delete(league: League) {
        viewModelScope.launch {
            repository.delete(league.id).onFailure { showError(it.message) }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun showError(message: String?) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
