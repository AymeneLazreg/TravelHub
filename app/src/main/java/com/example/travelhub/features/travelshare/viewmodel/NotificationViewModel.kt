package com.example.travelhub.features.travelshare.viewmodel

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.travelshare.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var notificationListener: ListenerRegistration? = null

    var notifications by mutableStateOf<List<Notification>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    val hasUnread by derivedStateOf { notifications.any { !it.read } }

    init {
        refreshNotifications()
    }

    fun refreshNotifications() {
        notificationListener?.remove()

        // On ne vide pas forcément la liste ici si on veut éviter un flash blanc,
        // mais pour un changement de compte, c'est obligatoire :
        notifications = emptyList()
        isLoading = true

        val userId = auth.currentUser?.uid ?: run {
            isLoading = false
            return
        }

        notificationListener = firestore.collection("notifications")
            .whereEqualTo("toUserId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) {
                    Log.e("NotificationViewModel", "Erreur écoute: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Utilisation de toList() pour forcer la recomposition Compose
                    val newNotifs = snapshot.toObjects(Notification::class.java).toList()
                    notifications = newNotifs
                    Log.d("NotificationViewModel", "Notifs reçues: ${newNotifs.size}")
                }
            }
    }

    fun markAllAsRead() {
        // On récupère uniquement celles qui sont réellement non lues dans l'état actuel
        val unreadNotifications = notifications.filter { !it.read }

        if (unreadNotifications.isEmpty()) {
            Log.d("NotificationViewModel", "Rien à marquer comme lu")
            return
        }

        viewModelScope.launch {
            try {
                // MISE À JOUR LOCALE immédiate pour que l'UI réagisse au bout des X secondes du délai
                notifications = notifications.map { it.copy(read = true) }

                val batch = firestore.batch()
                unreadNotifications.forEach { notification ->
                    val ref = firestore.collection("notifications").document(notification.id)
                    batch.update(ref, "read", true)
                }
                batch.commit().await()
                Log.d("NotificationViewModel", "Batch de lecture réussi")
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Erreur markAsRead: ${e.message}")
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("notifications").document(notificationId).delete().await()
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Erreur suppression: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationListener?.remove()
    }
}