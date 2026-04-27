package com.example.travelhub.features.travelshare.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.travelhub.features.travelshare.model.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var userGroups by mutableStateOf<List<Group>>(emptyList())
    var isLoading by mutableStateOf(false)

    // --- CRÉATION AVEC UNICITÉ INSENSIBLE À LA CASSE ---
    fun createGroup(groupName: String, onResult: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        // On force en minuscules pour la base de données
        val formattedName = groupName.trim().lowercase()

        if (formattedName.isEmpty()) {
            onResult(false, "Le nom ne peut pas être vide")
            return
        }

        isLoading = true

        firestore.collection("groups")
            .whereEqualTo("name", formattedName)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // Si le snapshot n'est pas vide, c'est que le nom (en minuscules) existe déjà
                    onResult(false, "Ce groupe existe déjà")
                    isLoading = false
                } else {
                    val groupId = firestore.collection("groups").document().id
                    val newGroup = Group(
                        id = groupId,
                        name = formattedName, // Enregistré en minuscules
                        adminId = currentUserId,
                        members = listOf(currentUserId),
                        inviteCode = ""
                    )

                    firestore.collection("groups").document(groupId).set(newGroup)
                        .addOnSuccessListener {
                            fetchUserGroups()
                            onResult(true, "Groupe créé avec succès !")
                        }
                        .addOnFailureListener {
                            onResult(false, "Erreur lors de la création")
                            isLoading = false
                        }
                }
            }
            .addOnFailureListener {
                onResult(false, "Erreur de connexion à la base de données")
                isLoading = false
            }
    }

    // --- REJOINDRE PAR NOM (INSENSIBLE À LA CASSE) ---
    fun joinGroupByName(groupName: String, onResult: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        val formattedName = groupName.trim().lowercase()

        isLoading = true

        firestore.collection("groups")
            .whereEqualTo("name", formattedName)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(false, "Groupe introuvable")
                    isLoading = false
                } else {
                    val doc = snapshot.documents[0]
                    val groupId = doc.id
                    val members = doc.get("members") as? List<String> ?: emptyList()

                    if (members.contains(currentUserId)) {
                        onResult(false, "Vous êtes déjà membre de ce groupe")
                        isLoading = false
                    } else {
                        firestore.collection("groups").document(groupId)
                            .update("members", FieldValue.arrayUnion(currentUserId))
                            .addOnSuccessListener {
                                fetchUserGroups()
                                onResult(true, "Bienvenue dans le groupe !")
                            }
                            .addOnFailureListener {
                                onResult(false, "Impossible de rejoindre le groupe")
                                isLoading = false
                            }
                    }
                }
            }
            .addOnFailureListener {
                onResult(false, "Erreur réseau")
                isLoading = false
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
    // --- QUITTER UN GROUPE ---
    fun leaveGroup(groupId: String, onResult: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        isLoading = true

        firestore.collection("groups").document(groupId)
            .update("members", FieldValue.arrayRemove(currentUserId))
            .addOnSuccessListener {
                fetchUserGroups()
                onResult(true, "Vous avez quitté le groupe")
            }
            .addOnFailureListener {
                onResult(false, "Erreur lors de l'opération")
                isLoading = false
            }
    }

    // --- SUPPRIMER UN GROUPE (Seulement pour l'admin) ---
    fun deleteGroup(groupId: String, onResult: (Boolean, String) -> Unit) {
        isLoading = true

        firestore.collection("groups").document(groupId).delete()
            .addOnSuccessListener {
                fetchUserGroups()
                onResult(true, "Groupe supprimé définitivement")
            }
            .addOnFailureListener {
                onResult(false, "Erreur lors de la suppression")
                isLoading = false
            }
    }
}