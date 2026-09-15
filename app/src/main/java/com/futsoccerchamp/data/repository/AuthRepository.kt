package com.futsoccerchamp.data.repository

import com.futsoccerchamp.data.model.League
import com.futsoccerchamp.data.model.UserProfile
import com.futsoccerchamp.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun profile(uid: String): UserProfile? =
        firestore.collection("users").document(uid).get().await().toObject(UserProfile::class.java)

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        leagueName: String
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("Usuário não criado")

        firestore.collection("users").document(uid)
            .set(
                UserProfile(
                    name = name.trim(),
                    email = email.trim(),
                    role = UserRole.ADMIN.name
                )
            )
            .await()

        firestore.collection("leagues")
            .add(
                League(
                    name = leagueName.trim(),
                    ownerId = uid,
                    ownerName = name.trim(),
                    createdAt = System.currentTimeMillis()
                )
            )
            .await()
        Unit
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }

    fun signOut() = auth.signOut()
}
