package com.futsoccerchamp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.Championship
import com.futsoccerchamp.data.repository.ChampionshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val championships: List<Championship> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val ownerId: String,
    private val repository: ChampionshipRepository = FirebaseModule.championshipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeByOwner(ownerId)
                .catch { error -> _uiState.value = HomeUiState(loading = false, error = error.message) }
                .collect { list ->
                    _uiState.value = HomeUiState(
                        loading = false,
                        championships = list.sortedByDescending { it.createdAt }
                    )
                }
        }
    }

    fun create(name: String, season: String, teamLimit: String, description: String) {
        if (name.isBlank() || season.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Nome e temporada são obrigatórios.")
            return
        }
        viewModelScope.launch {
            repository.create(
                Championship(
                    name = name.trim(),
                    season = season.trim(),
                    teamLimit = teamLimit.toIntOrNull() ?: 0,
                    description = description.trim(),
                    ownerId = ownerId
                )
            ).onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun delete(championship: Championship) {
        viewModelScope.launch {
            repository.delete(championship.id)
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
