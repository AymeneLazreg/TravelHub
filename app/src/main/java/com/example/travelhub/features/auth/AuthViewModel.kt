package com.example.travelhub.features.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Inscription : Vérifie le pseudo, crée le compte Auth, puis sauvegarde dans Firestore
     */
    fun registerUser(
        email: String,
        pass: String,
        nom: String,
        prenom: String,
        username: String, // <-- NOUVEAU PARAMÈTRE
        interests: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. On vérifie d'abord si le nom d'utilisateur existe déjà dans Firestore
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Si on trouve un document, c'est que le pseudo est pris !
                    onError("Ce nom d'utilisateur est déjà pris. Veuillez en choisir un autre.")
                } else {
                    // 2. Le pseudo est libre ! On crée le compte Firebase Auth
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = task.result?.user?.uid

                                if (userId != null) {
                                    // 3. Préparation des données pour Firestore
                                    val userMap = hashMapOf(
                                        "id" to userId,
                                        "email" to email,
                                        "nom" to nom,
                                        "prenom" to prenom,
                                        "username" to username, // <-- ON SAUVEGARDE LE PSEUDO
                                        "interests" to interests,
                                        "bio" to "",
                                        "location" to ""
                                    )

                                    // 4. Sauvegarde finale dans la collection "users"
                                    firestore.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { e ->
                                            onError(e.localizedMessage ?: "Erreur lors de la sauvegarde du profil")
                                        }
                                } else {
                                    onSuccess()
                                }
                            } else {
                                onError(task.exception?.localizedMessage ?: "Erreur lors de l'inscription (Email peut-être déjà utilisé)")
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onError("Impossible de vérifier le nom d'utilisateur : ${e.localizedMessage}")
            }
    }

    /**
     * Connexion (Inchangée)
     */
    fun loginUser(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Identifiants incorrects")
                }
            }
    }
}