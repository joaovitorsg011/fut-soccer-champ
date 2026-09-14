package com.futsoccerchamp.presentation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val HOME = "home"
    const val CHAMPIONSHIP = "championship/{championshipId}"

    fun championship(id: String) = "championship/$id"
}
