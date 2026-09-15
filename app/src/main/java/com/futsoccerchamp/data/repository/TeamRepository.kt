package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.Team
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TeamRepository(
    private val firestore: FirebaseFirestore,
    private val currentUserId: () -> String
) {

    private val collection = firestore.collection("teams")

    fun observeByLeague(leagueId: String, ownerId: String?): Flow<List<Team>> =
        collection.whereEqualTo("leagueId", leagueId).ownedBy(ownerId).snapshotsAsFlow()

    suspend fun create(team: Team): Result<String> = runCatching {
        collection.add(team.copy(ownerId = currentUserId(), createdAt = System.currentTimeMillis())).await().id
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
        listOf("homeTeamId", "awayTeamId").forEach { field ->
            firestore.collection("matches").whereEqualTo(field, team.id).get().await()
                .documents.forEach { it.reference.delete().await() }
        }
        firestore.collection("seasons").whereArrayContains("teamIds", team.id).get().await()
            .documents.forEach { season ->
                val remaining = (season.get("teamIds") as? List<*>).orEmpty().filterNot { it == team.id }
                season.reference.update("teamIds", remaining).await()
            }
        collection.document(team.id).delete().await()
        Unit
    }
}
