package com.futsoccerchamp.data.firebase

import com.futsoccerchamp.data.repository.AuthRepository
import com.futsoccerchamp.data.repository.LeagueRepository
import com.futsoccerchamp.data.repository.MatchRepository
import com.futsoccerchamp.data.repository.PlayerRepository
import com.futsoccerchamp.data.repository.RoundRepository
import com.futsoccerchamp.data.repository.SeasonRepository
import com.futsoccerchamp.data.repository.TournamentRepository
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
    val leagueRepository: LeagueRepository by lazy { LeagueRepository(firestore) }
    val tournamentRepository: TournamentRepository by lazy { TournamentRepository(firestore) { auth.currentUser?.uid.orEmpty() } }
    val seasonRepository: SeasonRepository by lazy { SeasonRepository(firestore) { auth.currentUser?.uid.orEmpty() } }
    val teamRepository: TeamRepository by lazy { TeamRepository(firestore) { auth.currentUser?.uid.orEmpty() } }
    val roundRepository: RoundRepository by lazy { RoundRepository(firestore) { auth.currentUser?.uid.orEmpty() } }
    val matchRepository: MatchRepository by lazy { MatchRepository(firestore) { auth.currentUser?.uid.orEmpty() } }
    val playerRepository: PlayerRepository by lazy { PlayerRepository(firestore) { auth.currentUser?.uid.orEmpty() } }
}
