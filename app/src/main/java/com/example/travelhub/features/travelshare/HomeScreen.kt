package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Modèle de données temporaire pour nos publications
data class TravelPost(
    val id: Int,
    val authorName: String,
    val location: String,
    val timeAgo: String,
    val description: String,
    val likes: Int,
    val comments: Int
)

// 2. Fausse base de données basée sur vos maquettes
val mockPosts = listOf(
    TravelPost(1, "Helena", "Greece", "3 min ago", "Beautiful sunset near Oia villa...", 21, 4),
    TravelPost(2, "Daniel", "Spain", "2 hrs ago", "Wandering through Madrid's hidden streets today, and honestly the atmosphere alone felt like a trip.", 6, 18),
    TravelPost(3, "Nada Abir", "Montpellier", "5 hrs ago", "Walking through the historic center felt like stepping back in time...", 19, 2)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Barre de recherche ---
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search photos, places, travelers...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            // CORRECTION ICI : Utilisation de OutlinedTextFieldDefaults.colors
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        // --- Filtres (Nature, City, etc.) ---
        val filters = listOf("All", "Nature", "City", "Beach", "Museum")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = filter == "All", // On simule que "All" est sélectionné
                    onClick = { /* Logique de filtre à venir */ },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Le fil d'actualité (LazyColumn) ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Laisse de la place pour la barre de navigation en bas
        ) {
            items(mockPosts) { post ->
                PostItem(post)
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

// 3. Le design d'une publication individuelle
@Composable
fun PostItem(post: TravelPost) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        // En-tête : Avatar + Nom + Lieu + Date
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) // Placeholder pour la photo de profil

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = " - ${post.location}", fontSize = 14.sp, color = Color.DarkGray)
                }
                Text(text = post.timeAgo, fontSize = 12.sp, color = Color.Gray)
            }

            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image de la publication (Placeholder temporaire)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Place, contentDescription = "Image", tint = Color.Gray, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(text = post.description, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Interactions (Likes, Commentaires, Carte)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "❤️ ${post.likes} likes", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = "💬 ${post.comments} comments", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.weight(1f))

            Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "View on...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}