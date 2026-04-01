package com.example.travelhub.features.travelshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }

    // On simule 21 photos pour remplir notre grille d'exploration
    val mockImages = (1..21).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Barre de recherche en haut
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Lieu, thème, auteur...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        // La grille d'images (3 colonnes)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // On fixe à 3 images par ligne
            contentPadding = PaddingValues(start = 2.dp, end = 2.dp, bottom = 90.dp), // Padding bas pour la navigation
            modifier = Modifier.fillMaxSize()
        ) {
            items(mockImages) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // aspect carré parfait
                        .padding(2.dp) // petit espace entre chaque photo
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    // Icône temporaire en attendant les vraies photos
                    Icon(Icons.Default.LocationOn, contentDescription = "Photo", tint = Color.Gray)
                }
            }
        }
    }
}