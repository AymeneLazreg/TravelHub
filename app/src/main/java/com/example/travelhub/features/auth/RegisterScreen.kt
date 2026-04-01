package com.example.travelhub.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    // États pour les champs (TP3)
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }

    // Centres d'intérêt
    val interestsList = listOf("Sport", "Musique", "Lecture", "Voyage", "Art")
    val selectedInterests = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()) // Permet de scroller si le clavier cache des champs
    ) {
        Text("Créer un compte", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mot de passe") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        Text("Centres d'intérêt", fontWeight = FontWeight.SemiBold)

        // Affichage des puces (Chips) pour les intérêts
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interestsList.forEach { interest ->
                FilterChip(
                    selected = selectedInterests.contains(interest),
                    onClick = {
                        if (selectedInterests.contains(interest)) selectedInterests.remove(interest)
                        else selectedInterests.add(interest)
                    },
                    label = { Text(interest) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRegisterSuccess,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Soumettre")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Déjà un compte ? Connexion")
        }
    }
}