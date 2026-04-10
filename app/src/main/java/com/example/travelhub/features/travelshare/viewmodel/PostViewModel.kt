package com.example.travelhub.features.travelshare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.travelshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    /**
     * Enregistre une photo dans Storage et les infos dans Firestore.
     */
    fun uploadPost(
        imageUri: Uri,
        description: String,
        location: String,
        tags: List<String>,
        onComplete: () -> Unit
    ) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _isUploading.value = true
            try {
                // 1. Upload de l'image vers Firebase Storage
                val fileName = "posts/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await()

                // 2. Récupération des infos de l'utilisateur actuel
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val username = userDoc.getString("username") ?: "Aventurier"
                val userProfileUrl = userDoc.getString("photoUrl") ?: ""

                // 3. Création de l'objet Post
                val postId = firestore.collection("posts").document().id
                val newPost = Post(
                    id = postId,
                    userId = currentUser.uid,
                    username = username,
                    userProfileUrl = userProfileUrl,
                    imageUrl = downloadUrl.toString(),
                    description = description,
                    locationName = location,
                    tags = tags
                )

                // 4. Envoi vers Firestore
                firestore.collection("posts").document(postId).set(newPost).await()

                _isUploading.value = false
                onComplete()
            } catch (e: Exception) {
                _isUploading.value = false
                // On pourrait ajouter une gestion d'erreur ici
            }
        }
    }
}