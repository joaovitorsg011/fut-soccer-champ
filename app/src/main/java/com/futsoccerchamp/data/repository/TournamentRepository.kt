package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Tournament
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TournamentRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("tournaments")

    fun observeByLeague(leagueId: String): Flow<List<Tournament>> =
        collection.whereEqualTo("leagueId", leagueId).snapshotsAsFlow()

    suspend fun get(id: String): Tournament? =
        collection.document(id).get().await().toObject(Tournament::class.java)

    suspend fun create(tournament: Tournament): Result<String> = runCatching {
        collection.add(tournament.copy(createdAt = System.currentTimeMillis())).await().id
    }

    suspend fun update(tournament: Tournament): Result<Unit> = runCatching {
        collection.document(tournament.id)
            .update(mapOf("name" to tournament.name, "description" to tournament.description))
            .await()
        Unit
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        val seasons = firestore.collection("seasons").whereEqualTo("tournamentId", id).get().await()
        seasons.documents.forEach { season ->
            listOf("rounds", "matches").forEach { name ->
                firestore.collection(name).whereEqualTo("seasonId", season.id).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }
            season.reference.delete().await()
        }
        collection.document(id).delete().await()
        Unit
    }
}
