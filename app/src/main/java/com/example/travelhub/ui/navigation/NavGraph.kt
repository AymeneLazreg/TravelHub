package com.example.travelhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travelhub.features.auth.LoginScreen
import com.example.travelhub.features.auth.RegisterScreen
import com.example.travelhub.features.main.MainScreen
import com.example.travelhub.features.profile.EditProfileScreen
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.profile.OtherProfileViewModel
import com.example.travelhub.features.profile.OtherProfileScreen // Import du nouveau fichier
import com.example.travelhub.features.travelpath.TravelPathPreferencesScreen
import com.example.travelhub.features.travelshare.NotificationsScreen
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Instances uniques partagées pour les écrans principaux
    val notificationViewModel: NotificationViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // --- AUTHENTIFICATION ---
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
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

        // --- ÉCRAN PRINCIPAL (CONTENEUR) ---
        composable("home") {
            MainScreen(
                navController = navController,
                notificationViewModel = notificationViewModel,
                postViewModel = postViewModel,
                profileViewModel = profileViewModel
            )
        }

        // --- PROFIL D'UN AUTRE UTILISATEUR ---
        composable(
            route = "other_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            // On crée une instance de ViewModel spécifique pour cet utilisateur
            val otherProfileViewModel: OtherProfileViewModel = viewModel()

            OtherProfileScreen(
                userId = userId,
                viewModel = otherProfileViewModel,
                postViewModel = postViewModel, // Passé pour permettre les interactions (likes/reports)
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- FONCTIONNALITÉS ANNEXES ---
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

        composable("notifications") {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = notificationViewModel
            )
        }
    }
}