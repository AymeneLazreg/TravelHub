package com.example.travelhub.features.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelhub.utils.AppConfig

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onAnonymousClick: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Traveling",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Validation du TP
                if (login.length > AppConfig.MAX_LOGIN_LENGTH || login.isEmpty()) {
                    Toast.makeText(context, "Login invalide (max ${AppConfig.MAX_LOGIN_LENGTH} char)", Toast.LENGTH_SHORT).show()
                } else if (password.length < AppConfig.MIN_PASSWORD_LENGTH) {
                    Toast.makeText(context, "Mot de passe trop court", Toast.LENGTH_SHORT).show()
                } else {
                    // Simulation succès
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Se connecter")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("Pas de compte ? S'inscrire")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onAnonymousClick) {
            Text("Continuer en mode anonyme", color = MaterialTheme.colorScheme.secondary)
        }
    }
}