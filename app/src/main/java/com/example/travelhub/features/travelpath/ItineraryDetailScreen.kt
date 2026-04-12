package com.example.travelhub.features.travelpath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun ItineraryDetailScreen(
    itineraryId: String,
    onBackClick: () -> Unit,
    travelPathViewModel: TravelPathViewModel = viewModel()
) {
    // On cherche l'itinéraire cliqué dans la liste
    val itinerary = travelPathViewModel.itineraries.find { it.id == itineraryId }

    if (itinerary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())
    ) {
        // En-tête avec image
        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            AsyncImage(
                model = itinerary.imageUrl,
                contentDescription = itinerary.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Bouton retour par-dessus l'image
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(16.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
        }

        // Contenu
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = itinerary.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Badges d'infos
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(onClick = {}, label = { Text(itinerary.price) })
                SuggestionChip(onClick = {}, label = { Text(itinerary.duration) })
                SuggestionChip(onClick = {}, label = { Text(itinerary.effort) })
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Votre Parcours", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))

            // Liste des étapes avec icônes
            itinerary.steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = step, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray)
                }
            }
        }
    }
}