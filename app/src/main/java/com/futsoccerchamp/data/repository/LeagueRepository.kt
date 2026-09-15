package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.League
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class LeagueRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("leagues")

    fun observeByOwner(ownerId: String): Flow<List<League>> =
        collection.whereEqualTo("ownerId", ownerId).snapshotsAsFlow()

    fun observeAll(): Flow<List<League>> = collection.snapshotsAsFlow()

    suspend fun get(id: String): League? =
        collection.document(id).get().await().toObject(League::class.java)

    suspend fun create(league: League): Result<String> = runCatching {
        collection.add(league.copy(createdAt = System.currentTimeMillis())).await().id
    }

    suspend fun update(league: League): Result<Unit> = runCatching {
        collection.document(league.id)
            .update(mapOf("name" to league.name, "description" to league.description))
            .await()
        Unit
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        val seasons = firestore.collection("seasons").whereEqualTo("leagueId", id).get().await()
        seasons.documents.forEach { season ->
            listOf("rounds", "matches").forEach { name ->
                firestore.collection(name).whereEqualTo("seasonId", season.id).get().await()
                    .documents.forEach { it.reference.delete().await() }
            }
            season.reference.delete().await()
        }
        listOf("tournaments", "teams", "players").forEach { name ->
            firestore.collection(name).whereEqualTo("leagueId", id).get().await()
                .documents.forEach { it.reference.delete().await() }
        }
        collection.document(id).delete().await()
        Unit
    }
}
