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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.futsoccerchamp.presentation.auth.AuthViewModel
import com.futsoccerchamp.presentation.auth.LoginScreen
import com.futsoccerchamp.presentation.auth.SignUpScreen
import com.futsoccerchamp.presentation.championship.ChampionshipScreen
import com.futsoccerchamp.presentation.championship.ChampionshipViewModel
import com.futsoccerchamp.presentation.home.HomeScreen
import com.futsoccerchamp.presentation.home.HomeViewModel
import com.futsoccerchamp.presentation.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val userId by authViewModel.userId.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {

            SplashScreen(
                onFinished = {
                    navController.replaceWith(if (userId == null) Routes.LOGIN else Routes.HOME)
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

        composable(Routes.HOME) {
            val ownerId = userId
            if (ownerId != null) {
                val homeViewModel: HomeViewModel = viewModel(
                    key = "home-$ownerId",
                    factory = factory { HomeViewModel(ownerId) }
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenChampionship = { navController.navigate(Routes.championship(it)) },
                    onSignOut = { authViewModel.signOut() }
                )
            }
        }

        composable(
            route = Routes.CHAMPIONSHIP,
            arguments = listOf(navArgument("championshipId") { type = NavType.StringType })
        ) { entry ->
            val championshipId = entry.arguments?.getString("championshipId").orEmpty()
            val championshipViewModel: ChampionshipViewModel = viewModel(
                key = "championship-$championshipId",
                factory = factory { ChampionshipViewModel(championshipId) }
            )
            ChampionshipScreen(
                viewModel = championshipViewModel,
                onBackToChampionships = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onSignOut = { authViewModel.signOut() }
            )
        }
    }

    var previousUserId by remember { mutableStateOf(userId) }
    LaunchedEffect(userId) {
        if (userId != previousUserId) {
            previousUserId = userId
            navController.replaceWith(if (userId == null) Routes.LOGIN else Routes.HOME)
        }
    }
}

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
