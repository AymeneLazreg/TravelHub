package com.example.travelhub.features.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("John Doe") }
    var username by remember { mutableStateOf("johndoe") }
    var location by remember { mutableStateOf("Montpellier, France") }
    var bio by remember { mutableStateOf("Voyageur passionné 🌍📸") }

    val context = LocalContext.current

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
                text = "Edit Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                .clickable { Toast.makeText(context, "Changer la photo...", Toast.LENGTH_SHORT).show() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(40.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upload Photo", color = Color.DarkGray, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Nom Complet", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Nom D'utilisateur", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Location", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = location, onValueChange = { location = it }, modifier = Modifier.fillMaxWidth(), trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select location") }, shape = RoundedCornerShape(8.dp), readOnly = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bio", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp), maxLines = 4)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Profil sauvegardé !", Toast.LENGTH_SHORT).show()
                onSaveClick() // Ferme l'écran
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
        ) {
            Text("Sauvegarder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}