package com.futsoccerchamp.presentation.rounds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.futsoccerchamp.data.model.Match
import com.futsoccerchamp.data.model.Player
import com.futsoccerchamp.data.model.Round
import com.futsoccerchamp.data.model.Team
import com.futsoccerchamp.presentation.common.ConfirmDialog
import com.futsoccerchamp.presentation.common.EmptyState
import com.futsoccerchamp.presentation.matches.MatchFormDialog
import com.futsoccerchamp.presentation.matches.MatchResultDialog
import com.futsoccerchamp.presentation.teams.TeamBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoundsTab(
    rounds: List<Round>,
    teams: List<Team>,
    matchesOf: (String) -> List<Match>,
    teamOf: (String) -> Team?,
    playersOf: (String) -> List<Player>,
    drawLocked: Boolean,
    onEditMatch: (Match, String, String, String, String, String) -> Unit,
    onRegisterResult: (Match, String, String, Map<String, Int>, Map<String, Int>) -> Unit,
    onClearResult: (Match) -> Unit,
    onGenerateRounds: () -> Unit,
    modifier: Modifier = Modifier
) {
    var matchForResult by remember { mutableStateOf<Match?>(null) }
    var matchToEdit by remember { mutableStateOf<Match?>(null) }
    var confirmGenerate by remember { mutableStateOf(false) }

    if (rounds.isEmpty()) {
        DrawPrompt(
            enoughTeams = teams.size >= 2,
            onGenerate = onGenerateRounds,
            modifier = modifier
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { rounds.size })
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 12.dp
        ) {
            rounds.forEachIndexed { index, round ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text("Rodada ${round.number}") }
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val round = rounds[page]
            val matches = matchesOf(round.id)

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { RoundSummary(round = round, matches = matches) }

                items(matches, key = { it.id }) { match ->
                    MatchScoreCard(
                        match = match,
                        home = teamOf(match.homeTeamId),
                        away = teamOf(match.awayTeamId),
                        onOpenResult = { matchForResult = match },
                        onOpenDetails = { matchToEdit = match }
                    )
                }

                if (!drawLocked) {
                    item {
                        OutlinedButton(
                            onClick = { confirmGenerate = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null)
                            Text("Sortear tabela novamente", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                } else {
                    item { DrawLockedNotice() }
                }
            }
        }
    }

    matchForResult?.let { match ->
        MatchResultDialog(
            match = match,
            homeTeam = teamOf(match.homeTeamId),
            awayTeam = teamOf(match.awayTeamId),
            homePlayers = playersOf(match.homeTeamId),
            awayPlayers = playersOf(match.awayTeamId),
            onClearResult = { onClearResult(match) },
            onDismiss = { matchForResult = null },
            onConfirm = { home, away, goals, saves ->
                onRegisterResult(match, home, away, goals, saves)
                matchForResult = null
            }
        )
    }

    matchToEdit?.let { match ->
        MatchFormDialog(
            title = "Detalhes da partida",
            teams = teams,
            initialHomeTeamId = match.homeTeamId,
            initialAwayTeamId = match.awayTeamId,
            initialDate = match.date,
            initialTime = match.time,
            initialPlace = match.place,
            onDismiss = { matchToEdit = null },
            onConfirm = { home, away, date, time, place ->
                onEditMatch(match, home, away, date, time, place)
                matchToEdit = null
            }
        )
    }

    if (confirmGenerate) {
        ConfirmDialog(
            title = "Sortear tabela novamente",
            message = "As rodadas atuais serão substituídas por um novo sorteio com os times cadastrados.",
            confirmLabel = "Sortear",
            onConfirm = {
                onGenerateRounds()
                confirmGenerate = false
            },
            onDismiss = { confirmGenerate = false }
        )
    }
}

@Composable
private fun DrawPrompt(
    enoughTeams: Boolean,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        EmptyState(
            title = "Tabela ainda não sorteada",
            subtitle = if (enoughTeams) {
                "O sorteio monta todas as rodadas do turno, definindo os confrontos e o mando de campo."
            } else {
                "Cadastre ao menos dois times para sortear a tabela."
            },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onGenerate,
            enabled = enoughTeams,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Icon(Icons.Default.Casino, contentDescription = null)
            Text("Sortear tabela", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun RoundSummary(round: Round, matches: List<Match>) {
    val played = matches.count { it.finished }

    Text(
        "Rodada ${round.number} · $played de ${matches.size} partidas encerradas",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchScoreCard(
    match: Match,
    home: Team?,
    away: Team?,
    onOpenResult: () -> Unit,
    onOpenDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpenResult, onLongClick = onOpenDetails)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamSide(team = home, modifier = Modifier.weight(1f))
            Score(match = match)
            TeamSide(team = away, modifier = Modifier.weight(1f))
        }

        val info = listOfNotNull(
            match.date.takeIf { it.isNotBlank() },
            match.time.takeIf { it.isNotBlank() },
            match.place.takeIf { it.isNotBlank() }
        ).joinToString(" · ")

        if (info.isNotBlank()) {
            Text(
                info,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun TeamSide(team: Team?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        team?.let { TeamBadge(it, size = 52) } ?: Box(Modifier.size(52.dp))
        Text(
            team?.name.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun Score(match: Match) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (match.finished) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = if (match.finished) "${match.homeGoals}  ${match.awayGoals}" else "0  0",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (match.finished) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun DrawLockedNotice() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            "Campeonato em andamento: a tabela não pode mais ser sorteada.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
