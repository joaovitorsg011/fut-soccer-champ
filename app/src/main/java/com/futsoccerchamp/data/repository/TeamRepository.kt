package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Team
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TeamRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("teams")

    fun observeByChampionship(championshipId: String): Flow<List<Team>> =
        collection.whereEqualTo("championshipId", championshipId).snapshotsAsFlow()

    suspend fun count(championshipId: String): Int =
        collection.whereEqualTo("championshipId", championshipId).get().await().size()

    suspend fun create(team: Team): Result<String> = runCatching {
        collection.add(team.copy(createdAt = System.currentTimeMillis())).await().id
    }

    suspend fun update(team: Team): Result<Unit> = runCatching {
        collection.document(team.id)
            .update(
                mapOf(
                    "name" to team.name,
                    "abbreviation" to team.abbreviation,
                    "logoUrl" to team.logoUrl,
                    "logo" to team.logo
                )
            )
            .await()
        Unit
    }

    suspend fun delete(team: Team): Result<Unit> = runCatching {
        firestore.collection("players").whereEqualTo("teamId", team.id).get().await()
            .documents.forEach { it.reference.delete().await() }
        val matches = firestore.collection("matches")
            .whereEqualTo("championshipId", team.championshipId).get().await()
        matches.documents
            .filter { it.getString("homeTeamId") == team.id || it.getString("awayTeamId") == team.id }
            .forEach { it.reference.delete().await() }
        collection.document(team.id).delete().await()
        Unit
    }
}
