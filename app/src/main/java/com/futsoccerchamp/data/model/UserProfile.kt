package com.futsoccerchamp.data.model

import com.google.firebase.firestore.DocumentId

enum class UserRole { ROOT, ADMIN }

data class UserProfile(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = UserRole.ADMIN.name
) {
    val isRoot: Boolean get() = role == UserRole.ROOT.name
}
