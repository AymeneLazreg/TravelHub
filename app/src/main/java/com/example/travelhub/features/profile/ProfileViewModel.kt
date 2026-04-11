package com.example.travelhub.features.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.travelhub.features.travelshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldPath
import com.google.firebase.storage.FirebaseStorage

// Data class alignée sur Firestore
data class UserProfile(
    val nom: String = "",
    val prenom: String = "",
    val username: String = "",
    val bio: String = "",
    val location: String = "",
    val photoUrl: String = "",
    val interests: List<String> = emptyList(),
    val favorites: List<String> = emptyList()
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- ÉTATS OBSERVABLES ---
    var userProfile by mutableStateOf(UserProfile())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var userPosts by mutableStateOf<List<Post>>(emptyList())
        private set

    var favoritePosts by mutableStateOf<List<Post>>(emptyList())
        private set

    init {
        refreshAllData()
    }

    /**
     * Nettoie les données actuelles et recharge tout pour l'utilisateur
     * qui vient de se connecter.
     */
    fun refreshAllData() {
        isLoading = true
        // Important : on vide pour ne pas afficher brièvement les données du compte précédent
        userProfile = UserProfile()
        userPosts = emptyList()
        favoritePosts = emptyList()

        loadUserProfile()
        loadUserPosts()
    }

    // --- CHARGEMENT DU PROFIL (TEMPS RÉEL) ---
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: run {
            isLoading = false
            return
        }

        firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("ProfileViewModel", "Erreur Profil: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    if (profile != null) {
                        userProfile = profile
                        loadFavoritePosts(profile.favorites)
                    }
                }
                isLoading = false
            }
    }

    // --- CHARGEMENT DES POSTS DE L'UTILISATEUR ---
    fun loadUserPosts() {
        val userId = auth.currentUser?.uid ?: run {
            isLoading = false
            return
        }

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                // Dès qu'on reçoit une réponse (même erreur d'index), on arrête le chargement
                isLoading = false

                if (e != null) {
                    Log.e("ProfileViewModel", "Erreur écoute: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    userPosts = snapshot.toObjects(Post::class.java).toList()
                    Log.d("ProfileViewModel", "Posts synchronisés pour $userId : ${userPosts.size}")
                }
            }
    }

    // --- CHARGEMENT DES POSTS FAVORIS ---
    private fun loadFavoritePosts(favoriteIds: List<String>) {
        if (favoriteIds.isEmpty()) {
            favoritePosts = emptyList()
            return
        }

        firestore.collection("posts")
            .whereIn(FieldPath.documentId(), favoriteIds)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ProfileViewModel", "Erreur favoris: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    favoritePosts = snapshot.toObjects(Post::class.java).toList()
                }
            }
    }

    // --- MISE À JOUR DU PROFIL ---
    fun updateUserProfile(
        nom: String,
        prenom: String,
        username: String,
        bio: String,
        location: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "nom" to nom,
            "prenom" to prenom,
            "username" to username,
            "bio" to bio,
            "location" to location
        )

        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur") }
    }

    // --- UPLOAD IMAGE DE PROFIL ---
    fun uploadProfileImage(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_images/$userId.jpg")

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                firestore.collection("users").document(userId)
                    .update("photoUrl", downloadUrl.toString())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur Firestore") }
            }
        }.addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur Storage") }
    }

    fun removePostLocally(postId: String) {
        userPosts = userPosts.filter { it.id != postId }
    }
}