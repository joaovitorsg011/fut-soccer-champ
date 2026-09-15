package com.futsoccerchamp.data.repository

import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

inline fun <reified T : Any> Query.snapshotsAsFlow(): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        trySend(snapshot?.toObjects(T::class.java).orEmpty())
    }
    awaitClose { registration.remove() }
}

fun Query.ownedBy(ownerId: String?): Query =
    if (ownerId.isNullOrBlank()) this else whereEqualTo("ownerId", ownerId)
