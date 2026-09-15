package com.futsoccerchamp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.futsoccerchamp.presentation.auth.AuthViewModel
import com.futsoccerchamp.presentation.auth.LoginScreen
import com.futsoccerchamp.presentation.auth.SignUpScreen
import com.futsoccerchamp.presentation.leagues.LeaguesScreen
import com.futsoccerchamp.presentation.leagues.LeaguesViewModel
import com.futsoccerchamp.presentation.seasons.SeasonScreen
import com.futsoccerchamp.presentation.seasons.SeasonViewModel
import com.futsoccerchamp.presentation.seasons.SeasonsScreen
import com.futsoccerchamp.presentation.seasons.SeasonsViewModel
import com.futsoccerchamp.presentation.splash.SplashScreen
import com.futsoccerchamp.presentation.teams.LeagueTeamsScreen
import com.futsoccerchamp.presentation.teams.LeagueTeamsViewModel
import com.futsoccerchamp.presentation.theme.ThemeMode
import com.futsoccerchamp.presentation.tournaments.LeagueScreen
import com.futsoccerchamp.presentation.tournaments.TournamentsViewModel

@Composable
fun AppNavigation(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val session by authViewModel.session.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.replaceWith(
                        if (session.userId == null) Routes.LOGIN else Routes.LEAGUES
                    )
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) }
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.LEAGUES) {
            val userId = session.userId
            if (userId != null) {
                val leaguesViewModel: LeaguesViewModel = viewModel(
                    key = "leagues-$userId-${session.isRoot}",
                    factory = factory { LeaguesViewModel(userId, session.isRoot) }
                )
                val leaguesState by leaguesViewModel.uiState.collectAsStateWithLifecycle()
                val ownLeagueId = leaguesState.leagues.firstOrNull()?.id

                LaunchedEffect(ownLeagueId, session.isRoot) {
                    if (!session.isRoot && ownLeagueId != null) {
                        navController.replaceWith(Routes.league(ownLeagueId))
                    }
                }

                LeaguesScreen(
                    viewModel = leaguesViewModel,
                    readOnly = session.isRoot,
                    showList = session.isRoot || ownLeagueId == null,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onOpenLeague = { navController.navigate(Routes.league(it)) },
                    onSignOut = { authViewModel.signOut() }
                )
            }
        }

        composable(
            route = Routes.LEAGUE,
            arguments = listOf(navArgument("leagueId") { type = NavType.StringType })
        ) { entry ->
            val leagueId = entry.arg("leagueId")
            val tournamentsViewModel: TournamentsViewModel = viewModel(
                key = "tournaments-$leagueId",
                factory = factory { TournamentsViewModel(leagueId) }
            )
            LeagueScreen(
                viewModel = tournamentsViewModel,
                readOnly = session.isRoot,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onOpenTournament = { navController.navigate(Routes.tournament(leagueId, it)) },
                onOpenTeams = { navController.navigate(Routes.leagueTeams(leagueId)) },
                showSwitchLeague = session.isRoot,
                onSwitchLeague = { navController.replaceWith(Routes.LEAGUES) },
                onSignOut = { authViewModel.signOut() }
            )
        }

        composable(
            route = Routes.LEAGUE_TEAMS,
            arguments = listOf(navArgument("leagueId") { type = NavType.StringType })
        ) { entry ->
            val leagueId = entry.arg("leagueId")
            val teamsViewModel: LeagueTeamsViewModel = viewModel(
                key = "league-teams-$leagueId",
                factory = factory { LeagueTeamsViewModel(leagueId) }
            )
            LeagueTeamsScreen(
                viewModel = teamsViewModel,
                readOnly = session.isRoot,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TOURNAMENT,
            arguments = listOf(
                navArgument("leagueId") { type = NavType.StringType },
                navArgument("tournamentId") { type = NavType.StringType }
            )
        ) { entry ->
            val leagueId = entry.arg("leagueId")
            val tournamentId = entry.arg("tournamentId")
            val seasonsViewModel: SeasonsViewModel = viewModel(
                key = "seasons-$tournamentId",
                factory = factory { SeasonsViewModel(leagueId, tournamentId) }
            )
            SeasonsScreen(
                viewModel = seasonsViewModel,
                readOnly = session.isRoot,
                onOpenSeason = { navController.navigate(Routes.season(leagueId, it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SEASON,
            arguments = listOf(
                navArgument("leagueId") { type = NavType.StringType },
                navArgument("seasonId") { type = NavType.StringType }
            )
        ) { entry ->
            val leagueId = entry.arg("leagueId")
            val seasonId = entry.arg("seasonId")
            val seasonViewModel: SeasonViewModel = viewModel(
                key = "season-$seasonId",
                factory = factory { SeasonViewModel(leagueId, seasonId) }
            )
            SeasonScreen(
                viewModel = seasonViewModel,
                readOnly = session.isRoot,
                onBack = { navController.popBackStack() }
            )
        }
    }

    var previousUserId by remember { mutableStateOf(session.userId) }
    LaunchedEffect(session.userId) {
        if (session.userId != previousUserId) {
            previousUserId = session.userId
            navController.replaceWith(
                if (session.userId == null) Routes.LOGIN else Routes.LEAGUES
            )
        }
    }

    val createdLeagueId by authViewModel.createdLeagueId.collectAsStateWithLifecycle()
    LaunchedEffect(createdLeagueId, session.userId) {
        val leagueId = createdLeagueId
        if (leagueId != null && session.userId != null) {
            authViewModel.consumeCreatedLeague()
            navController.replaceWith(Routes.league(leagueId))
        }
    }
}

private fun NavBackStackEntry.arg(name: String): String = arguments?.getString(name).orEmpty()

private fun NavHostController.replaceWith(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun <T : ViewModel> factory(builder: () -> T) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = builder() as VM
}
