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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
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
        apiKey = ""
    )

    var itineraries by mutableStateOf<List<Itinerary>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isSearching by mutableStateOf(false)
        private set

    var currentSearchIds by mutableStateOf<List<String>>(emptyList())
        private set

    var citySuggestions by mutableStateOf<List<String>>(emptyList())
        private set

    private var snapshotListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                loadMyItineraries(user.uid)
            } else {
                itineraries = emptyList()
                currentSearchIds = emptyList()
                isSearching = false
                citySuggestions = emptyList()
                snapshotListener?.remove()
            }
        }
    }

    private fun loadMyItineraries(userId: String) {
        isLoading = true
        snapshotListener?.remove()

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

    fun searchCity(query: String) {
        if (query.length < 2) {
            citySuggestions = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val safeQuery = query.replace(" ", "+")
                val url = URL("https://photon.komoot.io/api/?q=$safeQuery&layer=city&limit=5")

                val jsonString = url.readText()
                val jsonObject = JSONObject(jsonString)
                val features = jsonObject.getJSONArray("features")

                val results = mutableListOf<String>()

                for (i in 0 until features.length()) {
                    val properties = features.getJSONObject(i).getJSONObject("properties")
                    val name = properties.optString("name", "")
                    val country = properties.optString("country", "")

                    if (name.isNotEmpty()) {
                        val displayName = if (country.isNotEmpty()) "$name ($country)" else name
                        if (!results.contains(displayName)) {
                            results.add(displayName)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    citySuggestions = results
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    citySuggestions = emptyList()
                }
            }
        }
    }

    fun generateAndSaveItineraries(
        location: String,
        budget: String, duration: String, activities: List<String>, effort: String, sensitivities: List<String>,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Non connecté")

        val oldResults = itineraries.filter { !it.favorite }
        oldResults.forEach { firestore.collection("itineraries").document(it.id).delete() }

        viewModelScope.launch {
            try {
                isSearching = true

                // Calcul du nombre de lieux minimum selon la durée
                val durationInt = duration.toIntOrNull() ?: 2
                val minPlaces = when {
                    durationInt <= 2 -> 2
                    durationInt in 3..4 -> 3
                    durationInt in 5..8 -> 5
                    durationInt in 9..10 -> 7
                    else -> 8
                }

                // --- TON PROMPT MAJ ---
                val prompt = """
                    Tu es un guide local expert et factuel de la destination suivante : $location.

                    OBJECTIF :
                    Générer EXACTEMENT 4 itinéraires touristiques DISTINCTS et réalistes pour visiter ce lieu.

                    CRITÈRES À RESPECTER IMPÉRATIVEMENT :
                    - Budget total maximum par circuit : $budget€
                    - Durée totale estimée : $duration heures
                    - Thèmes souhaités : ${activities.joinToString(", ")}
                    - Niveau d’effort physique : $effort
                    - Contraintes météo : ${sensitivities.joinToString(", ")}

                    RÈGLES CRITIQUES (ANTI-HALLUCINATION) :
                    1. FIABILITÉ ABSOLUE :
                       - Utilise uniquement des lieux réels, connus et vérifiables à $location.
                       - INTERDICTION totale d’inventer des noms de lieux, musées, restaurants ou attractions.
                       - Si tu n’es pas sûr qu’un lieu existe, NE L’UTILISE PAS.

                    2. STRUCTURE STRICTE :
                       - Tu dois générer EXACTEMENT 4 circuits (ni plus, ni moins).
                       - Chaque circuit doit contenir AU MOINS $minPlaces lieux différents.
                       - Aucun lieu ne doit apparaître dans plusieurs circuits (unicité globale stricte).

                    3. COHÉRENCE :
                       - Chaque circuit doit être logique géographiquement (éviter les trajets incohérents).
                       - Les lieux doivent être compatibles entre eux en termes de proximité.

                    4. QUALITÉ :
                       - Les circuits doivent être différents en ambiance et expérience (culture, nature, détente, food, etc.).
                       - Adapter les choix aux contraintes météo et effort.

                    FORMAT DE SORTIE OBLIGATOIRE (STRICT, AUCUN TEXTE EN PLUS) :

                    CIRCUIT 1
                    TITRE: ...
                    LIEUX: lieu1 | lieu2 | lieu3 | ...

                    ---
                    CIRCUIT 2
                    TITRE: ...
                    LIEUX: lieu1 | lieu2 | lieu3 | ...

                    ---
                    CIRCUIT 3
                    TITRE: ...
                    LIEUX: lieu1 | lieu2 | lieu3 | ...

                    ---
                    CIRCUIT 4
                    TITRE: ...
                    LIEUX: lieu1 | lieu2 | lieu3 | ...

                    IMPORTANT FINAL :
                    - Aucun commentaire, aucune explication, aucun texte hors format.
                    - Respect strict du format sinon réponse invalide.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)

                val blocks = response.text?.split("---")?.map { it.trim() }?.filter { it.length > 10 } ?: emptyList()

                val batch = firestore.batch()
                val newIds = mutableListOf<String>()

                // Nettoyage pour l'URL d'image
                val imgSearchTag = location.replace(" ", "").replace("(", "").replace(")", "").split(",")[0]

                blocks.take(5).forEachIndexed { index, block ->
                    var title = "Circuit à $location"
                    var steps = emptyList<String>()

                    block.lines().forEach { line ->
                        if (line.startsWith("TITRE:")) title = line.removePrefix("TITRE:").trim()
                        if (line.startsWith("LIEUX:")) {
                            steps = line.removePrefix("LIEUX:").split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        }
                    }

                    if (steps.size >= 2) {
                        val id = UUID.randomUUID().toString()
                        newIds.add(id)

                        // --- LA CORRECTION FINALE IMAGE EST ICI ---
                        // On crée un identifiant UNIQUE perçu par le serveur pour CHAQUE image.
                        // System.currentTimeMillis() garantit que ça change à chaque recherche.
                        // (100..999).random() et index garantissent que les 5 sont différentes entre elles.
                        val forceNewImageId = System.currentTimeMillis() + (100..999).random() + index

                        // On utilise 'random' au lieu de 'lock' pour forcer le cache busting.
                        // On simplifie les tags pour plus de diversité de la ville.
                        val img = "https://loremflickr.com/600/400/$imgSearchTag,travel?random=$forceNewImageId"

                        batch.set(firestore.collection("itineraries").document(id),
                            Itinerary(id, userId, title, "~$budget€", effort, "${duration}h", steps, img, false)
                        )
                    }
                }

                if (newIds.size >= 3) {
                    batch.commit().addOnSuccessListener {
                        currentSearchIds = newIds
                        onSuccess()
                    }.addOnFailureListener { onError(it.message ?: "Erreur base de données") }
                } else {
                    isSearching = false
                    onError("L'IA n'a pas réussi à générer assez d'itinéraires fiables pour $location. Modifiez légèrement la durée ou le budget et réessayez.")
                }

            } catch (e: Exception) {
                isSearching = false
                onError("Erreur de génération : ${e.localizedMessage}")
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