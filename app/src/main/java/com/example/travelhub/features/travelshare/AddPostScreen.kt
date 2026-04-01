package com.example.travelhub.features.travelshare

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowDropDown

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddPostScreen() {
    var location by remember { mutableStateOf("France") }
    var description by remember { mutableStateOf("") }

    // Gestion du menu déroulant pour le bouton Publier
    var expandedMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Les tags de votre maquette
    val availableTags = listOf("Nature", "Sunset", "Greece", "City", "Museum")
    val selectedTags = remember { mutableStateListOf("Nature", "Sunset", "Greece") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()) // Pour pouvoir faire défiler si le clavier est ouvert
    ) {
        // En-tête
        Text(
            text = "Upload a travel moment",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Zone d'upload d'image (Placeholder fidèle à votre Figma)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                .clickable {
                    Toast.makeText(context, "Ouverture de la galerie photo...", Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(48.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upload Photo", color = Color.DarkGray, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Champ Localisation (Simulé comme un menu déroulant pour l'instant)
        Text("Location", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select location") },
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ Description avec l'icône micro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tags avec LazyRow (Correction du crash)
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
                    label = { Text(tag) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton Publier avec menu déroulant
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expandedMenu = true }, // Ouvre le sous-menu au clic
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
            ) {
                Text("Publish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Le menu déroulant (Public / Groupe)
            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                modifier = Modifier.fillMaxWidth(0.8f) // Prend 80% de la largeur
            ) {
                DropdownMenuItem(
                    text = { Text("Publier en public") },
                    onClick = {
                        expandedMenu = false
                        Toast.makeText(context, "Photo publiée publiquement !", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Publier dans un groupe") },
                    onClick = {
                        expandedMenu = false
                        Toast.makeText(context, "Choix du groupe à venir...", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Espace supplémentaire pour la BottomBar
        Spacer(modifier = Modifier.height(80.dp))
    }
}