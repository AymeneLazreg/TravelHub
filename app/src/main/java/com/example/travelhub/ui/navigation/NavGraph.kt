package com.example.travelhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel // <-- IMPORT À VÉRIFIER
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelhub.features.auth.LoginScreen
import com.example.travelhub.features.auth.RegisterScreen
import com.example.travelhub.features.main.MainScreen
import com.example.travelhub.features.profile.EditProfileScreen
import com.example.travelhub.features.travelpath.ItineraryDetailScreen
import com.example.travelhub.features.travelpath.TravelPathPreferencesScreen
import com.example.travelhub.features.travelpath.TravelPathViewModel // <-- IMPORT DU VIEWMODEL

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // NOUVEAU : On crée le cerveau ICI pour le partager entre tous les écrans du voyage
    val sharedTravelViewModel: TravelPathViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") },
                onAnonymousClick = { navController.navigate("home") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            // On le donne à l'écran principal
            MainScreen(navController = navController, travelPathViewModel = sharedTravelViewModel)
        }

        composable("travel_preferences") {
            // On le donne aussi au formulaire !
            TravelPathPreferencesScreen(
                onBackClick = { navController.popBackStack() },
                onGenerateClick = { navController.popBackStack() },
                travelPathViewModel = sharedTravelViewModel
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable("itinerary_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ItineraryDetailScreen(
                itineraryId = id,
                onBackClick = { navController.popBackStack() },
                travelPathViewModel = sharedTravelViewModel
            )
        }
    }
}