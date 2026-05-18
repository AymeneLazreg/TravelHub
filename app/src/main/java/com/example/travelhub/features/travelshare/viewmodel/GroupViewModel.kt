package com.example.travelhub.features.travelshare.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelhub.features.travelshare.model.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GroupViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var userGroups by mutableStateOf<List<Group>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        fetchUserGroups()
    }

    fun createGroup(name: String, imageUri: Uri?, onResult: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val formattedName = name.trim()

        if (formattedName.isEmpty()) {
            onResult(false, "Le nom ne peut pas être vide")
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                val existing = firestore.collection("groups")
                    .get()
                    .await()

                val alreadyExists = existing.documents.any {
                    it.getString("name")?.lowercase() == formattedName.lowercase()
                }

                if (alreadyExists) {
                    onResult(false, "Ce nom de groupe est déjà pris")
                    isLoading = false
                    return@launch
                }

                var downloadUrl = ""

                if (imageUri != null) {
                    val fileName = "groups/${UUID.randomUUID()}.jpg"
                    val ref = storage.reference.child(fileName)
                    ref.putFile(imageUri).await()
                    downloadUrl = ref.downloadUrl.await().toString()
                }

                val groupId = firestore.collection("groups").document().id

                val group = Group(
                    id = groupId,
                    name = formattedName,
                    adminId = currentUser.uid,
                    members = listOf(currentUser.uid),
                    imageUrl = downloadUrl
                )

                firestore.collection("groups").document(groupId).set(group).await()

                fetchUserGroups()
                onResult(true, "Groupe créé avec succès !")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Erreur lors de la création")
            } finally {
                isLoading = false
            }
        }
    }

    fun joinGroupByName(groupName: String, onResult: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        val formattedName = groupName.trim().lowercase()

        isLoading = true

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("groups").get().await()

                val groupDoc = snapshot.documents.find {
                    it.getString("name")?.lowercase() == formattedName
                }

                if (groupDoc == null) {
                    onResult(false, "Groupe introuvable")
                } else {
                    val groupId = groupDoc.id
                    val members = groupDoc.get("members") as? List<String> ?: emptyList()

                    if (members.contains(currentUserId)) {
                        onResult(false, "Vous êtes déjà membre")
                    } else {
                        firestore.collection("groups").document(groupId)
                            .update("members", FieldValue.arrayUnion(currentUserId))
                            .await()

                        fetchUserGroups()
                        onResult(true, "Bienvenue dans le groupe !")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Erreur réseau")
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchUserGroups() {
        val currentUserId = auth.currentUser?.uid ?: return
        isLoading = true

        firestore.collection("groups")
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                userGroups = snapshot.toObjects(Group::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun leaveGroup(groupId: String, onResult: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        isLoading = true

        viewModelScope.launch {
            try {
                firestore.collection("groups").document(groupId)
                    .update("members", FieldValue.arrayRemove(currentUserId))
                    .await()

                fetchUserGroups()
                onResult(true, "Vous avez quitté le groupe")
            } catch (e: Exception) {
                onResult(false, "Erreur lors de l'opération")
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteGroup(groupId: String, onResult: (Boolean, String) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                firestore.collection("groups").document(groupId).delete().await()

                val postsSnapshot = firestore.collection("posts")
                    .whereEqualTo("groupId", groupId)
                    .get()
                    .await()

                for (doc in postsSnapshot.documents) {
                    doc.reference.update(
                        mapOf(
                            "groupId" to null,
                            "groupName" to null
                        )
                    ).await()
                }

                fetchUserGroups()
                onResult(true, "Groupe supprimé définitivement")
            } catch (e: Exception) {
                onResult(false, "Erreur lors de la suppression")
            } finally {
                isLoading = false
            }
        }
    }

    fun updateGroup(
        group: Group,
        newName: String,
        newImageUri: Uri?,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        val formattedName = newName.trim()

        if (formattedName.isEmpty()) {
            onResult(false, "Le nom ne peut pas être vide")
            return
        }

        if (group.adminId != currentUserId) {
            onResult(false, "Seul l'administrateur peut modifier ce groupe")
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                val existing = firestore.collection("groups")
                    .get()
                    .await()

                val alreadyExists = existing.documents.any { doc ->
                    val docName = doc.getString("name")?.lowercase()
                    doc.id != group.id && docName == formattedName.lowercase()
                }

                if (alreadyExists) {
                    onResult(false, "Ce nom de groupe est déjà pris")
                    isLoading = false
                    return@launch
                }

                var imageUrl = group.imageUrl

                if (newImageUri != null) {
                    val fileName = "groups/${group.id}_${UUID.randomUUID()}.jpg"
                    val ref = storage.reference.child(fileName)
                    ref.putFile(newImageUri).await()
                    imageUrl = ref.downloadUrl.await().toString()
                }

                firestore.collection("groups").document(group.id)
                    .update(
                        mapOf(
                            "name" to formattedName,
                            "imageUrl" to imageUrl
                        )
                    )
                    .await()

                val postsSnapshot = firestore.collection("posts")
                    .whereEqualTo("groupId", group.id)
                    .get()
                    .await()

                for (doc in postsSnapshot.documents) {
                    doc.reference.update("groupName", formattedName).await()
                }

                fetchUserGroups()
                onResult(true, "Groupe modifié avec succès")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Erreur lors de la modification")
            } finally {
                isLoading = false
            }
        }
    }
}