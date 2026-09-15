package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Round
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class RoundRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("rounds")

    fun observeBySeason(seasonId: String): Flow<List<Round>> =
        collection.whereEqualTo("seasonId", seasonId).snapshotsAsFlow()

    suspend fun create(round: Round): Result<String> = runCatching {
        collection.add(round).await().id
    }

    suspend fun delete(round: Round): Result<Unit> = runCatching {
        firestore.collection("matches").whereEqualTo("roundId", round.id).get().await()
            .documents.forEach { it.reference.delete().await() }
        collection.document(round.id).delete().await()
        Unit
    }

    suspend fun deleteBySeason(seasonId: String): Result<Unit> = runCatching {
        firestore.collection("matches").whereEqualTo("seasonId", seasonId).get().await()
            .documents.forEach { it.reference.delete().await() }
        collection.whereEqualTo("seasonId", seasonId).get().await()
            .documents.forEach { it.reference.delete().await() }
        Unit
    }
}
