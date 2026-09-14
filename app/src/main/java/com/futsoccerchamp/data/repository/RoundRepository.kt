package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Round
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class RoundRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("rounds")

    fun observeByChampionship(championshipId: String): Flow<List<Round>> =
        collection.whereEqualTo("championshipId", championshipId).snapshotsAsFlow()

    suspend fun nextNumber(championshipId: String): Int =
        collection.whereEqualTo("championshipId", championshipId).get().await()
            .documents.mapNotNull { it.getLong("number")?.toInt() }.maxOrNull()?.plus(1) ?: 1

    suspend fun create(round: Round): Result<String> = runCatching {
        collection.add(round).await().id
    }

    suspend fun delete(round: Round): Result<Unit> = runCatching {
        firestore.collection("matches").whereEqualTo("roundId", round.id).get().await()
            .documents.forEach { it.reference.delete().await() }
        collection.document(round.id).delete().await()
        Unit
    }

    suspend fun deleteByChampionship(championshipId: String): Result<Unit> = runCatching {
        firestore.collection("matches").whereEqualTo("championshipId", championshipId).get().await()
            .documents.forEach { it.reference.delete().await() }
        collection.whereEqualTo("championshipId", championshipId).get().await()
            .documents.forEach { it.reference.delete().await() }
        Unit
    }
}
