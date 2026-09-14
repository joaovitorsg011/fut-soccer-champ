package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Championship
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ChampionshipRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("championships")

    fun observeByOwner(ownerId: String): Flow<List<Championship>> =
        collection.whereEqualTo("ownerId", ownerId).snapshotsAsFlow()

    fun observeById(id: String): Flow<List<Championship>> =
        collection.whereEqualTo("__name__", id).snapshotsAsFlow()

    suspend fun get(id: String): Championship? =
        collection.document(id).get().await().toObject(Championship::class.java)

    suspend fun create(championship: Championship): Result<String> = runCatching {
        collection.add(championship.copy(createdAt = System.currentTimeMillis())).await().id
    }

    suspend fun update(championship: Championship): Result<Unit> = runCatching {
        collection.document(championship.id)
            .update(
                mapOf(
                    "name" to championship.name,
                    "season" to championship.season,
                    "teamLimit" to championship.teamLimit,
                    "description" to championship.description
                )
            )
            .await()
        Unit
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        listOf("teams", "rounds", "matches", "players").forEach { name ->
            firestore.collection(name).whereEqualTo("championshipId", id).get().await()
                .documents.forEach { it.reference.delete().await() }
        }
        collection.document(id).delete().await()
        Unit
    }
}
