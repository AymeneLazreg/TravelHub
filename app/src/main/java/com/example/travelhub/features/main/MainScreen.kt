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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.travelhub.features.profile.ProfileScreen
import com.example.travelhub.features.travelpath.TravelPathScreen
import com.example.travelhub.features.travelpath.TravelPathViewModel // IMPORT
import com.example.travelhub.features.travelshare.AddPostScreen
import com.example.travelhub.features.travelshare.HomeScreen
import com.example.travelhub.features.travelshare.SearchScreen

data class BottomNavItem(val name: String, val route: String, val icon: ImageVector)

@Composable
fun MainScreen(
    navController: NavController,
    travelPathViewModel: TravelPathViewModel = viewModel() // NOUVEAU PARAMÈTRE
) {
    var currentRoute by rememberSaveable { mutableStateOf("accueil") }

    val items = listOf(
        BottomNavItem("Accueil", "accueil", Icons.Default.Home),
        BottomNavItem("Recherche", "recherche", Icons.Default.Search),
        BottomNavItem("Ajout", "ajout", Icons.Default.AddCircle),
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
                        onClick = { currentRoute = item.route },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            when (currentRoute) {
                "accueil" -> HomeScreen()
                "recherche" -> SearchScreen()
                "ajout" -> AddPostScreen()
                "profil" -> ProfileScreen(onEditClick = { navController.navigate("edit_profile") })
                "voyager" -> TravelPathScreen(
                    onNewSearchClick = { navController.navigate("travel_preferences") },
                    onItineraryClick = { id -> navController.navigate("itinerary_detail/$id") },
                    travelPathViewModel = travelPathViewModel // ON TRANSMET LE CERVEAU PARTAGÉ
                )
            }
        }
    }
}