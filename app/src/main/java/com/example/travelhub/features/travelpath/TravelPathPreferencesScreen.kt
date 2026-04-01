package com.example.travelhub.features.travelpath

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPathPreferencesScreen(
    onBackClick: () -> Unit,
    onGenerateClick: () -> Unit
) {
    val context = LocalContext.current

    // Variables d'état pour le formulaire
    var budget by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    // Listes de choix
    val activities = listOf("Culture", "Restauration", "Loisirs", "Découverte")
    val selectedActivities = remember { mutableStateListOf("Culture") }

    val effortLevels = listOf("Faible", "Moyen", "Intense")
    var selectedEffort by remember { mutableStateOf("Moyen") }

    val weatherSensitivities = listOf("Froid", "Chaleur", "Humidité")
    val selectedSensitivities = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // En-tête avec bouton retour
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                text = "Nouveau Parcours",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Section : Activités
        Text("Activités souhaitées", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activities) { activity ->
                FilterChip(
                    selected = selectedActivities.contains(activity),
                    onClick = {
                        if (selectedActivities.contains(activity)) selectedActivities.remove(activity)
                        else selectedActivities.add(activity)
                    },
                    label = { Text(activity) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section : Budget et Durée
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("Budget max (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Durée dispo (h)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section : Niveau d'effort (Un seul choix possible)
        Text("Niveau d'effort accepté", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(effortLevels) { effort ->
                FilterChip(
                    selected = selectedEffort == effort,
                    onClick = { selectedEffort = effort },
                    label = { Text(effort) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section : Sensibilité Météo
        Text("Sensibilités météo", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(weatherSensitivities) { weather ->
                FilterChip(
                    selected = selectedSensitivities.contains(weather),
                    onClick = {
                        if (selectedSensitivities.contains(weather)) selectedSensitivities.remove(weather)
                        else selectedSensitivities.add(weather)
                    },
                    label = { Text(weather) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Bouton de génération
        Button(
            onClick = {
                Toast.makeText(context, "Génération des parcours en cours...", Toast.LENGTH_SHORT).show()
                onGenerateClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
        ) {
            Text("Générer les options", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}