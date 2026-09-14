package com.futsoccerchamp.presentation.championship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futsoccerchamp.data.firebase.FirebaseModule
import com.futsoccerchamp.data.model.Championship
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.PlayerPosition
import com.futsoccerchamp.data.model.Round
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.data.repository.ChampionshipRepository
import com.futsoccerchamp.data.repository.MatchRepository
import com.futsoccerchamp.data.repository.PlayerRepository
import com.futsoccerchamp.data.repository.RoundRepository
import com.futsoccerchamp.data.repository.TeamRepository
import com.futsoccerchamp.domain.model.PlayerRanking
import com.futsoccerchamp.domain.model.Standing
import com.futsoccerchamp.domain.usecase.CalculateRankingsUseCase
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
    val players: List<Player> = emptyList(),
    val standings: List<Standing> = emptyList(),
    val scorers: List<PlayerRanking> = emptyList(),
    val goalkeepers: List<PlayerRanking> = emptyList(),
    val error: String? = null
) {
    fun team(id: String): Team? = teams.firstOrNull { it.id == id }

    fun matchesOfRound(roundId: String): List<Match> = matches.filter { it.roundId == roundId }

    fun playersOf(teamId: String): List<Player> = players.filter { it.teamId == teamId }

    fun playerCountOf(teamId: String): Int = players.count { it.teamId == teamId }

    val drawLocked: Boolean get() = matches.any { it.finished }
}

class ChampionshipViewModel(
    private val championshipId: String,
    private val championshipRepository: ChampionshipRepository = FirebaseModule.championshipRepository,
    private val teamRepository: TeamRepository = FirebaseModule.teamRepository,
    private val roundRepository: RoundRepository = FirebaseModule.roundRepository,
    private val matchRepository: MatchRepository = FirebaseModule.matchRepository,
    private val playerRepository: PlayerRepository = FirebaseModule.playerRepository,
    private val calculateStandings: CalculateStandingsUseCase = CalculateStandingsUseCase(),
    private val calculateRankings: CalculateRankingsUseCase = CalculateRankingsUseCase(),
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
        observe(playerRepository.observeByChampionship(championshipId)) { players ->
            update { copy(players = players.sortedWith(compareBy({ it.number }, { it.name }))) }
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

    fun addTeam(name: String, abbreviation: String, logo: String) {
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

    fun addPlayer(teamId: String, name: String, number: String, position: String) {
        if (name.isBlank()) return showError("Informe o nome do jogador.")
        val state = _uiState.value
        if (state.playersOf(teamId).any { it.name.equals(name.trim(), ignoreCase = true) }) {
            return showError("Este jogador já está no elenco.")
        }
        if (position == PlayerPosition.GOALKEEPER.name &&
            state.playersOf(teamId).count { it.isGoalkeeper } >= MAX_GOALKEEPERS
        ) {
            return showError("O time já tem $MAX_GOALKEEPERS goleiros cadastrados.")
        }
        launchWithError {
            playerRepository.create(
                Player(
                    championshipId = championshipId,
                    teamId = teamId,
                    name = name.trim(),
                    number = number.toIntOrNull() ?: 0,
                    position = position
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

    fun deleteTeam(team: Team) = launchWithError { teamRepository.delete(team) }

    fun generateAllRounds() {
        val state = _uiState.value
        if (state.teams.size < 2) return showError("Cadastre pelo menos 2 times.")
        if (state.drawLocked) {
            return showError("O campeonato já começou: a tabela não pode mais ser sorteada.")
        }

        launchWithError {
            roundRepository.deleteByChampionship(championshipId)
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
        saves: Map<String, Int>
    ) = launchWithError {
        matchRepository.registerResult(match.id, homeGoals, awayGoals, goals, saves)
    }

    fun clearResult(match: Match) = launchWithError { matchRepository.clearResult(match.id) }

    fun restartCompetition() = launchWithError {
        roundRepository.deleteByChampionship(championshipId)
    }

    fun consumeError() = update { copy(error = null) }

    private fun <T> observe(flow: kotlinx.coroutines.flow.Flow<T>, onEach: (T) -> Unit) {
        viewModelScope.launch {
            flow.catch { error -> update { copy(loading = false, error = error.message) } }
                .collect { value -> onEach(value) }
        }
    }

    private fun recalculate() = update {
        copy(
            standings = calculateStandings(teams, matches),
            scorers = calculateRankings.topScorers(players, teams, matches),
            goalkeepers = calculateRankings.topGoalkeepers(players, teams, matches)
        )
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

    private companion object {
        const val MAX_GOALKEEPERS = 3
    }
}
