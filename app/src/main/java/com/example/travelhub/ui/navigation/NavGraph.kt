package com.example.travelhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelhub.features.auth.LoginScreen
import com.example.travelhub.features.auth.RegisterScreen
import com.example.travelhub.features.main.MainScreen
import com.example.travelhub.features.profile.EditProfileScreen
import com.example.travelhub.features.travelpath.TravelPathPreferencesScreen
import com.example.travelhub.features.travelshare.NotificationsScreen // N'oublie pas l'import !

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
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
            MainScreen(navController = navController)
        }

        composable("travel_preferences") {
            TravelPathPreferencesScreen(
                onBackClick = { navController.popBackStack() },
                onGenerateClick = { navController.popBackStack() }
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        // --- AJOUT DE LA ROUTE NOTIFICATIONS ICI ---
        composable("notifications") {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}