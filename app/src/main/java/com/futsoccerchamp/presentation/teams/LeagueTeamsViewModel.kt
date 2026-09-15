package com.futsoccerchamp.presentation.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.PlayerPosition
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.data.repository.PlayerRepository
import com.futsoccerchamp.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class LeagueTeamsUiState(
    val loading: Boolean = true,
    val teams: List<Team> = emptyList(),
    val players: List<Player> = emptyList(),
    val error: String? = null
) {
    fun playersOf(teamId: String): List<Player> = players.filter { it.teamId == teamId }

    fun playerCountOf(teamId: String): Int = players.count { it.teamId == teamId }
}

class LeagueTeamsViewModel(
    private val leagueId: String,
    private val ownerId: String?,
    private val teamRepository: TeamRepository = FirebaseModule.teamRepository,
    private val playerRepository: PlayerRepository = FirebaseModule.playerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueTeamsUiState())
    val uiState: StateFlow<LeagueTeamsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            teamRepository.observeByLeague(leagueId, ownerId)
                .catch { error -> update { copy(loading = false, error = error.message) } }
                .collect { teams -> update { copy(loading = false, teams = teams.sortedBy { it.name }) } }
        }
        viewModelScope.launch {
            playerRepository.observeByLeague(leagueId, ownerId)
                .catch { error -> update { copy(error = error.message) } }
                .collect { players ->
                    update { copy(players = players.sortedWith(compareBy({ it.number }, { it.name }))) }
                }
        }
    }

    fun addTeam(name: String, abbreviation: String, logo: String) {
        if (name.isBlank()) return showError("Informe o nome do time.")
        if (_uiState.value.teams.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            return showError("Já existe um time com esse nome.")
        }
        launchWithError {
            teamRepository.create(
                Team(
                    leagueId = leagueId,
                    name = name.trim(),
                    abbreviation = abbreviation.trim().uppercase(),
                    logo = logo
                )
            )
        }
    }

    fun updateTeam(team: Team, name: String, abbreviation: String, logo: String) {
        if (name.isBlank()) return showError("Informe o nome do time.")
        launchWithError {
            teamRepository.update(
                team.copy(
                    name = name.trim(),
                    abbreviation = abbreviation.trim().uppercase(),
                    logo = logo
                )
            )
        }
    }

    fun deleteTeam(team: Team) = launchWithError { teamRepository.delete(team) }

    fun addPlayer(teamId: String, name: String, number: String, position: String) {
        if (name.isBlank()) return showError("Informe o nome do jogador.")
        if (_uiState.value.playersOf(teamId).any { it.name.equals(name.trim(), ignoreCase = true) }) {
            return showError("Este jogador já está no elenco.")
        }
        launchWithError {
            playerRepository.create(
                Player(
                    leagueId = leagueId,
                    teamId = teamId,
                    name = name.trim(),
                    number = number.toIntOrNull() ?: 0,
                    position = position.ifBlank { PlayerPosition.FORWARD.name }
                )
            )
        }
    }

    fun updatePlayer(player: Player, name: String, number: String, position: String) {
        if (name.isBlank()) return showError("Informe o nome do jogador.")
        launchWithError {
            playerRepository.update(
                player.copy(
                    name = name.trim(),
                    number = number.toIntOrNull() ?: 0,
                    position = position
                )
            )
        }
    }

    fun deletePlayer(player: Player) = launchWithError { playerRepository.delete(player.id) }

    fun consumeError() = update { copy(error = null) }

    private fun launchWithError(block: suspend () -> Result<*>) {
        viewModelScope.launch {
            block().onFailure { error -> update { copy(error = error.message) } }
        }
    }

    private fun showError(message: String) = update { copy(error = message) }

    private fun update(block: LeagueTeamsUiState.() -> LeagueTeamsUiState) {
        _uiState.value = _uiState.value.block()
    }
}
