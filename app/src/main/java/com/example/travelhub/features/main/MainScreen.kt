package com.example.travelhub.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

// --- IMPORTS TRAVELPATH (Ton travail) ---
import com.example.travelhub.features.travelpath.TravelPathScreen
import com.example.travelhub.features.travelpath.TravelPathViewModel

// --- IMPORTS TRAVELSHARE (Son travail) ---
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.profile.ProfileScreen
import com.example.travelhub.features.travelshare.AddPostScreen
import com.example.travelhub.features.travelshare.HomeScreen
import com.example.travelhub.features.travelshare.SearchScreen
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    navController: NavController,
    // --- FUSION DES VIEWMODELS ---
    travelPathViewModel: TravelPathViewModel, // Le tien
    notificationViewModel: NotificationViewModel, // Les siens
    postViewModel: PostViewModel,
    profileViewModel: ProfileViewModel
) {
    // Récupération de l'ID de l'utilisateur actuel pour la comparaison
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    // --- INITIALISATION AU CHARGEMENT (Son travail) ---
    LaunchedEffect(Unit) {
        profileViewModel.refreshAllData()
        notificationViewModel.refreshNotifications()
    }

    // Utilisation de rememberSaveable (Ton travail) pour la stabilité de navigation
    var currentRoute by rememberSaveable { mutableStateOf("accueil") }

    val items = listOf(
        BottomNavItem("Accueil", "accueil", Icons.Default.Home),
        BottomNavItem("Recherche", "recherche", Icons.Default.Search),
        BottomNavItem("Ajout", "ajout", Icons.Default.AddCircle),
        BottomNavItem("Profil", "profil", Icons.Default.Person),
        BottomNavItem("Voyager", "voyager", Icons.Default.Flight) // Gardé "Flight" pour le design
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        selected = currentRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                            // Rafraîchissement manuel spécifique quand on clique sur l'onglet Profil (Son travail)
                            if (item.route == "profil") {
                                profileViewModel.loadUserPosts()
                            }
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (currentRoute) {
                // --- SES ONGLETS ---
                "accueil" -> HomeScreen(
                    viewModel = postViewModel,
                    profileViewModel = profileViewModel,
                    notificationViewModel = notificationViewModel,
                    onNotificationsClick = {
                        navController.navigate("notifications")
                    },
                    onUserClick = { userId ->
                        // CONDITION : Si c'est mon ID, je vais sur l'onglet profil
                        if (userId == currentUserId) {
                            currentRoute = "profil"
                            profileViewModel.loadUserPosts()
                        } else {
                            navController.navigate("other_profile/$userId")
                        }
                    }
                )
                "recherche" -> SearchScreen()
                "ajout" -> AddPostScreen(
                    postViewModel = postViewModel,
                    profileViewModel = profileViewModel,
                    onPostSuccess = {
                        currentRoute = "accueil"
                        profileViewModel.loadUserPosts()
                    }
                )
                "profil" -> ProfileScreen(
                    onEditClick = { navController.navigate("edit_profile") },
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    profileViewModel = profileViewModel,
                    postViewModel = postViewModel,
                    onUserClick = { userId ->
                        // Si on clique sur soi-même depuis son propre profil, on ne fait rien
                        if (userId != currentUserId) {
                            navController.navigate("other_profile/$userId")
                        }
                    }
                )
                // --- TON ONGLET ---
                "voyager" -> TravelPathScreen(
                    onNewSearchClick = { navController.navigate("travel_preferences") },
                    onItineraryClick = { id -> navController.navigate("itinerary_detail/$id") },
                    travelPathViewModel = travelPathViewModel
                )
            }
        }
    }
}