package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Match
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class MatchRepository(
    private val firestore: FirebaseFirestore,
    private val currentUserId: () -> String
) {

    private val collection = firestore.collection("matches")

    fun observeBySeason(seasonId: String): Flow<List<Match>> =
        collection.whereEqualTo("seasonId", seasonId).snapshotsAsFlow()

    fun observeByRound(roundId: String): Flow<List<Match>> =
        collection.whereEqualTo("roundId", roundId).snapshotsAsFlow()

    suspend fun create(match: Match): Result<String> = runCatching {
        collection.add(match.copy(ownerId = currentUserId())).await().id
    }

    suspend fun update(match: Match): Result<Unit> = runCatching {
        collection.document(match.id)
            .update(
                mapOf(
                    "roundId" to match.roundId,
                    "homeTeamId" to match.homeTeamId,
                    "awayTeamId" to match.awayTeamId,
                    "date" to match.date,
                    "time" to match.time,
                    "place" to match.place
                )
            )
            .await()
        Unit
    }

    suspend fun registerResult(
        matchId: String,
        homeGoals: Int,
        awayGoals: Int,
        goals: Map<String, Int>,
        misses: Map<String, Int>,
        saves: Map<String, Int>
    ): Result<Unit> = runCatching {
        collection.document(matchId)
            .update(
                mapOf(
                    "homeGoals" to homeGoals,
                    "awayGoals" to awayGoals,
                    "finished" to true,
                    "goals" to goals,
                    "misses" to misses,
                    "saves" to saves
                )
            )
            .await()
        Unit
    }

    suspend fun clearResult(matchId: String): Result<Unit> = runCatching {
        collection.document(matchId)
            .update(
                mapOf(
                    "homeGoals" to null,
                    "awayGoals" to null,
                    "finished" to false,
                    "goals" to emptyMap<String, Int>(),
                    "misses" to emptyMap<String, Int>(),
                    "saves" to emptyMap<String, Int>()
                )
            )
            .await()
        Unit
    }

    suspend fun delete(matchId: String): Result<Unit> = runCatching {
        collection.document(matchId).delete().await()
        Unit
    }
}
