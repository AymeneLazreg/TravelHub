package com.example.travelhub.features.travelpath

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.util.UUID

data class Itinerary(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val price: String = "",
    val effort: String = "",
    val duration: String = "",
    val steps: List<String> = emptyList(),
    val imageUrl: String = "",
    val favorite: Boolean = false
)

class TravelPathViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyAb06vD_lTq4TCwYztB2tF66Px9lWoKBWw" // <--- N'OUBLIE PAS TA CLÉ !
    )

    var itineraries by mutableStateOf<List<Itinerary>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isSearching by mutableStateOf(false)
        private set

    var currentSearchIds by mutableStateOf<List<String>>(emptyList())
        private set

    // NOUVEAU : Un objet pour garder la trace de notre écouteur Firestore afin de l'arrêter si besoin
    private var snapshotListener: ListenerRegistration? = null

    init {
        // NOUVEAU : On écoute les changements de compte (Connexion / Déconnexion)
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Un utilisateur s'est connecté, on charge SES données
                loadMyItineraries(user.uid)
            } else {
                // Déconnexion : On vide TOUTE la mémoire pour ne rien fuiter au prochain compte !
                itineraries = emptyList()
                currentSearchIds = emptyList()
                isSearching = false
                snapshotListener?.remove() // On arrête d'écouter la base de données de l'ancien compte
            }
        }
    }

    private fun loadMyItineraries(userId: String) {
        isLoading = true
        snapshotListener?.remove() // Sécurité : on supprime l'ancien écouteur avant d'en lancer un nouveau

        snapshotListener = firestore.collection("itineraries")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { isLoading = false; return@addSnapshotListener }
                if (snapshot != null) {
                    itineraries = snapshot.documents.mapNotNull { it.toObject(Itinerary::class.java) }
                }
                isLoading = false
            }
    }

    fun generateAndSaveItineraries(
        ville: String, budget: String, duration: String, activities: List<String>, effort: String, sensitivities: List<String>,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Non connecté")

        // Nettoyage des anciens résultats non favoris
        val oldResults = itineraries.filter { !it.favorite }
        oldResults.forEach { firestore.collection("itineraries").document(it.id).delete() }

        viewModelScope.launch {
            try {
                isSearching = true

                val prompt = """
                    Expert en tourisme pour $ville. Génère 5 options d'itinéraires.
                    Critères : Budget $budget€, Durée $duration h, Activités: ${activities.joinToString(", ")}, Effort: $effort.
                    Format : 
                    TITRE: [Nom]
                    LIEUX: [Lieu A]|[Lieu B]
                    ---
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val blocks = response.text?.split("---")?.map { it.trim() }?.filter { it.length > 10 } ?: emptyList()

                val batch = firestore.batch()
                val newIds = mutableListOf<String>()

                blocks.take(5).forEach { block ->
                    var title = "Circuit $ville"
                    var steps = emptyList<String>()

                    block.lines().forEach { line ->
                        if (line.startsWith("TITRE:")) title = line.removePrefix("TITRE:").trim()
                        // NOUVEAU : On filtre immédiatement les lieux vides
                        if (line.startsWith("LIEUX:")) {
                            steps = line.removePrefix("LIEUX:").split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        }
                    }

                    // NOUVEAU : On ne sauvegarde l'itinéraire QUE s'il contient au moins un lieu !
                    if (steps.isNotEmpty()) {
                        val id = UUID.randomUUID().toString()
                        newIds.add(id)

                        val img = "https://loremflickr.com/600/400/${ville.replace(" ","")},travel?random=${UUID.randomUUID().toString().take(4)}"

                        batch.set(firestore.collection("itineraries").document(id),
                            Itinerary(id, userId, title, "~$budget€", effort, "${duration}h", steps, img, false)
                        )
                    }
                }

                // NOUVEAU : On vérifie qu'on a bien généré au moins 1 résultat valide
                if (newIds.isNotEmpty()) {
                    batch.commit().addOnSuccessListener {
                        currentSearchIds = newIds
                        onSuccess()
                    }.addOnFailureListener { onError(it.message ?: "Erreur base de données") }
                } else {
                    // Si tous les blocs étaient vides ou mal formés par l'IA
                    isSearching = false
                    onError("L'IA n'a pas réussi à trouver de lieux précis. Veuillez relancer la recherche.")
                }

            } catch (e: Exception) {
                isSearching = false
                onError("Erreur : ${e.localizedMessage}")
            }
        }
    }

    fun toggleFavorite(itinerary: Itinerary) {
        firestore.collection("itineraries").document(itinerary.id).update("favorite", !itinerary.favorite)
    }

    fun deleteItinerary(id: String) {
        firestore.collection("itineraries").document(id).delete()
    }

    fun resetToFavorites() {
        isSearching = false
        currentSearchIds = emptyList()
    }
}