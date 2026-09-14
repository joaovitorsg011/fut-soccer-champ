package com.futsoccerchamp.presentation.championship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.Championship
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Round
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.data.repository.ChampionshipRepository
import com.futsoccerchamp.data.repository.MatchRepository
import com.futsoccerchamp.data.repository.RoundRepository
import com.futsoccerchamp.data.repository.TeamRepository
import com.futsoccerchamp.domain.model.Standing
import com.futsoccerchamp.domain.usecase.CalculateStandingsUseCase
import com.futsoccerchamp.domain.usecase.GenerateRoundsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ChampionshipUiState(
    val loading: Boolean = true,
    val championship: Championship? = null,
    val teams: List<Team> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val matches: List<Match> = emptyList(),
    val standings: List<Standing> = emptyList(),
    val error: String? = null
) {
    fun team(id: String): Team? = teams.firstOrNull { it.id == id }

    fun matchesOfRound(roundId: String): List<Match> = matches.filter { it.roundId == roundId }

    val finishedMatches: List<Match> get() = matches.filter { it.finished }
}

class ChampionshipViewModel(
    private val championshipId: String,
    private val championshipRepository: ChampionshipRepository = FirebaseModule.championshipRepository,
    private val teamRepository: TeamRepository = FirebaseModule.teamRepository,
    private val roundRepository: RoundRepository = FirebaseModule.roundRepository,
    private val matchRepository: MatchRepository = FirebaseModule.matchRepository,
    private val calculateStandings: CalculateStandingsUseCase = CalculateStandingsUseCase(),
    private val generateRounds: GenerateRoundsUseCase = GenerateRoundsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChampionshipUiState())
    val uiState: StateFlow<ChampionshipUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val championship = runCatching { championshipRepository.get(championshipId) }.getOrNull()
            update { copy(loading = false, championship = championship) }
        }
        observe(teamRepository.observeByChampionship(championshipId)) { teams ->
            update { copy(teams = teams.sortedBy { it.name }) }
            recalculate()
        }
        observe(roundRepository.observeByChampionship(championshipId)) { rounds ->
            update { copy(rounds = rounds.sortedBy { it.number }) }
        }
        observe(matchRepository.observeByChampionship(championshipId)) { matches ->
            update { copy(matches = matches) }
            recalculate()
        }
    }

    fun updateChampionship(name: String, season: String, teamLimit: String, description: String) {
        val current = _uiState.value.championship ?: return
        val updated = current.copy(
            name = name.trim(),
            season = season.trim(),
            teamLimit = teamLimit.toIntOrNull() ?: 0,
            description = description.trim()
        )
        launchWithError {
            championshipRepository.update(updated).onSuccess {
                update { copy(championship = updated) }
            }
        }
    }

    fun addTeam(name: String, abbreviation: String, logoUrl: String) {
        val state = _uiState.value
        val limit = state.championship?.teamLimit ?: 0
        when {
            name.isBlank() -> return showError("Informe o nome do time.")
            limit > 0 && state.teams.size >= limit ->
                return showError("Limite de $limit times atingido.")
            state.teams.any { it.name.equals(name.trim(), ignoreCase = true) } ->
                return showError("Já existe um time com esse nome.")
        }
        launchWithError {
            teamRepository.create(
                Team(
                    championshipId = championshipId,
                    name = name.trim(),
                    abbreviation = abbreviation.trim().uppercase(),
                    logoUrl = logoUrl.trim()
                )
            )
        }
    }

    fun updateTeam(team: Team, name: String, abbreviation: String, logoUrl: String) {
        if (name.isBlank()) return showError("Informe o nome do time.")
        launchWithError {
            teamRepository.update(
                team.copy(
                    name = name.trim(),
                    abbreviation = abbreviation.trim().uppercase(),
                    logoUrl = logoUrl.trim()
                )
            )
        }
    }

    fun deleteTeam(team: Team) = launchWithError { teamRepository.delete(team) }

    fun addRound() = launchWithError {
        val number = roundRepository.nextNumber(championshipId)
        roundRepository.create(Round(championshipId = championshipId, number = number))
    }

    fun deleteRound(round: Round) = launchWithError { roundRepository.delete(round) }

    fun generateAllRounds() {
        val state = _uiState.value
        if (state.teams.size < 2) return showError("Cadastre pelo menos 2 times.")
        if (state.rounds.isNotEmpty()) return showError("Apague as rodadas existentes antes de gerar.")

        launchWithError {
            generateRounds(state.teams).forEach { generated ->
                val roundId = roundRepository
                    .create(Round(championshipId = championshipId, number = generated.number))
                    .getOrNull() ?: return@forEach
                generated.matches.forEach { pair ->
                    matchRepository.create(
                        Match(
                            championshipId = championshipId,
                            roundId = roundId,
                            homeTeamId = pair.homeTeamId,
                            awayTeamId = pair.awayTeamId
                        )
                    )
                }
            }
            Result.success(Unit)
        }
    }

    fun addMatch(roundId: String, homeTeamId: String, awayTeamId: String, date: String, time: String, place: String) {
        if (homeTeamId.isBlank() || awayTeamId.isBlank()) return showError("Selecione os dois times.")
        if (homeTeamId == awayTeamId) return showError("O time não pode jogar contra ele mesmo.")
        launchWithError {
            matchRepository.create(
                Match(
                    championshipId = championshipId,
                    roundId = roundId,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    date = date.trim(),
                    time = time.trim(),
                    place = place.trim()
                )
            )
        }
    }

    fun updateMatch(match: Match, homeTeamId: String, awayTeamId: String, date: String, time: String, place: String) {
        if (homeTeamId == awayTeamId) return showError("O time não pode jogar contra ele mesmo.")
        launchWithError {
            matchRepository.update(
                match.copy(
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    date = date.trim(),
                    time = time.trim(),
                    place = place.trim()
                )
            )
        }
    }

    fun registerResult(match: Match, homeGoals: String, awayGoals: String) {
        val home = homeGoals.toIntOrNull()
        val away = awayGoals.toIntOrNull()
        if (home == null || away == null || home < 0 || away < 0) {
            return showError("Informe um placar válido.")
        }
        launchWithError { matchRepository.registerResult(match.id, home, away) }
    }

    fun clearResult(match: Match) = launchWithError { matchRepository.clearResult(match.id) }

    fun deleteMatch(match: Match) = launchWithError { matchRepository.delete(match.id) }

    fun consumeError() = update { copy(error = null) }

    private fun <T> observe(flow: kotlinx.coroutines.flow.Flow<T>, onEach: (T) -> Unit) {
        viewModelScope.launch {
            flow.catch { error -> update { copy(loading = false, error = error.message) } }
                .collect { value -> onEach(value) }
        }
    }

    private fun recalculate() = update {
        copy(standings = calculateStandings(teams, matches))
    }

    private fun launchWithError(block: suspend () -> Result<*>) {
        viewModelScope.launch {
            block().onFailure { error -> update { copy(error = error.message) } }
        }
    }

    private fun showError(message: String) = update { copy(error = message) }

    private fun update(block: ChampionshipUiState.() -> ChampionshipUiState) {
        _uiState.value = _uiState.value.block()
    }
}
