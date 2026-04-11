package com.example.travelhub.features.travelshare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.profile.UserProfile
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    // --- RECHERCHE ---
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val filteredPosts: StateFlow<List<Post>> = combine(_posts, _searchQuery, _selectedCategory) { posts, query, category ->
        posts.filter { post ->
            val matchesQuery = query.isBlank() ||
                    post.description.contains(query, ignoreCase = true) ||
                    post.locationName.contains(query, ignoreCase = true) ||
                    post.username.contains(query, ignoreCase = true) ||
                    post.tags.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = category == null || post.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- LIKES & COMMENTS STATES ---
    private val _likersDetails = MutableStateFlow<List<UserProfile>>(emptyList())
    val likersDetails: StateFlow<List<UserProfile>> = _likersDetails

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    init { fetchPosts() }

    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val postList = snapshot?.toObjects(Post::class.java) ?: emptyList()
                _posts.value = postList
            }
    }

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }
    fun onCategorySelected(category: String?) { _selectedCategory.value = category }

    // --- GESTION DES LIKES ---
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

    fun fetchLikersDetails(userIds: List<String>) {
        if (userIds.isEmpty()) {
            _likersDetails.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), userIds)
                    .get()
                    .await()
                _likersDetails.value = snapshot.toObjects(UserProfile::class.java)
            } catch (e: Exception) {
                _likersDetails.value = emptyList()
            }
        }
    }

    // --- GESTION DES COMMENTAIRES ---
    fun addComment(postId: String, text: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val newComment = Comment(
                    userId = currentUser.uid,
                    username = userDoc.getString("username") ?: "Aventurier",
                    userProfileUrl = userDoc.getString("photoUrl") ?: "",
                    text = text,
                    timestamp = com.google.firebase.Timestamp.now()
                )
                firestore.collection("posts").document(postId)
                    .update("comments", FieldValue.arrayUnion(newComment)).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteComment(postId: String, comment: Comment) {
        viewModelScope.launch {
            try {
                firestore.collection("posts").document(postId)
                    .update("comments", FieldValue.arrayRemove(comment)).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- UPLOAD ---
    fun uploadPost(imageUri: Uri, description: String, location: String, category: String, tags: List<String>, onComplete: () -> Unit) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val fileName = "posts/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await()

                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()

                val postId = firestore.collection("posts").document().id
                val newPost = Post(
                    id = postId,
                    userId = currentUser.uid,
                    username = userDoc.getString("username") ?: "Aventurier",
                    userProfileUrl = userDoc.getString("photoUrl") ?: "",
                    imageUrl = downloadUrl.toString(),
                    description = description,
                    locationName = location,
                    category = category,
                    tags = tags,
                    timestamp = com.google.firebase.Timestamp.now()
                )

                firestore.collection("posts").document(postId).set(newPost).await()
                _isUploading.value = false
                onComplete()
            } catch (e: Exception) {
                _isUploading.value = false
            }
        }
    }
}