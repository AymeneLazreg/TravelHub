package com.example.travelhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.travelhub.ui.navigation.NavGraph // NOUVEL IMPORT IMPORTANT
import com.example.travelhub.ui.theme.TravelHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // On supprime le texte temporaire et on lance le NavGraph !
                    // C'est lui qui va décider d'afficher le LoginScreen au démarrage.
                    NavGraph()
                }
            }
        }
    }
}