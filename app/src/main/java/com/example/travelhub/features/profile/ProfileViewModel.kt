package com.example.travelhub.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val nom: String = "",
    val prenom: String = "",
    val username: String = "",
    val bio: String = "",
    val location: String = "",
    val interests: List<String> = emptyList()
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var userProfile by mutableStateOf(UserProfile())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            isLoading = true

            // LA CORRECTION EST ICI : On "écoute" en temps réel !
            firestore.collection("users").document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        userProfile = UserProfile(
                            nom = document.getString("nom") ?: "",
                            prenom = document.getString("prenom") ?: "",
                            username = document.getString("username") ?: "",
                            bio = document.getString("bio") ?: "",
                            location = document.getString("location") ?: "",
                            interests = (document.get("interests") as? List<String>) ?: emptyList()
                        )
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    fun updateUserProfile(
        nom: String,
        prenom: String,
        username: String,
        bio: String,
        location: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val updates = hashMapOf<String, Any>(
                "nom" to nom,
                "prenom" to prenom,
                "username" to username,
                "bio" to bio,
                "location" to location
            )

            firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    // Plus besoin de modifier les données manuellement ici,
                    // le "addSnapshotListener" du dessus s'en charge instantanément !
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Erreur lors de la mise à jour")
                }
        } else {
            onError("Utilisateur non connecté")
        }
    }
}