package com.futsoccerchamp.presentation.tournaments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.League
import com.futsoccerchamp.data.model.Tournament
import com.futsoccerchamp.data.repository.LeagueRepository
import com.futsoccerchamp.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class TournamentsUiState(
    val loading: Boolean = true,
    val league: League? = null,
    val tournaments: List<Tournament> = emptyList(),
    val error: String? = null
)

class TournamentsViewModel(
    private val leagueId: String,
    private val repository: TournamentRepository = FirebaseModule.tournamentRepository,
    private val leagueRepository: LeagueRepository = FirebaseModule.leagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentsUiState())
    val uiState: StateFlow<TournamentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val league = runCatching { leagueRepository.get(leagueId) }.getOrNull()
            _uiState.value = _uiState.value.copy(league = league)
        }
        viewModelScope.launch {
            repository.observeByLeague(leagueId)
                .catch { error -> _uiState.value = _uiState.value.copy(loading = false, error = error.message) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        tournaments = list.sortedBy { it.name }
                    )
                }
        }
    }

    fun create(name: String, description: String) {
        if (name.isBlank()) return showError("Informe o nome do torneio.")
        viewModelScope.launch {
            repository.create(
                Tournament(leagueId = leagueId, name = name.trim(), description = description.trim())
            ).onFailure { showError(it.message) }
        }
    }

    fun update(tournament: Tournament, name: String, description: String) {
        if (name.isBlank()) return showError("Informe o nome do torneio.")
        viewModelScope.launch {
            repository.update(tournament.copy(name = name.trim(), description = description.trim()))
                .onFailure { showError(it.message) }
        }
    }

    fun delete(tournament: Tournament) {
        viewModelScope.launch {
            repository.delete(tournament.id).onFailure { showError(it.message) }
        }
    }

    fun updateLeague(name: String, description: String) {
        val league = _uiState.value.league ?: return
        if (name.isBlank()) return showError("Informe o nome da liga.")
        viewModelScope.launch {
            val updated = league.copy(name = name.trim(), description = description.trim())
            leagueRepository.update(updated)
                .onSuccess { _uiState.value = _uiState.value.copy(league = updated) }
                .onFailure { showError(it.message) }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun showError(message: String?) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
