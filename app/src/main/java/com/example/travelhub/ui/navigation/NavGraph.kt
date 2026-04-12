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
import com.example.travelhub.features.profile.OtherProfileScreen
import com.example.travelhub.features.travelpath.TravelPathPreferencesScreen
import com.example.travelhub.features.travelshare.NotificationsScreen
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth // Import pour identifier l'utilisateur actuel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Instances uniques partagées
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

        // --- ÉCRAN PRINCIPAL ---
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
            val otherProfileViewModel: OtherProfileViewModel = viewModel()

            OtherProfileScreen(
                userId = userId,
                viewModel = otherProfileViewModel,
                postViewModel = postViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = notificationViewModel,
                onUserClick = { userId ->
                    navController.navigate("other_profile/$userId")
                },
                onNotificationContentClick = { notification ->
                    // 1. On prépare le ViewModel avant de changer d'écran
                    postViewModel.selectedPostIdFromNotif = notification.postId
                    postViewModel.shouldOpenCommentsFromNotif = (notification.type == "COMMENT")

                    // 2. On redirige vers l'accueil
                    navController.navigate("home") {
                        // On vide la pile pour que "Home" soit l'écran principal
                        popUpTo("home") { inclusive = true }
                    }
                }
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
    }
}