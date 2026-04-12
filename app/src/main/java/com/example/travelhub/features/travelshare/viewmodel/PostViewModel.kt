package com.example.travelhub.features.travelshare.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.profile.UserProfile
import com.example.travelhub.features.travelshare.model.Post
import com.example.travelhub.features.travelshare.model.Comment
import com.example.travelhub.features.travelshare.model.Notification // Ajouté
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.util.Log
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class PostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedDate = MutableStateFlow<Long?>(null)

    // --- ÉTATS POUR LA NAVIGATION DEPUIS LES NOTIFS ---
    var selectedPostIdFromNotif by mutableStateOf<String?>(null)

    // On précise bien <Boolean> pour que Kotlin sache que c'est vrai/faux
    var shouldOpenCommentsFromNotif by mutableStateOf<Boolean>(false)

    // Fonction pour réinitialiser après l'ouverture
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

    // --- LIKES ---
    fun toggleLike(post: Post) {
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(post.id)
        if (post.likedBy.contains(currentUser.uid)) {
            postRef.update("likesCount", FieldValue.increment(-1L), "likedBy", FieldValue.arrayRemove(currentUser.uid))
        } else {
            postRef.update("likesCount", FieldValue.increment(1L), "likedBy", FieldValue.arrayUnion(currentUser.uid))
            sendNotification(post, "LIKE") // Ajouté pour le point rouge
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

    // --- FAVORIS (LA FONCTION QUI MANQUAIT) ---
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

                    // Optionnel : notifier l'auteur
                    val postDoc = firestore.collection("posts").document(postId).get().await()
                    val post = postDoc.toObject(Post::class.java)
                    if (post != null) {
                        sendNotification(post, "FAVORITE")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- COMMENTAIRES ---
    fun addComment(post: Post, text: String) { // Changé postId par post pour la notif
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
            } catch (e: Exception) { _isUploading.value = false }
        }
    }

    fun deletePost(post: Post) {

        viewModelScope.launch {

            try {

// Suppression de l'image

                val imageRef = storage.getReferenceFromUrl(post.imageUrl)

                imageRef.delete().await()


// Suppression du document

                firestore.collection("posts").document(post.id).delete().await()

                Log.d("PostViewModel", "Post supprimé avec succès")

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

    // --- ENVOI DES NOTIFICATIONS ---
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
        val currentTags = currentPost.tags // Liste de tags (ex: ["Nature", "Montagne"])

        return allPosts.filter { post ->
            // On exclut le post actuel de la liste des résultats
            post.id != currentPost.id &&
                    // On vérifie s'il y a au moins un tag en commun
                    post.tags.any { tag -> currentTags.contains(tag) }
        }
    }
}