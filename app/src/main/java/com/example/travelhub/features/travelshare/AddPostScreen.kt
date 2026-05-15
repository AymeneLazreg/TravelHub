package com.example.travelhub.features.travelshare

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.travelhub.features.profile.ProfileViewModel
import com.example.travelhub.features.travelshare.viewmodel.GroupViewModel
import com.example.travelhub.features.travelshare.viewmodel.PostViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    onPostSuccess: () -> Unit = {},
    postViewModel: PostViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    var locationName by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expandedMenu by remember { mutableStateOf(false) }

    var showMapDialog by remember { mutableStateOf(false) }

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
    var selectedCategory by remember { mutableStateOf("") }

    var tagInput by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    val context = LocalContext.current
    val isUploading by postViewModel.isUploading.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.6108, 3.8767), 13f)
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = data?.get(0) ?: ""

                if (spokenText.isNotBlank()) {
                    description = if (description.isBlank()) spokenText else "$description $spokenText"
                }
            }
        }
    )

    val startVoiceInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "fr-FR")

            putExtra(RecognizerIntent.EXTRA_PROMPT, "Décrivez votre moment...")
        }

        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Vocal non disponible", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude

                    val currentLatLng = LatLng(latitude, longitude)

                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                } else {
                    Toast.makeText(
                        context,
                        "Position actuelle indisponible, déplace la carte manuellement",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                showMapDialog = true
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Impossible de récupérer la position",
                    Toast.LENGTH_SHORT
                ).show()

                showMapDialog = true
            }
        } else {
            Toast.makeText(context, "Permission GPS refusée", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(Unit) {
        groupViewModel.fetchUserGroups()
    }

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
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )

                    Text(
                        text = "Upload Photo",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Location",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = locationName,
            onValueChange = {
                locationName = it
                if (fullAddress.isBlank()) {
                    fullAddress = it
                }
            },
            placeholder = { Text("Ex: Paris, France") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Carte",
                        tint = Color(0xFF2196F3)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Description",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5,
            trailingIcon = {
                IconButton(onClick = { startVoiceInput() }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dictée vocale",
                        tint = Color(0xFFE91E63)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Catégorie",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tags",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            placeholder = { Text("Ex: Paris, plage, coucher de soleil") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        val cleanTag = tagInput.trim()

                        if (cleanTag.isNotBlank() && !selectedTags.contains(cleanTag)) {
                            selectedTags.add(cleanTag)
                            tagInput = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter un tag",
                        tint = Color(0xFF2196F3)
                    )
                }
            }
        )

        if (selectedTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedTags) { tag ->
                    AssistChip(
                        onClick = { },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Supprimer le tag",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        selectedTags.remove(tag)
                                    }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.Black)
            } else {
                Button(
                    onClick = { expandedMenu = true },
                    enabled = selectedImageUri != null &&
                            description.isNotBlank() &&
                            locationName.isNotBlank() &&
                            selectedCategory.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
                ) {
                    Text(
                        text = "Publish to...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                DropdownMenuItem(
                    text = { Text("Publier en public") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expandedMenu = false

                        publishAction(
                            groupId = null,
                            groupName = null,
                            uri = selectedImageUri,
                            description = description,
                            location = locationName,
                            fullAddress = fullAddress.ifBlank { locationName },
                            latitude = latitude,
                            longitude = longitude,
                            category = selectedCategory,
                            tags = selectedTags.toList(),
                            postViewModel = postViewModel,
                            profileViewModel = profileViewModel,
                            context = context,
                            onPostSuccess = onPostSuccess
                        )
                    }
                )

                if (groupViewModel.userGroups.isNotEmpty()) {
                    Divider()

                    groupViewModel.userGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text("Groupe : ${group.name}") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                expandedMenu = false

                                publishAction(
                                    groupId = group.id,
                                    groupName = group.name,
                                    uri = selectedImageUri,
                                    description = description,
                                    location = locationName,
                                    fullAddress = fullAddress.ifBlank { locationName },
                                    latitude = latitude,
                                    longitude = longitude,
                                    category = selectedCategory,
                                    tags = selectedTags.toList(),
                                    postViewModel = postViewModel,
                                    profileViewModel = profileViewModel,
                                    context = context,
                                    onPostSuccess = onPostSuccess
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showMapDialog) {
        AlertDialog(
            onDismissRequest = { showMapDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val center = cameraPositionState.position.target

                        latitude = center.latitude
                        longitude = center.longitude

                        val previousLocation = locationName
                        val previousFullAddress = fullAddress

                        updateAddressFromCoordinates(context, latitude, longitude) { result ->
                            locationName = if (result.city.isNotBlank()) {
                                result.city
                            } else {
                                previousLocation.ifBlank { "Lieu sélectionné" }
                            }

                            fullAddress = if (result.fullAddress.isNotBlank()) {
                                result.fullAddress
                            } else {
                                previousFullAddress.ifBlank { locationName }
                            }
                        }

                        showMapDialog = false
                    }
                ) {
                    Text("Valider")
                }
            },
            title = { Text("Ajuster la position") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                    )

                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp),
                        tint = Color.Red
                    )
                }
            }
        )
    }
}

private data class AddressResult(
    val city: String,
    val fullAddress: String
)

private fun updateAddressFromCoordinates(
    context: Context,
    lat: Double,
    lng: Double,
    onResult: (AddressResult) -> Unit
) {
    fun extractAddress(locale: Locale): AddressResult? {
        return try {
            val geocoder = Geocoder(context, locale)
            val addresses = geocoder.getFromLocation(lat, lng, 5)

            Log.d("GeocoderDebug", "Locale utilisée : $locale")
            Log.d("GeocoderDebug", "Coordonnées : lat=$lat, lng=$lng")
            Log.d("GeocoderDebug", "Résultats Geocoder : $addresses")

            val address = addresses?.firstOrNull()

            if (address != null) {
                val full = when {
                    !address.getAddressLine(0).isNullOrBlank() -> {
                        address.getAddressLine(0)
                    }

                    else -> {
                        ""
                    }
                }

                val city = when {
                    !address.locality.isNullOrBlank() -> {
                        address.locality
                    }

                    !address.subAdminArea.isNullOrBlank() -> {
                        address.subAdminArea
                    }

                    !address.adminArea.isNullOrBlank() -> {
                        address.adminArea
                    }

                    !address.countryName.isNullOrBlank() -> {
                        address.countryName
                    }

                    else -> {
                        full
                    }
                }

                AddressResult(
                    city = city,
                    fullAddress = full.ifBlank { city }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("GeocoderDebug", "Erreur Geocoder avec locale $locale", e)
            null
        }
    }

    val result = extractAddress(Locale.FRANCE)
        ?: extractAddress(Locale.getDefault())
        ?: AddressResult(
            city = "",
            fullAddress = ""
        )

    onResult(result)
}

private fun publishAction(
    groupId: String?,
    groupName: String?,
    uri: Uri?,
    description: String,
    location: String,
    fullAddress: String,
    latitude: Double,
    longitude: Double,
    category: String,
    tags: List<String>,
    postViewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    context: Context,
    onPostSuccess: () -> Unit
) {
    uri?.let {
        postViewModel.uploadPost(
            imageUri = it,
            description = description,
            location = location,
            fullAddress = fullAddress,
            latitude = latitude,
            longitude = longitude,
            category = category,
            tags = tags,
            groupId = groupId,
            groupName = groupName
        ) {
            profileViewModel.loadUserPosts()
            Toast.makeText(context, "Post publié !", Toast.LENGTH_LONG).show()
            onPostSuccess()
        }
    }
}