package com.example.travelhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelhub.features.auth.LoginScreen
import com.example.travelhub.features.auth.RegisterScreen
import com.example.travelhub.features.main.MainScreen
import com.example.travelhub.features.profile.EditProfileScreen
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.travelpath.TravelPathPreferencesScreen
import com.example.travelhub.features.travelshare.NotificationsScreen
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // Instances uniques partagées pour toute la session
    val notificationViewModel: NotificationViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

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
            MainScreen(
                navController = navController,
                notificationViewModel = notificationViewModel,
                postViewModel = postViewModel,
                profileViewModel = profileViewModel
            )
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

        composable("notifications") {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = notificationViewModel // Reçoit la même instance
            )
        }
    }
}