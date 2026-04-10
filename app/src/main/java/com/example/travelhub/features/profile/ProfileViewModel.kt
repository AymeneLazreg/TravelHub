package com.example.travelhub.features.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class UserProfile(
    val nom: String = "",
    val prenom: String = "",
    val username: String = "",
    val bio: String = "",
    val location: String = "",
    val photoUrl: String = "", // <-- NOUVEAU : l'URL de la photo
    val interests: List<String> = emptyList()
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance() // <-- NOUVEAU : Firebase Storage

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
                            photoUrl = document.getString("photoUrl") ?: "", // <-- On récupère l'URL
                            interests = (document.get("interests") as? List<String>) ?: emptyList()
                        )
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Sauvegarde classique du texte
    fun updateUserProfile(
        nom: String, prenom: String, username: String, bio: String, location: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val updates = hashMapOf<String, Any>(
                "nom" to nom, "prenom" to prenom, "username" to username, "bio" to bio, "location" to location
            )
            firestore.collection("users").document(userId).update(updates)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur lors de la mise à jour") }
        } else {
            onError("Utilisateur non connecté")
        }
    }

    /**
     * NOUVELLE FONCTION : Envoie la photo dans Storage, puis sauvegarde son lien dans Firestore
     */
    fun uploadProfileImage(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        // On crée un chemin dans le "disque dur" Firebase
        val storageRef = storage.reference.child("profile_images/$userId.jpg")

        // 1. On envoie le fichier
        storageRef.putFile(uri)
            .addOnSuccessListener {
                // 2. Si succès, on demande le lien internet public de cette image
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // 3. On sauvegarde ce lien dans le document Firestore de l'utilisateur
                    firestore.collection("users").document(userId)
                        .update("photoUrl", downloadUrl.toString())
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur base de données") }
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Erreur lors de l'envoi de l'image")
            }
    }
}