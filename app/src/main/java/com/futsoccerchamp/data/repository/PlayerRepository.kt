package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Player
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class PlayerRepository(
    private val firestore: FirebaseFirestore,
    private val currentUserId: () -> String
) {

    private val collection = firestore.collection("players")

    fun observeByLeague(leagueId: String): Flow<List<Player>> =
        collection.whereEqualTo("leagueId", leagueId).snapshotsAsFlow()

    suspend fun create(player: Player): Result<String> = runCatching {
        collection.add(player.copy(ownerId = currentUserId(), createdAt = System.currentTimeMillis())).await().id
    }

    suspend fun update(player: Player): Result<Unit> = runCatching {
        collection.document(player.id)
            .update(
                mapOf(
                    "name" to player.name,
                    "number" to player.number,
                    "position" to player.position
                )
            )
            .await()
        Unit
    }

    suspend fun delete(playerId: String): Result<Unit> = runCatching {
        collection.document(playerId).delete().await()
        Unit
    }

    suspend fun deleteByTeam(teamId: String): Result<Unit> = runCatching {
        collection.whereEqualTo("teamId", teamId).get().await()
            .documents.forEach { it.reference.delete().await() }
        Unit
    }
}
