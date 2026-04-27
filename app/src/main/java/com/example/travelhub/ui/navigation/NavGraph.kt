package com.example.travelhub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// --- IMPORTS TRAVELPATH ---
import com.example.travelhub.features.travelpath.ItineraryDetailScreen
import com.example.travelhub.features.travelpath.TravelPathPreferencesScreen
import com.example.travelhub.features.travelpath.TravelPathViewModel

// --- IMPORTS TRAVELSHARE & AUTH ---
import com.example.travelhub.features.auth.LoginScreen
import com.example.travelhub.features.auth.RegisterScreen
import com.example.travelhub.features.main.MainScreen
import com.example.travelhub.features.profile.EditProfileScreen
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.profile.OtherProfileViewModel
import com.example.travelhub.features.profile.OtherProfileScreen
import com.example.travelhub.features.travelshare.NotificationsScreen
// AJOUTE CET IMPORT :
import com.example.travelhub.features.travelshare.GroupScreen
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
// AJOUTE CET IMPORT :
import com.example.travelhub.features.travelshare.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    // --- INSTANCES PARTAGÉES ---
    val sharedTravelViewModel: TravelPathViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    // ON CRÉE L'INSTANCE DU CERVEAU DES GROUPES ICI :
    val groupViewModel: GroupViewModel = viewModel()

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
                travelPathViewModel = sharedTravelViewModel,
                notificationViewModel = notificationViewModel,
                postViewModel = postViewModel,
                profileViewModel = profileViewModel,
                // ON PASSE LE GROUPVIEWMODEL AU MAINSCREEN S'IL EN A BESOIN :
                groupViewModel = groupViewModel
            )
        }

        // --- NOUVELLE ROUTE : GROUPES ---
        composable("groups") {
            GroupScreen(
                viewModel = groupViewModel
            )
        }

        // --- TRAVELPATH ---
        composable("travel_preferences") {
            TravelPathPreferencesScreen(
                onBackClick = { navController.popBackStack() },
                onGenerateClick = { navController.popBackStack() },
                travelPathViewModel = sharedTravelViewModel
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

        // --- TRAVELSHARE & PROFILS ---
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
                    postViewModel.selectedPostIdFromNotif = notification.postId
                    postViewModel.shouldOpenCommentsFromNotif = (notification.type == "COMMENT")
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
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