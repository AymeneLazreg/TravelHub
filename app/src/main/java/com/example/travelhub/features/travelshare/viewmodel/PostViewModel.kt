package com.example.travelhub.features.travelshare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.profile.UserProfile
import com.example.travelhub.features.travelshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
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

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    // --- NOUVEAU : Détails des gens qui ont liké ---
    private val _likersDetails = MutableStateFlow<List<UserProfile>>(emptyList())
    val likersDetails: StateFlow<List<UserProfile>> = _likersDetails

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val postList = snapshot?.toObjects(Post::class.java) ?: emptyList()
                _posts.value = postList
            }
    }

    fun fetchLikersDetails(userIds: List<String>) {
        if (userIds.isEmpty()) {
            _likersDetails.value = emptyList()
            return
        }
        // Récupère les profils dont l'ID est dans la liste likedBy
        firestore.collection("users")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), userIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(UserProfile::class.java)
                _likersDetails.value = users
            }
    }

    fun uploadPost(imageUri: Uri, description: String, location: String, tags: List<String>, onComplete: () -> Unit) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val fileName = "posts/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await()

                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val username = userDoc.getString("username") ?: "Aventurier"
                val userProfileUrl = userDoc.getString("photoUrl") ?: ""

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
                firestore.collection("posts").document(postId).set(newPost).await()
                _isUploading.value = false
                onComplete()
            } catch (e: Exception) {
                _isUploading.value = false
            }
        }
    }

    fun toggleLike(post: Post) {
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(post.id)

        if (post.likedBy.contains(currentUser.uid)) {
            postRef.update(
                "likesCount", FieldValue.increment(-1L),
                "likedBy", FieldValue.arrayRemove(currentUser.uid)
            )
        } else {
            postRef.update(
                "likesCount", FieldValue.increment(1L),
                "likedBy", FieldValue.arrayUnion(currentUser.uid)
            )
        }
    }
}