package com.example.travelhub.features.profile

import android.net.Uri
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

data class UserProfile(
    val nom: String = "",
    val prenom: String = "",
    val username: String = "",
    val bio: String = "",
    val location: String = "",
    val photoUrl: String = "",
    val interests: List<String> = emptyList(),
    val favorites: List<String> = emptyList() // Ajout du champ favoris
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var userProfile by mutableStateOf(UserProfile())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var userPosts by mutableStateOf<List<Post>>(emptyList())
        private set

    // --- NOUVEAU : Liste des posts favoris réels ---
    var favoritePosts by mutableStateOf<List<Post>>(emptyList())
        private set

    init {
        loadUserProfile()
        loadUserPosts()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) return@addSnapshotListener
                if (document != null && document.exists()) {
                    val favs = (document.get("favorites") as? List<String>) ?: emptyList()
                    userProfile = UserProfile(
                        nom = document.getString("nom") ?: "",
                        prenom = document.getString("prenom") ?: "",
                        username = document.getString("username") ?: "",
                        bio = document.getString("bio") ?: "",
                        location = document.getString("location") ?: "",
                        photoUrl = document.getString("photoUrl") ?: "",
                        interests = (document.get("interests") as? List<String>) ?: emptyList(),
                        favorites = favs
                    )
                    // Dès que le profil change, on recharge les favoris réels
                    loadFavoritePosts(favs)
                }
                isLoading = false
            }
    }

    private fun loadUserPosts() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                userPosts = snapshot?.toObjects(Post::class.java) ?: emptyList()
            }
    }

    // --- NOUVELLE FONCTION : Charger les posts depuis les IDs favoris ---
    private fun loadFavoritePosts(favoriteIds: List<String>) {
        if (favoriteIds.isEmpty()) {
            favoritePosts = emptyList()
            return
        }

        // Firestore limite whereIn à 10 ou 30 IDs selon la version, suffisant ici
        firestore.collection("posts")
            .whereIn(FieldPath.documentId(), favoriteIds)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                favoritePosts = snapshot?.toObjects(Post::class.java) ?: emptyList()
            }
    }

    fun updateUserProfile(nom: String, prenom: String, username: String, bio: String, location: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>("nom" to nom, "prenom" to prenom, "username" to username, "bio" to bio, "location" to location)
        firestore.collection("users").document(userId).update(updates).addOnSuccessListener { onSuccess() }.addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur") }
    }

    fun uploadProfileImage(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                firestore.collection("users").document(userId).update("photoUrl", downloadUrl.toString()).addOnSuccessListener { onSuccess() }.addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur") }
            }
        }.addOnFailureListener { e -> onError(e.localizedMessage ?: "Erreur") }
    }
}