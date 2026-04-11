package com.example.travelhub.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
@Composable
fun ProfileScreen(
    onEditClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile = profileViewModel.userProfile
    val isLoading = profileViewModel.isLoading
    val userPosts = profileViewModel.userPosts
    val favoritePosts = profileViewModel.favoritePosts

    // État pour savoir quel onglet est sélectionné (0 pour Posts, 1 pour Favoris)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else {
            Column(modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)) {
                Text(text = "@${userProfile.username}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    if (userProfile.photoUrl.isNotEmpty()) {
                        AsyncImage(model = userProfile.photoUrl, contentDescription = "Photo de profil", modifier = Modifier.size(80.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = "Photo", tint = Color.Gray)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        ProfileStat(count = userPosts.size.toString(), label = "Posts")
                        // Compteur dynamique des favoris
                        ProfileStat(count = userProfile.favorites.size.toString(), label = "Favoris")
                    }
                }

                Text(text = "${userProfile.prenom} ${userProfile.nom}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = userProfile.bio, fontSize = 14.sp)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = userProfile.location, color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)) {
                    Text("Modifier le profil", fontWeight = FontWeight.SemiBold)
                }
            }

            // --- SYSTÈME D'ONGLETS ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color.Black
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Mes Posts", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Favoris", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            // --- AFFICHAGE DE LA GRILLE SELON L'ONGLET ---
            val currentGridPosts = if (selectedTabIndex == 0) userPosts else favoritePosts

            if (currentGridPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTabIndex == 0) "Aucune publication" else "Aucun favori ajouté",
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentGridPosts) { post ->
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.aspectRatio(1f).padding(1.dp).clip(RoundedCornerShape(2.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}