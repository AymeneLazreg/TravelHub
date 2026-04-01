package com.example.travelhub.features.travelpath

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Modèle de données pour un itinéraire
data class Itinerary(
    val title: String,
    val price: String,
    val effort: String,
    val duration: String,
    val steps: List<String>
)

// Fausses données
val mockItineraries = listOf(
    Itinerary("Paris Full culture", "~70€", "Faible Effort", "8h", listOf("Musée du Louvre", "Cathédrale Notre-Dame de Paris", "Sainte-Chapelle")),
    Itinerary("Paris Budget Culture", "~20€", "Moyen Effort", "4h", listOf("Basilique du Sacré-Cœur", "Montmartre", "Jardin des Tuileries")),
    Itinerary("Paris Culture express", "~20€", "Faible Effort", "2h", listOf("Musée du Louvre (visite rapide / extérieur)", "Pont des Arts", "Cathédrale Notre-Dame..."))
)

@Composable
fun TravelPathScreen(onNewSearchClick: () -> Unit) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Voyager", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)), shape = RoundedCornerShape(20.dp)) {
                    Text("Planifier")
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(onClick = { Toast.makeText(context, "Onglet Favoris à venir", Toast.LENGTH_SHORT).show() }, shape = RoundedCornerShape(20.dp), border = null) {
                    Text("Favoris", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 140.dp)) {
                items(mockItineraries) { itinerary ->
                    ItineraryCard(itinerary)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Le bouton qui va déclencher la navigation
        Button(
            onClick = onNewSearchClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Nouvelle recherche", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ItineraryCard(itinerary: Itinerary) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.width(80.dp).height(120.dp).clip(RoundedCornerShape(40.dp)).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Place, contentDescription = "Image", tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = itinerary.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "${itinerary.price} | ${itinerary.effort} | ${itinerary.duration}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            itinerary.steps.forEach { step ->
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(text = "• ", color = Color.Gray, fontSize = 14.sp)
                    Text(text = step, color = Color.DarkGray, fontSize = 14.sp, lineHeight = 18.sp)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(36.dp)) {
                Text("Enregistrer", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(36.dp)) {
                Text("Itinéraire", fontSize = 12.sp, color = Color.Black)
            }
        }
    }
}