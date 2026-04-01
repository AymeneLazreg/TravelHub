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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onEditClick: () -> Unit) {
    // Fausses photos pour le profil utilisateur
    val userPhotos = (1..12).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- En-tête du profil ---
        Column(modifier = Modifier.padding(24.dp)) {
            // Username et Menu
            Text(
                text = "@johndoe",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Ligne : Photo de profil + Stats (Posts, Followers, Following)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Photo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Photo", tint = Color.Gray)
                }

                // Stats
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    ProfileStat(count = "12", label = "Posts")
                    ProfileStat(count = "145", label = "Followers")
                    ProfileStat(count = "89", label = "Following")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bio et Localisation
            Text(text = "John Doe", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Voyageur passionné 🌍📸\nToujours à la recherche du prochain coucher de soleil.", fontSize = 14.sp)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Montpellier, France", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton Modifier le profil
            Button(
                onClick = onEditClick, // Déclenche la navigation vers l'édition
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)
            ) {
                Text("Modifier le profil", fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

        // --- Grille des photos de l'utilisateur ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(userPhotos) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Photo", tint = Color.Gray)
                }
            }
        }
    }
}

// Petit composant pour afficher les statistiques joliment
@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}