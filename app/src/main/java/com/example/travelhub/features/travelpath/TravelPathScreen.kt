package com.example.travelhub.features.travelpath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import coil.compose.AsyncImage

@Composable
fun TravelPathScreen(
    onNewSearchClick: () -> Unit,
    onItineraryClick: (String) -> Unit,
    travelPathViewModel: TravelPathViewModel // On reçoit le cerveau partagé !
) {
    val itineraries = travelPathViewModel.itineraries
    val isLoading = travelPathViewModel.isLoading
    val isSearching = travelPathViewModel.isSearching
    val currentSearchIds = travelPathViewModel.currentSearchIds // Les ID de la recherche actuelle

    // LOGIQUE DE FILTRAGE PARFAITE :
    // Si en recherche -> On affiche QUE les itinéraires dont l'ID fait partie de la recherche actuelle
    // Sinon -> On affiche QUE les favoris
    val listToDisplay = if (isSearching) {
        itineraries.filter { it.id in currentSearchIds }
    } else {
        itineraries.filter { it.favorite }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSearching) "Résultats" else "Mes Favoris",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isSearching) {
                    TextButton(onClick = { travelPathViewModel.resetToFavorites() }) {
                        Icon(Icons.Default.Favorite, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Voir Favoris")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color.Black)
            }

            if (listToDisplay.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSearching) "Aucun résultat." else "Vous n'avez pas encore de favoris.",
                        color = Color.Gray
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 150.dp)
            ) {
                items(items = listToDisplay, key = { it.id }) { itinerary ->
                    ItineraryCard(
                        itinerary = itinerary,
                        onFavoriteClick = { travelPathViewModel.toggleFavorite(itinerary) },
                        onDeleteClick = { travelPathViewModel.deleteItinerary(itinerary.id) },
                        onDetailsClick = { onItineraryClick(itinerary.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF5F5F5))
                }
            }
        }

        Button(
            onClick = onNewSearchClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .fillMaxWidth(0.8f)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Nouvelle recherche", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ItineraryCard(itinerary: Itinerary, onFavoriteClick: () -> Unit, onDeleteClick: () -> Unit, onDetailsClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = itinerary.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEEEEE)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(itinerary.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
            Text("${itinerary.price} • ${itinerary.duration}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            itinerary.steps.take(2).forEach {
                Text("• $it", fontSize = 13.sp, color = Color.DarkGray, maxLines = 1)
            }
            TextButton(onClick = onDetailsClick, contentPadding = PaddingValues(0.dp)) {
                Text("Voir l'itinéraire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (itinerary.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (itinerary.favorite) Color.Red else Color.Gray
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}