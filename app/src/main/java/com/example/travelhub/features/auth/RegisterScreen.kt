package com.example.travelhub.features.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") } // <-- NOUVELLE VARIABLE

    var isLoading by remember { mutableStateOf(false) }

    val interestsList = listOf("Sport", "Musique", "Lecture", "Voyage", "Art", "Photographie", "Nature")
    val selectedInterests = remember { mutableStateListOf<String>() }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Créer un compte", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // NOUVEAU CHAMP : Nom d'utilisateur (sans espaces)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it.replace(" ", "") }, // On empêche les espaces
            label = { Text("Nom d'utilisateur (ex: traveler99)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe (6 carac. min)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Centres d'intérêt", fontWeight = FontWeight.SemiBold)

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(interestsList) { interest ->
                FilterChip(
                    selected = selectedInterests.contains(interest),
                    onClick = {
                        if (selectedInterests.contains(interest)) selectedInterests.remove(interest)
                        else selectedInterests.add(interest)
                    },
                    label = { Text(interest) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // On vérifie que le pseudo est aussi rempli
                if (email.isNotEmpty() && password.length >= 6 && nom.isNotEmpty() && prenom.isNotEmpty() && username.isNotEmpty()) {
                    isLoading = true

                    authViewModel.registerUser(
                        email = email.trim(),
                        pass = password.trim(),
                        nom = nom.trim(),
                        prenom = prenom.trim(),
                        username = username.lowercase().trim(), // On force en minuscules pour la base de données
                        interests = selectedInterests.toList(),
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Bienvenue @$username !", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        },
                        onError = { errorMessage ->
                            isLoading = false
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Veuillez remplir tous les champs correctement", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("S'inscrire")
            }
        }

        TextButton(onClick = onBackToLogin, enabled = !isLoading) {
            Text("Déjà un compte ? Connexion")
        }
    }
}