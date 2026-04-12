package com.example.travelhub.features.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.travelhub.features.travelshare.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldPath

class OtherProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    var userProfile by mutableStateOf(UserProfile())
    var userPosts by mutableStateOf<List<Post>>(emptyList())
    var favoritePosts by mutableStateOf<List<Post>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun loadUser(userId: String) {
        if (userId.isEmpty()) {
            Log.e("OtherProfile", "L'ID utilisateur est vide !")
            return
        }

        isLoading = true
        Log.d("OtherProfile", "Chargement du profil pour l'ID : $userId")

        // 1. Charger les infos du profil
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = doc.toObject(UserProfile::class.java)
                    if (profile != null) {
                        userProfile = profile
                        Log.d("OtherProfile", "Profil trouvé : ${profile.username}")

                        // 2. Charger les posts et les favoris une fois le profil récupéré
                        loadPosts(userId)
                        loadFavoritePosts(profile.favorites)
                    }
                } else {
                    Log.e("OtherProfile", "Document utilisateur inexistant dans Firestore")
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("OtherProfile", "Erreur Firestore Profil: ${e.message}")
                isLoading = false
            }
    }

    private fun loadPosts(userId: String) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            // Note : Si ça ne s'affiche toujours pas, commente la ligne orderBy temporairement
            // Car un index Firestore manquant peut bloquer la requête
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                userPosts = snapshot.toObjects(Post::class.java)
                Log.d("OtherProfile", "${userPosts.size} posts récupérés")
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("OtherProfile", "Erreur Firestore Posts: ${e.message}")
                isLoading = false
            }
    }

    private fun loadFavoritePosts(favoriteIds: List<String>) {
        if (favoriteIds.isEmpty()) {
            favoritePosts = emptyList()
            return
        }

        firestore.collection("posts")
            .whereIn(FieldPath.documentId(), favoriteIds)
            .get()
            .addOnSuccessListener { snapshot ->
                favoritePosts = snapshot.toObjects(Post::class.java)
                Log.d("OtherProfile", "${favoritePosts.size} favoris récupérés")
            }
            .addOnFailureListener { e ->
                Log.e("OtherProfile", "Erreur Firestore Favoris: ${e.message}")
            }
    }
}