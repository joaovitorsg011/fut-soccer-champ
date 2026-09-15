package com.futsoccerchamp.presentation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val LEAGUES = "leagues"
    const val LEAGUE = "league/{leagueId}"
    const val LEAGUE_TEAMS = "league/{leagueId}/teams"
    const val TOURNAMENT = "league/{leagueId}/tournament/{tournamentId}"
    const val SEASON = "league/{leagueId}/season/{seasonId}"

    fun league(leagueId: String) = "league/$leagueId"

    fun leagueTeams(leagueId: String) = "league/$leagueId/teams"

    fun tournament(leagueId: String, tournamentId: String) =
        "league/$leagueId/tournament/$tournamentId"

    fun season(leagueId: String, seasonId: String) = "league/$leagueId/season/$seasonId"
}
