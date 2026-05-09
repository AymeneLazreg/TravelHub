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

// --- IMPORTS TRAVELPATH ---
import com.example.travelhub.features.travelpath.TravelPathScreen
import com.example.travelhub.features.travelpath.TravelPathViewModel

// --- IMPORTS TRAVELSHARE ---
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.profile.ProfileScreen
import com.example.travelhub.features.travelshare.AddPostScreen
import com.example.travelhub.features.travelshare.HomeScreen
import com.example.travelhub.features.travelshare.SearchScreen
import com.example.travelhub.features.travelshare.GroupScreen
import com.example.travelhub.features.travelshare.viewmodel.NotificationViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.example.travelhub.features.travelshare.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    navController: NavController,
    travelPathViewModel: TravelPathViewModel,
    notificationViewModel: NotificationViewModel,
    postViewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    groupViewModel: GroupViewModel
) {
    // Vérification de l'état de connexion (null = anonyme) [cite: 12, 17]
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUserId = currentUser?.uid
    val isAnonymous = currentUser == null

    LaunchedEffect(Unit) {
        // On ne rafraîchit les données sociales que si l'utilisateur est connecté
        if (!isAnonymous) {
            profileViewModel.refreshAllData()
            notificationViewModel.refreshNotifications()
            groupViewModel.fetchUserGroups()
        }
    }

    var currentRoute by rememberSaveable { mutableStateOf("accueil") }

    // --- LOGIQUE DE FILTRAGE DES ONGLETS (MODE ANONYME) ---
    // On définit les items de base accessibles à tous [cite: 23, 69]
    val items = remember(isAnonymous) {
        val baseItems = mutableListOf(
            BottomNavItem("Accueil", "accueil", Icons.Default.Home),
            BottomNavItem("Recherche", "recherche", Icons.Default.Search)
        )

        // On n'ajoute les fonctions sociales que pour le mode connecté
        if (!isAnonymous) {
            baseItems.add(BottomNavItem("Ajout", "ajout", Icons.Default.AddCircle))
            baseItems.add(BottomNavItem("Groupes", "groupes", Icons.Default.Groups))
            baseItems.add(BottomNavItem("Profil", "profil", Icons.Default.Person))
        }

        // L'onglet "Voyager" (Passerelle) reste accessible pour l'intégration globale [cite: 3, 69]
        baseItems.add(BottomNavItem("Voyager", "voyager", Icons.Default.Flight))
        baseItems
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        selected = currentRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                            if (item.route == "profil") {
                                profileViewModel.loadUserPosts()
                            }
                            if (item.route == "groupes") {
                                groupViewModel.fetchUserGroups()
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
                "accueil" -> HomeScreen(
                    viewModel = postViewModel,
                    profileViewModel = profileViewModel,
                    notificationViewModel = notificationViewModel,
                    onNotificationsClick = {
                        // En mode anonyme, on peut rediriger vers le login ou bloquer
                        if (!isAnonymous) {
                            navController.navigate("notifications")
                        }
                    },
                    onUserClick = { userId ->
                        if (userId == currentUserId) {
                            currentRoute = "profil"
                            profileViewModel.loadUserPosts()
                        } else {
                            navController.navigate("other_profile/$userId")
                        }
                    },
                    onGroupClick = { groupId, groupName ->
                        if (!isAnonymous) {
                            navController.navigate("group_detail/$groupId/$groupName")
                        }
                    },
                    onMapClick = {
                        // La vue carte (pins) est accessible en mode anonyme [cite: 23, 33]
                        navController.navigate("map_explorer")
                    }
                )
                "recherche" -> SearchScreen() // Accessible en anonyme [cite: 13, 23, 29]

                "ajout" -> if (!isAnonymous) {
                    AddPostScreen(
                        postViewModel = postViewModel,
                        profileViewModel = profileViewModel,
                        onPostSuccess = {
                            currentRoute = "accueil"
                            profileViewModel.loadUserPosts()
                        }
                    )
                }

                "groupes" -> if (!isAnonymous) {
                    GroupScreen(
                        viewModel = groupViewModel,
                        navController = navController
                    )
                }

                "profil" -> if (!isAnonymous) {
                    ProfileScreen(
                        onEditClick = { navController.navigate("edit_profile") },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        },
                        profileViewModel = profileViewModel,
                        postViewModel = postViewModel,
                        onUserClick = { userId ->
                            if (userId != currentUserId) {
                                navController.navigate("other_profile/$userId")
                            }
                        }
                    )
                }

                "voyager" -> TravelPathScreen(
                    onNewSearchClick = { navController.navigate("travel_preferences") },
                    onItineraryClick = { id -> navController.navigate("itinerary_detail/$id") },
                    travelPathViewModel = travelPathViewModel
                )
            }
        }
    }
}