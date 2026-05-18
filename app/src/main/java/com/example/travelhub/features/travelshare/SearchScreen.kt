package com.example.travelhub.features.travelshare

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: PostViewModel = viewModel()) {
    val filteredPosts by viewModel.filteredPosts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // --- CONFIGURATION RECHERCHE VOCALE ---
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = data?.get(0) ?: ""
                if (spokenText.isNotBlank()) {
                    searchQuery = spokenText
                    viewModel.onSearchQueryChanged(spokenText)
                }
            }
        }
    )

    val startVoiceSearch = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

            // --- FORCE LE FRANÇAIS ICI ---
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "fr-FR")

            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites un lieu, un nom ou un tag...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Recherche vocale non disponible", Toast.LENGTH_SHORT).show()
        }
    }

    // États pour le sélecteur de date
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val categories = listOf(
        "Nature",
        "Musée",
        "Rue",
        "Magasin",
        "Restaurant",
        "Monument",
        "Plage",
        "Parc",
        "Hôtel",
        "Café",
        "Montagne",
        "Événement"
    )
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onDateSelected(null)
                    showDatePicker = false
                }) { Text("Effacer") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Barre de recherche + Bouton Date
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onSearchQueryChanged(it)
                },
                placeholder = { Text("Lieu, nom, tag...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                // MICRO AJOUTÉ ICI
                trailingIcon = {
                    IconButton(onClick = { startVoiceSearch() }) {
                        Icon(Icons.Default.Mic, contentDescription = "Vocal", tint = Color(0xFF1976D2))
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.size(48.dp).background(Color(0xFFF5F5F5), CircleShape)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (datePickerState.selectedDateMillis != null) Color(0xFF1976D2) else Color.Gray
                )
            }
        }

        // Catégories
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                        viewModel.onCategorySelected(selectedCategory)
                    },
                    label = { Text(category) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE3F2FD),
                        selectedLabelColor = Color(0xFF1976D2)
                    )
                )
            }
        }

        // Grille de résultats (Design amélioré avec coins plus arrondis)
        if (filteredPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun voyage trouvé", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPosts) { post ->
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(3.dp)
                            .clip(RoundedCornerShape(12.dp)) // Design plus "moderne" que 4.dp
                            .background(Color(0xFFF0F0F0)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}