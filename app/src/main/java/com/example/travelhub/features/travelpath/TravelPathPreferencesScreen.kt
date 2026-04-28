package com.example.travelhub.features.travelpath

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPathPreferencesScreen(
    onBackClick: () -> Unit,
    onGenerateClick: () -> Unit,
    travelPathViewModel: TravelPathViewModel = viewModel()
) {
    val context = LocalContext.current
    val citySuggestions = travelPathViewModel.citySuggestions

    var locationInput by remember { mutableStateOf("") } // Changé de 'ville' à 'locationInput'
    var expanded by remember { mutableStateOf(false) }

    var budget by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val activities = listOf("Culture", "Restauration", "Loisirs", "Découverte")
    val selectedActivities = remember { mutableStateListOf("Culture") }

    val effortLevels = listOf("Faible", "Moyen", "Intense")
    var selectedEffort by remember { mutableStateOf("Moyen") }

    val weatherSensitivities = listOf("Chaleur", "Froid", "Pluie")
    val selectedSensitivities = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
            IconButton(onClick = onBackClick, enabled = !isGenerating) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text("Nouveau Parcours", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        Text("Destination (Ville et Pays)", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = locationInput,
                onValueChange = { newValue ->
                    locationInput = newValue
                    travelPathViewModel.searchCity(newValue)
                    expanded = true
                },
                placeholder = { Text("Ex: Londres, Royaume-Uni...") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            DropdownMenu(
                expanded = expanded && citySuggestions.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .exposedDropdownSize(),
                properties = PopupProperties(focusable = false)
            ) {
                citySuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            locationInput = suggestion // On garde le format "Ville (Pays)"
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Erreur de génération :", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 14.sp)
                    }
                    TextButton(
                        onClick = { errorMessage = "" },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Fermer", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        Button(
            onClick = {
                if (locationInput.isBlank() || budget.isBlank() || duration.isBlank()) {
                    Toast.makeText(context, "Veuillez remplir les informations de destination.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isGenerating = true
                errorMessage = ""

                travelPathViewModel.generateAndSaveItineraries(
                    location = locationInput.trim(),
                    budget = budget,
                    duration = duration,
                    activities = selectedActivities.toList(),
                    effort = selectedEffort,
                    sensitivities = selectedSensitivities.toList(),
                    onSuccess = {
                        isGenerating = false
                        onGenerateClick()
                    },
                    onError = { errorMsg ->
                        isGenerating = false
                        errorMessage = errorMsg
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
            enabled = !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Générer les options", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}