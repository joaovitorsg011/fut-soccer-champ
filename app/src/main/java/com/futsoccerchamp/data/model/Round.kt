package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

data class Round(
    @DocumentId val id: String = "",
    val seasonId: String = "",
    val number: Int = 0
)
