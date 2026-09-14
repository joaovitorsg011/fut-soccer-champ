package com.futsoccerchamp.data.firebase

import com.futsoccerchamp.data.repository.AuthRepository
import com.futsoccerchamp.data.repository.ChampionshipRepository
import com.futsoccerchamp.data.repository.MatchRepository
import com.futsoccerchamp.data.repository.RoundRepository
import com.futsoccerchamp.data.repository.TeamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseModule {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    val authRepository: AuthRepository by lazy { AuthRepository(auth, firestore) }
    val championshipRepository: ChampionshipRepository by lazy { ChampionshipRepository(firestore) }
    val teamRepository: TeamRepository by lazy { TeamRepository(firestore) }
    val roundRepository: RoundRepository by lazy { RoundRepository(firestore) }
    val matchRepository: MatchRepository by lazy { MatchRepository(firestore) }
}
