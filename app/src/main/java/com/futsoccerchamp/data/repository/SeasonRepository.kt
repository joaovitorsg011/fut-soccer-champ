package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Season
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class SeasonRepository(
    private val firestore: FirebaseFirestore,
    private val currentUserId: () -> String
) {

    private val collection = firestore.collection("seasons")

    fun observeByTournament(tournamentId: String): Flow<List<Season>> =
        collection.whereEqualTo("tournamentId", tournamentId).snapshotsAsFlow()

    suspend fun get(id: String): Season? =
        collection.document(id).get().await().toObject(Season::class.java)

    suspend fun create(season: Season): Result<String> = runCatching {
        collection.add(season.copy(ownerId = currentUserId(), createdAt = System.currentTimeMillis())).await().id
    }

    suspend fun update(season: Season): Result<Unit> = runCatching {
        collection.document(season.id)
            .update(mapOf("label" to season.label, "teamIds" to season.teamIds))
            .await()
        Unit
    }

    suspend fun setParticipants(seasonId: String, teamIds: List<String>): Result<Unit> = runCatching {
        collection.document(seasonId).update("teamIds", teamIds).await()
        Unit
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        listOf("rounds", "matches").forEach { name ->
            firestore.collection(name).whereEqualTo("seasonId", id).get().await()
                .documents.forEach { it.reference.delete().await() }
        }
        collection.document(id).delete().await()
        Unit
    }
}
