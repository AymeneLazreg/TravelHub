package com.example.travelhub.features.travelshare.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.travelshare.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var notifications by mutableStateOf<List<Notification>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    // Vérifie en temps réel s'il y a des notifs non lues
    val hasUnread by derivedStateOf { notifications.any { !it.read } }

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("notifications")
            .whereEqualTo("toUserId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    notifications = snapshot.toObjects(Notification::class.java)
                }
                isLoading = false
            }
    }

    fun markAllAsRead() {
        val unreadNotifications = notifications.filter { !it.read }
        if (unreadNotifications.isEmpty()) return

        // MISE À JOUR LOCALE : On force l'UI à changer tout de suite
        notifications = notifications.map { it.copy(read = true) }

        // MISE À JOUR FIREBASE
        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                unreadNotifications.forEach { notification ->
                    val ref = firestore.collection("notifications").document(notification.id)
                    batch.update(ref, "read", true)
                }
                batch.commit().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        firestore.collection("notifications").document(notificationId).delete()
    }
}