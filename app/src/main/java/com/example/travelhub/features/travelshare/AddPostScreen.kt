package com.example.travelhub.features.travelshare

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    onPostSuccess: () -> Unit = {},
    viewModel: PostViewModel = viewModel()
) {
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expandedMenu by remember { mutableStateOf(false) }

    // --- Catégorie (Choix unique) ---
    val categories = listOf("Nature", "Musée", "Rue", "Magasin", "Restaurant")
    var selectedCategory by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isUploading by viewModel.isUploading.collectAsState()

    val availableTags = listOf("Coucher de soleil", "Architecture", "Calme", "Gratuit", "Hiver")
    val selectedTags = remember { mutableStateListOf<String>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Upload a travel moment",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Zone d'upload Photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Upload Photo", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }
            } else {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Location
        Text("Location", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            placeholder = { Text("Ex: Paris, France") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text("Description", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section Type de lieu (Catégories - Choix Unique)
        Text("Catégorie", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE3F2FD),
                        selectedLabelColor = Color(0xFF1976D2)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Tags (Détails - Choix Multiples)
        Text("Tags", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableTags) { tag ->
                FilterChip(
                    selected = selectedTags.contains(tag),
                    onClick = {
                        if (selectedTags.contains(tag)) selectedTags.remove(tag) else selectedTags.add(tag)
                    },
                    label = { Text(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton de publication
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.Black)
            } else {
                Button(
                    onClick = { expandedMenu = true },
                    enabled = selectedImageUri != null && description.isNotBlank() && selectedCategory.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
                ) {
                    Text("Publish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Publier en public") },
                    onClick = {
                        expandedMenu = false
                        selectedImageUri?.let { uri ->
                            viewModel.uploadPost(
                                imageUri = uri,
                                description = description,
                                location = location,
                                category = selectedCategory,
                                tags = selectedTags.toList()
                            ) {
                                Toast.makeText(context, "Post publié !", Toast.LENGTH_LONG).show()
                                onPostSuccess()
                            }
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}