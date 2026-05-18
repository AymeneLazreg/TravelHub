package com.example.travelhub.features.travelpath

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import androidx.core.net.toUri
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun ItineraryDetailScreen(
    itineraryId: String,
    onBackClick: () -> Unit,
    travelPathViewModel: TravelPathViewModel = viewModel()
) {
    val context = LocalContext.current

    // On cherche l'itinéraire cliqué dans la liste
    val itinerary = travelPathViewModel.itineraries.find { it.id == itineraryId }

    if (itinerary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // Un conteneur Box englobe tout pour pouvoir superposer le bouton PDF en bas à droite
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // En-tête avec image
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                AsyncImage(
                    model = itinerary.imageUrl,
                    contentDescription = itinerary.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Bouton retour par-dessus l'image
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }
            }

            // Contenu
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 80.dp)) { // Ajout de padding bas pour ne pas masquer le bouton
                Text(text = itinerary.title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Badges d'infos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(onClick = {}, label = { Text(itinerary.price) })
                    SuggestionChip(onClick = {}, label = { Text(itinerary.duration) })
                    SuggestionChip(onClick = {}, label = { Text(itinerary.effort) })
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Affichage de la description générée par l'IA
                if (itinerary.description.isNotEmpty()) {
                    Text(
                        text = itinerary.description,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text("Votre Parcours", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))

                // Liste des étapes avec icônes cliquables
                itinerary.steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = step, fontSize = 16.sp, modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                val cityName = itinerary.title.substringBefore(" :")
                                val searchQuery = Uri.encode("$step, $cityName")
                                val mapUri = "geo:0,0?q=$searchQuery".toUri()
                                val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                context.startActivity(mapIntent)
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Voir sur la carte", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton pour tracer l'itinéeraire complet
                if (itinerary.steps.size >= 2) {
                    Button(
                        onClick = {
                            val cityName = itinerary.title.substringBefore(" :")
                            val origin = Uri.encode("${itinerary.steps.first()}, $cityName")
                            val destination = Uri.encode("${itinerary.steps.last()}, $cityName")

                            val waypointsList = itinerary.steps.drop(1).dropLast(1)
                            val waypointsString = waypointsList.joinToString("|") { Uri.encode("$it, $cityName") }
                            val waypointsParam = if (waypointsString.isNotEmpty()) "&waypoints=$waypointsString" else ""

                            val routeUri = "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination$waypointsParam&travelmode=walking".toUri()
                            val routeIntent = Intent(Intent.ACTION_VIEW, routeUri)
                            context.startActivity(routeIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tracer le parcours complet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // --- NOUVEAU : BOUTON SAUVEGARDER EN PDF (En bas à droite) ---
        Button(
            onClick = { exportItineraryToPdf(context, itinerary) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Rouge PDF
        ) {
            Text("Sauvegarder en PDF", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// --- FONCTION EN BACKEND POUR GENERER LE PDF VIA L'IMPRESSION ANDROID NATIVE ---
fun exportItineraryToPdf(context: Context, itinerary: Itinerary) {
    val webView = WebView(context)
    val htmlContent = """
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: sans-serif; padding: 30px; color: #212121; }
                h1 { color: #212121; font-size: 26px; border-bottom: 2px solid #212121; padding-bottom: 12px; margin-bottom: 8px; }
                .badge-container { margin-bottom: 20px; }
                .badge { display: inline-block; background: #F5F5F5; padding: 6px 12px; margin-right: 8px; border-radius: 4px; font-size: 14px; font-weight: bold; }
                .description { margin: 25px 0; font-size: 16px; color: #424242; line-height: 1.6; font-style: italic; }
                h2 { color: #212121; font-size: 20px; margin-top: 30px; margin-bottom: 15px; }
                .step { margin-bottom: 16px; font-size: 16px; display: block; }
                .step-number { font-weight: bold; margin-right: 12px; color: #D32F2F; }
            </style>
        </head>
        <body>
            <h1>${itinerary.title}</h1>
            <div class="badge-container">
                <span class="badge">Budget : ${itinerary.price}</span>
                <span class="badge">Durée : ${itinerary.duration}</span>
                <span class="badge">Effort : ${itinerary.effort}</span>
            </div>
            <div class="description">${itinerary.description}</div>
            <h2>Etapes du parcours</h2>
            ${itinerary.steps.mapIndexed { index, step ->
        """<div class="step"><span class="step-number">Étape ${index + 1} :</span> $step</div>"""
    }.joinToString("")}
        </body>
        </html>
    """.trimIndent()

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("TravelHub - ${itinerary.title}")
            printManager.print(
                "TravelHub - ${itinerary.title}",
                printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}