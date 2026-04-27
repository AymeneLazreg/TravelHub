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

    // --- CRÉATION DE GROUPE AVEC PHOTO ET UNICITÉ ---
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
                // 1. Vérifier si le groupe existe déjà (insensible à la casse)
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

                // 2. Upload de l'image si elle existe
                if (imageUri != null) {
                    val fileName = "groups/${UUID.randomUUID()}.jpg"
                    val ref = storage.reference.child(fileName)
                    ref.putFile(imageUri).await()
                    downloadUrl = ref.downloadUrl.await().toString()
                }

                // 3. Création de l'objet Groupe
                val groupId = firestore.collection("groups").document().id
                val group = Group(
                    id = groupId,
                    name = formattedName,
                    adminId = currentUser.uid,
                    members = listOf(currentUser.uid),
                    imageUrl = downloadUrl
                )

                // 4. Enregistrement Firestore
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

    // --- REJOINDRE PAR NOM (INSENSIBLE À LA CASSE) ---
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

    // --- CHARGER LES GROUPES ---
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

    // --- QUITTER UN GROUPE ---
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

    // --- SUPPRIMER UN GROUPE ---
    fun deleteGroup(groupId: String, onResult: (Boolean, String) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                firestore.collection("groups").document(groupId).delete().await()
                fetchUserGroups()
                onResult(true, "Groupe supprimé définitivement")
            } catch (e: Exception) {
                onResult(false, "Erreur lors de la suppression")
            } finally {
                isLoading = false
            }
        }
    }
}