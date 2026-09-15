package com.futsoccerchamp.presentation.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.PlayerPosition
import com.futsoccerchamp.data.model.Round
import com.futsoccerchamp.data.model.Season
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.data.repository.MatchRepository
import com.futsoccerchamp.data.repository.PlayerRepository
import com.futsoccerchamp.data.repository.RoundRepository
import com.futsoccerchamp.data.repository.SeasonRepository
import com.futsoccerchamp.data.repository.TeamRepository
import com.futsoccerchamp.domain.model.PlayerRanking
import com.futsoccerchamp.domain.model.Standing
import com.futsoccerchamp.domain.usecase.CalculateRankingsUseCase
import com.futsoccerchamp.domain.usecase.CalculateStandingsUseCase
import com.futsoccerchamp.domain.usecase.GenerateRoundsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class SeasonUiState(
    val loading: Boolean = true,
    val season: Season? = null,
    val leagueTeams: List<Team> = emptyList(),
    val teams: List<Team> = emptyList(),
    val players: List<Player> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val matches: List<Match> = emptyList(),
    val standings: List<Standing> = emptyList(),
    val scorers: List<PlayerRanking> = emptyList(),
    val goalkeepers: List<PlayerRanking> = emptyList(),
    val missers: List<PlayerRanking> = emptyList(),
    val error: String? = null
) {
    fun team(id: String): Team? = teams.firstOrNull { it.id == id }

    fun matchesOfRound(roundId: String): List<Match> = matches.filter { it.roundId == roundId }

    fun playersOf(teamId: String): List<Player> = players.filter { it.teamId == teamId }

    fun standingOf(teamId: String): Standing? = standings.firstOrNull { it.teamId == teamId }

    fun positionOf(teamId: String): Int = standings.indexOfFirst { it.teamId == teamId } + 1

    fun playerTotals(playerId: String): Triple<Int, Int, Int> {
        val finished = matches.filter { it.finished }
        return Triple(
            finished.sumOf { it.goals[playerId] ?: 0 },
            finished.sumOf { it.saves[playerId] ?: 0 },
            finished.sumOf { it.misses[playerId] ?: 0 }
        )
    }

    val drawLocked: Boolean get() = matches.any { it.finished }
}

class SeasonViewModel(
    private val leagueId: String,
    private val seasonId: String,
    private val ownerId: String?,
    private val seasonRepository: SeasonRepository = FirebaseModule.seasonRepository,
    private val teamRepository: TeamRepository = FirebaseModule.teamRepository,
    private val playerRepository: PlayerRepository = FirebaseModule.playerRepository,
    private val roundRepository: RoundRepository = FirebaseModule.roundRepository,
    private val matchRepository: MatchRepository = FirebaseModule.matchRepository,
    private val calculateStandings: CalculateStandingsUseCase = CalculateStandingsUseCase(),
    private val calculateRankings: CalculateRankingsUseCase = CalculateRankingsUseCase(),
    private val generateRounds: GenerateRoundsUseCase = GenerateRoundsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeasonUiState())
    val uiState: StateFlow<SeasonUiState> = _uiState.asStateFlow()

    private var allTeams: List<Team> = emptyList()
    private var allPlayers: List<Player> = emptyList()

    init {
        viewModelScope.launch {
            val season = runCatching { seasonRepository.get(seasonId) }.getOrNull()
            update { copy(loading = false, season = season) }
            applyParticipants()
        }
        observe(teamRepository.observeByLeague(leagueId, ownerId)) { teams ->
            allTeams = teams.sortedBy { it.name }
            applyParticipants()
        }
        observe(playerRepository.observeByLeague(leagueId, ownerId)) { players ->
            allPlayers = players.sortedWith(compareBy({ it.number }, { it.name }))
            applyParticipants()
        }
        observe(roundRepository.observeBySeason(seasonId, ownerId)) { rounds ->
            update { copy(rounds = rounds.sortedBy { it.number }) }
        }
        observe(matchRepository.observeBySeason(seasonId, ownerId)) { matches ->
            update { copy(matches = matches) }
            recalculate()
        }
    }

    fun generateAllRounds() {
        val state = _uiState.value
        if (state.teams.size < 2) return showError("A temporada precisa de ao menos dois times.")
        if (state.drawLocked) {
            return showError("A temporada já começou: a tabela não pode mais ser sorteada.")
        }

        launchWithError {
            roundRepository.deleteBySeason(seasonId)
            generateRounds(state.teams).forEach { generated ->
                val roundId = roundRepository
                    .create(Round(seasonId = seasonId, number = generated.number))
                    .getOrNull() ?: return@forEach
                generated.matches.forEach { pair ->
                    matchRepository.create(
                        Match(
                            seasonId = seasonId,
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

    fun registerResult(
        match: Match,
        homeGoals: Int,
        awayGoals: Int,
        goals: Map<String, Int>,
        misses: Map<String, Int>,
        saves: Map<String, Int>
    ) = launchWithError {
        matchRepository.registerResult(match.id, homeGoals, awayGoals, goals, misses, saves)
    }

    fun clearResult(match: Match) = launchWithError { matchRepository.clearResult(match.id) }

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

    fun restartSeason() = launchWithError { roundRepository.deleteBySeason(seasonId) }

    fun updateParticipants(teamIds: List<String>) {
        val season = _uiState.value.season ?: return
        if (_uiState.value.matches.isNotEmpty()) {
            return showError("Reinicie a temporada antes de alterar os participantes.")
        }
        launchWithError {
            seasonRepository.setParticipants(seasonId, teamIds).onSuccess {
                update { copy(season = season.copy(teamIds = teamIds)) }
                applyParticipants()
            }
        }
    }

    fun consumeError() = update { copy(error = null) }

    private fun applyParticipants() {
        val participants = _uiState.value.season?.teamIds.orEmpty()
        val teams = if (participants.isEmpty()) {
            emptyList()
        } else {
            allTeams.filter { it.id in participants }
        }
        val teamIds = teams.map { it.id }.toSet()
        update {
            copy(
                leagueTeams = allTeams,
                teams = teams,
                players = allPlayers.filter { it.teamId in teamIds }
            )
        }
        recalculate()
    }

    private fun <T> observe(flow: Flow<T>, onEach: (T) -> Unit) {
        viewModelScope.launch {
            flow.catch { error -> update { copy(loading = false, error = error.message) } }
                .collect { value -> onEach(value) }
        }
    }

    private fun recalculate() = update {
        copy(
            standings = calculateStandings(teams, matches),
            scorers = calculateRankings.topScorers(players, teams, matches),
            goalkeepers = calculateRankings.topGoalkeepers(players, teams, matches),
            missers = calculateRankings.mostMisses(players, teams, matches)
        )
    }

    private fun launchWithError(block: suspend () -> Result<*>) {
        viewModelScope.launch {
            block().onFailure { error -> update { copy(error = error.message) } }
        }
    }

    private fun showError(message: String) = update { copy(error = message) }

    private fun update(block: SeasonUiState.() -> SeasonUiState) {
        _uiState.value = _uiState.value.block()
    }
}
