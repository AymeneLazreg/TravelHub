package com.example.travelhub.features.travelshare.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.profile.UserProfile
import com.example.travelhub.features.travelshare.model.Comment
import com.example.travelhub.features.travelshare.model.Notification
import com.example.travelhub.features.travelshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class PostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedDate = MutableStateFlow<Long?>(null)

    var selectedPostIdFromNotif by mutableStateOf<String?>(null)
    var shouldOpenCommentsFromNotif by mutableStateOf<Boolean>(false)

    fun clearNavigationRequest() {
        selectedPostIdFromNotif = null
        shouldOpenCommentsFromNotif = false
    }

    val filteredPosts: StateFlow<List<Post>> = combine(
        _posts, _searchQuery, _selectedCategory, _selectedDate
    ) { posts, query, category, dateMillis ->
        val cleanQuery = query.trim().lowercase()

        posts.filter { post ->
            val matchesQuery = cleanQuery.isBlank() ||
                    post.username.lowercase().contains(cleanQuery) ||
                    post.locationName.lowercase().contains(cleanQuery) ||
                    post.description.lowercase().contains(cleanQuery) ||
                    post.tags.any { it.lowercase().contains(cleanQuery) }

            val matchesCategory = category == null || post.category == category

            val matchesDate = if (dateMillis == null) true else {
                val postDate = post.timestamp.toDate()
                val selectedDate = Date(dateMillis)
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                sdf.format(postDate) == sdf.format(selectedDate)
            }
            matchesQuery && matchesCategory && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
                _posts.value = snapshot?.toObjects(Post::class.java) ?: emptyList()
            }
    }

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }
    fun onCategorySelected(category: String?) { _selectedCategory.value = category }
    fun onDateSelected(date: Long?) { _selectedDate.value = date }

    fun toggleLike(post: Post) {
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(post.id)
        if (post.likedBy.contains(currentUser.uid)) {
            postRef.update("likesCount", FieldValue.increment(-1L), "likedBy", FieldValue.arrayRemove(currentUser.uid))
        } else {
            postRef.update("likesCount", FieldValue.increment(1L), "likedBy", FieldValue.arrayUnion(currentUser.uid))
            sendNotification(post, "LIKE")
        }
    }

    fun fetchLikersDetails(userIds: List<String>) {
        if (userIds.isEmpty()) { _likersDetails.value = emptyList(); return }
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").whereIn(FieldPath.documentId(), userIds).get().await()
                _likersDetails.value = snapshot.toObjects(UserProfile::class.java)
            } catch (e: Exception) { _likersDetails.value = emptyList() }
        }
    }

    fun toggleFavorite(postId: String) {
        val currentUser = auth.currentUser ?: return
        val userRef = firestore.collection("users").document(currentUser.uid)

        viewModelScope.launch {
            try {
                val snapshot = userRef.get().await()
                val favorites = (snapshot.get("favorites") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                if (favorites.contains(postId)) {
                    userRef.update("favorites", FieldValue.arrayRemove(postId)).await()
                } else {
                    userRef.update("favorites", FieldValue.arrayUnion(postId)).await()
                    val postDoc = firestore.collection("posts").document(postId).get().await()
                    val post = postDoc.toObject(Post::class.java)
                    if (post != null) {
                        sendNotification(post, "FAVORITE")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addComment(post: Post, text: String) {
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
                firestore.collection("posts").document(post.id)
                    .update("comments", FieldValue.arrayUnion(newComment)).await()

                sendNotification(post, "COMMENT", text)
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

    // --- UPLOAD MODIFIÉ AVEC GROUP ID ---
    fun uploadPost(
        imageUri: Uri,
        description: String,
        location: String,
        category: String,
        tags: List<String>,
        groupId: String? = null, // Paramètre ajouté
        onComplete: () -> Unit
    ) {
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

                // On prépare l'objet avec le groupId (sera null si public)
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
                    timestamp = com.google.firebase.Timestamp.now(),
                    // Si ta data class Post ne contient pas encore ce champ,
                    // assure-toi de l'ajouter dans ton fichier Post.kt
                )

                // On utilise un Map pour être sûr que Firestore enregistre le champ groupId
                val postData = hashMapOf(
                    "id" to postId,
                    "userId" to currentUser.uid,
                    "username" to (userDoc.getString("username") ?: "Aventurier"),
                    "userProfileUrl" to (userDoc.getString("photoUrl") ?: ""),
                    "imageUrl" to downloadUrl.toString(),
                    "description" to description,
                    "locationName" to location,
                    "category" to category,
                    "tags" to tags,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "groupId" to groupId, // Champ ajouté ici
                    "likesCount" to 0,
                    "likedBy" to emptyList<String>(),
                    "comments" to emptyList<Comment>()
                )

                firestore.collection("posts").document(postId).set(postData).await()
                _isUploading.value = false
                onComplete()
            } catch (e: Exception) {
                Log.e("Upload", "Erreur: ${e.message}")
                _isUploading.value = false
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                val imageRef = storage.getReferenceFromUrl(post.imageUrl)
                imageRef.delete().await()
                firestore.collection("posts").document(post.id).delete().await()
            } catch (e: Exception) {
                Log.e("PostViewModel", "Erreur suppression: ${e.message}")
            }
        }
    }

    fun reportPost(post: Post) {
        viewModelScope.launch {
            val report = hashMapOf(
                "postId" to post.id,
                "reportedBy" to auth.currentUser?.uid,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("reports").add(report)
        }
    }

    private fun sendNotification(post: Post, type: String, commentText: String = "") {
        val currentUser = auth.currentUser ?: return
        if (post.userId == currentUser.uid) return

        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val notifId = UUID.randomUUID().toString()
                val notification = Notification(
                    id = notifId,
                    type = type,
                    fromUserId = currentUser.uid,
                    fromUsername = userDoc.getString("username") ?: "Aventurier",
                    fromUserProfileUrl = userDoc.getString("photoUrl") ?: "",
                    toUserId = post.userId,
                    postId = post.id,
                    postImageUrl = post.imageUrl,
                    commentText = commentText,
                    timestamp = com.google.firebase.Timestamp.now(),
                    read = false
                )
                firestore.collection("notifications").document(notifId).set(notification)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun getSimilarPosts(currentPost: Post): List<Post> {
        val allPosts = _posts.value
        val currentTags = currentPost.tags
        return allPosts.filter { post ->
            post.id != currentPost.id && post.tags.any { tag -> currentTags.contains(tag) }
        }
    }
}