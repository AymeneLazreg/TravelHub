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
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    LaunchedEffect(Unit) {
        profileViewModel.refreshAllData()
        notificationViewModel.refreshNotifications()
        groupViewModel.fetchUserGroups()
    }

    var currentRoute by rememberSaveable { mutableStateOf("accueil") }

    val items = listOf(
        BottomNavItem("Accueil", "accueil", Icons.Default.Home),
        BottomNavItem("Recherche", "recherche", Icons.Default.Search),
        BottomNavItem("Ajout", "ajout", Icons.Default.AddCircle),
        BottomNavItem("Groupes", "groupes", Icons.Default.Groups),
        BottomNavItem("Profil", "profil", Icons.Default.Person),
        BottomNavItem("Voyager", "voyager", Icons.Default.Flight)
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
                        navController.navigate("notifications")
                    },
                    onUserClick = { userId ->
                        if (userId == currentUserId) {
                            currentRoute = "profil"
                            profileViewModel.loadUserPosts()
                        } else {
                            navController.navigate("other_profile/$userId")
                        }
                    },
                    // --- CORRECTION : AJOUT DE LA NAVIGATION VERS LE GROUPE ---
                    onGroupClick = { groupId, groupName ->
                        navController.navigate("group_detail/$groupId/$groupName")
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
                "groupes" -> GroupScreen(
                    viewModel = groupViewModel,
                    navController = navController // Ajout de navController si ton GroupScreen en a besoin
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
                        if (userId != currentUserId) {
                            navController.navigate("other_profile/$userId")
                        }
                    }
                )
                "voyager" -> TravelPathScreen(
                    onNewSearchClick = { navController.navigate("travel_preferences") },
                    onItineraryClick = { id -> navController.navigate("itinerary_detail/$id") },
                    travelPathViewModel = travelPathViewModel
                )
            }
        }
    }
}