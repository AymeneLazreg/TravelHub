package com.example.travelhub.features.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel() // <-- Injection du ViewModel
) {
    val context = LocalContext.current
    val userProfile = profileViewModel.userProfile

    // On pré-remplit les champs avec les données actuelles.
    // Le "remember(userProfile.nom)" permet de mettre à jour le texte dès que Firebase a chargé les données
    var nom by remember(userProfile.nom) { mutableStateOf(userProfile.nom) }
    var prenom by remember(userProfile.prenom) { mutableStateOf(userProfile.prenom) }
    var username by remember(userProfile.username) { mutableStateOf(userProfile.username) }
    var location by remember(userProfile.location) { mutableStateOf(userProfile.location) }
    var bio by remember(userProfile.bio) { mutableStateOf(userProfile.bio) }

    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // En-tête
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text("Edit Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        // Photo de profil (visuel uniquement pour l'instant)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                .clickable { Toast.makeText(context, "L'upload de photo arrive bientôt !", Toast.LENGTH_SHORT).show() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Image, contentDescription = "Upload", modifier = Modifier.size(40.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upload Photo", color = Color.DarkGray, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Champs de formulaire
        Text("Nom", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = nom, onValueChange = { nom = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Prénom", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = prenom, onValueChange = { prenom = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Nom D'utilisateur", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = username, onValueChange = { username = it.replace(" ", "") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Localisation", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = location, onValueChange = { location = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true, placeholder = { Text("Ex: Paris, France") })

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bio", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp), maxLines = 4, placeholder = { Text("Parlez de vos voyages...") })

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton de sauvegarde
        Button(
            onClick = {
                isSaving = true
                profileViewModel.updateUserProfile(
                    nom = nom.trim(),
                    prenom = prenom.trim(),
                    username = username.trim().lowercase(),
                    bio = bio.trim(),
                    location = location.trim(),
                    onSuccess = {
                        isSaving = false
                        Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                        onSaveClick() // Déclenche le retour à l'écran précédent
                    },
                    onError = { errorMessage ->
                        isSaving = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sauvegarder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}