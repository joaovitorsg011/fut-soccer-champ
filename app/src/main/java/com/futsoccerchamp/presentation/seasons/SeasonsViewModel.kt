package com.futsoccerchamp.presentation.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.Season
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.data.model.Tournament
import com.futsoccerchamp.data.repository.SeasonRepository
import com.futsoccerchamp.data.repository.TeamRepository
import com.futsoccerchamp.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class SeasonsUiState(
    val loading: Boolean = true,
    val tournament: Tournament? = null,
    val removed: Boolean = false,
    val seasons: List<Season> = emptyList(),
    val teams: List<Team> = emptyList(),
    val error: String? = null
)

class SeasonsViewModel(
    private val leagueId: String,
    private val tournamentId: String,
    private val ownerId: String?,
    private val repository: SeasonRepository = FirebaseModule.seasonRepository,
    private val tournamentRepository: TournamentRepository = FirebaseModule.tournamentRepository,
    private val teamRepository: TeamRepository = FirebaseModule.teamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeasonsUiState())
    val uiState: StateFlow<SeasonsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tournamentRepository.observeById(tournamentId)
                .catch { error -> _uiState.value = _uiState.value.copy(error = error.message) }
                .collect { tournament ->
                    _uiState.value = _uiState.value.copy(
                        tournament = tournament,
                        removed = tournament == null
                    )
                }
        }
        viewModelScope.launch {
            repository.observeByTournament(tournamentId, ownerId)
                .catch { error -> _uiState.value = _uiState.value.copy(loading = false, error = error.message) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        seasons = list.sortedByDescending { it.label }
                    )
                }
        }
        viewModelScope.launch {
            teamRepository.observeByLeague(leagueId, ownerId)
                .catch { error -> _uiState.value = _uiState.value.copy(error = error.message) }
                .collect { teams -> _uiState.value = _uiState.value.copy(teams = teams.sortedBy { it.name }) }
        }
    }

    fun create(label: String, teamIds: List<String>) {
        if (label.isBlank()) return showError("Informe o nome da temporada.")
        viewModelScope.launch {
            repository.create(
                Season(
                    leagueId = leagueId,
                    tournamentId = tournamentId,
                    label = label.trim(),
                    teamIds = teamIds
                )
            ).onFailure { showError(it.message) }
        }
    }

    fun update(season: Season, label: String, teamIds: List<String>) {
        if (label.isBlank()) return showError("Informe o nome da temporada.")
        viewModelScope.launch {
            repository.update(season.copy(label = label.trim(), teamIds = teamIds))
                .onFailure { showError(it.message) }
        }
    }

    fun delete(season: Season) {
        viewModelScope.launch {
            repository.delete(season.id).onFailure { showError(it.message) }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun showError(message: String?) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
