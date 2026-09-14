package com.futsoccerchamp.presentation.rounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun RoundsTab(
    rounds: List<Round>,
    teams: List<Team>,
    matchesOf: (String) -> List<Match>,
    teamOf: (String) -> Team?,
    playersOf: (String) -> List<Player>,
    onAddMatch: (roundId: String, homeTeamId: String, awayTeamId: String, date: String, time: String, place: String) -> Unit,
    onEditMatch: (Match, String, String, String, String, String) -> Unit,
    onRegisterResult: (Match, String, String, Map<String, Int>, Map<String, Int>) -> Unit,
    onClearResult: (Match) -> Unit,
    onDeleteMatch: (Match) -> Unit,
    onDeleteRound: (Round) -> Unit,
    onGenerateRounds: () -> Unit,
    modifier: Modifier = Modifier
) {
    var roundForNewMatch by remember { mutableStateOf<Round?>(null) }
    var matchToEdit by remember { mutableStateOf<Match?>(null) }
    var matchForResult by remember { mutableStateOf<Match?>(null) }
    var matchToDelete by remember { mutableStateOf<Match?>(null) }
    var roundToDelete by remember { mutableStateOf<Round?>(null) }

    if (rounds.isEmpty()) {
        Column(modifier) {
            EmptyState(
                title = "Nenhuma rodada criada",
                subtitle = "Use o botão + para criar uma rodada, ou gere o calendário completo a partir dos times.",
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onGenerateRounds,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Gerar rodadas automaticamente")
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rounds, key = { it.id }) { round ->
            RoundCard(
                round = round,
                matches = matchesOf(round.id),
                teamOf = teamOf,
                onAddMatch = { roundForNewMatch = round },
                onDeleteRound = { roundToDelete = round },
                onEditMatch = { matchToEdit = it },
                onRegisterResult = { matchForResult = it },
                onClearResult = onClearResult,
                onDeleteMatch = { matchToDelete = it }
            )
        }
    }

    roundForNewMatch?.let { round ->
        MatchFormDialog(
            title = "Nova partida — Rodada ${round.number}",
            teams = teams,
            onDismiss = { roundForNewMatch = null },
            onConfirm = { home, away, date, time, place ->
                onAddMatch(round.id, home, away, date, time, place)
                roundForNewMatch = null
            }
        )
    }

    matchToEdit?.let { match ->
        MatchFormDialog(
            title = "Editar partida",
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

    matchForResult?.let { match ->
        MatchResultDialog(
            match = match,
            homeTeam = teamOf(match.homeTeamId),
            awayTeam = teamOf(match.awayTeamId),
            homePlayers = playersOf(match.homeTeamId),
            awayPlayers = playersOf(match.awayTeamId),
            onDismiss = { matchForResult = null },
            onConfirm = { home, away, goals, saves ->
                onRegisterResult(match, home, away, goals, saves)
                matchForResult = null
            }
        )
    }

    matchToDelete?.let { match ->
        ConfirmDialog(
            title = "Excluir partida",
            message = "Esta partida será removida do campeonato.",
            onConfirm = {
                onDeleteMatch(match)
                matchToDelete = null
            },
            onDismiss = { matchToDelete = null }
        )
    }

    roundToDelete?.let { round ->
        ConfirmDialog(
            title = "Excluir rodada ${round.number}",
            message = "Todas as partidas desta rodada serão removidas.",
            onConfirm = {
                onDeleteRound(round)
                roundToDelete = null
            },
            onDismiss = { roundToDelete = null }
        )
    }
}

@Composable
private fun RoundCard(
    round: Round,
    matches: List<Match>,
    teamOf: (String) -> Team?,
    onAddMatch: () -> Unit,
    onDeleteRound: () -> Unit,
    onEditMatch: (Match) -> Unit,
    onRegisterResult: (Match) -> Unit,
    onClearResult: (Match) -> Unit,
    onDeleteMatch: (Match) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Rodada ${round.number}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAddMatch) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar partida")
            }
            IconButton(onClick = onDeleteRound) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir rodada")
            }
        }

        if (matches.isEmpty()) {
            Text(
                "Nenhuma partida nesta rodada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
            return@Card
        }

        matches.forEachIndexed { index, match ->
            if (index > 0) HorizontalDivider()
            MatchRow(
                match = match,
                home = teamOf(match.homeTeamId),
                away = teamOf(match.awayTeamId),
                onEdit = { onEditMatch(match) },
                onRegisterResult = { onRegisterResult(match) },
                onClearResult = { onClearResult(match) },
                onDelete = { onDeleteMatch(match) }
            )
        }
    }
}

@Composable
private fun MatchRow(
    match: Match,
    home: Team?,
    away: Team?,
    onEdit: () -> Unit,
    onRegisterResult: () -> Unit,
    onClearResult: () -> Unit,
    onDelete: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            home?.let { TeamBadge(it, size = 28) }
            Text(
                home?.name ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Text(
                if (match.finished) "${match.homeGoals} x ${match.awayGoals}" else "x",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                away?.name ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            away?.let { TeamBadge(it, size = 28) }
        }

        val info = listOfNotNull(
            match.date.takeIf { it.isNotBlank() },
            match.time.takeIf { it.isNotBlank() },
            match.place.takeIf { it.isNotBlank() }
        ).joinToString(" · ")

        if (info.isNotBlank()) {
            Text(
                info,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onRegisterResult) {
                Icon(Icons.Default.Scoreboard, contentDescription = "Registrar resultado")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar partida")
            }
            if (match.finished) {
                TextButton(onClick = onClearResult) { Text("Limpar placar") }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir partida")
            }
        }
    }
}
