package com.example.travelhub.features.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.travelhub.utils.PrefsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// 1. On change ViewModel par AndroidViewModel(application)
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 2. On initialise le PrefsManager avec le context de l'application
    private val prefsManager = PrefsManager(application)

    // 3. On expose l'email sauvegardé pour que le LoginScreen puisse le lire
    val savedEmail = prefsManager.getEmail.asLiveData()

    /**
     * Connexion : Authentifie l'utilisateur ET sauvegarde l'email si réussi
     */
    fun loginUser(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // --- NOUVEAU : Sauvegarde de l'identifiant ---
                    viewModelScope.launch {
                        prefsManager.saveEmail(email)
                    }
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Identifiants incorrects")
                }
            }
    }

    /**
     * Inscription : Vérifie le pseudo, crée le compte, sauvegarde Firestore ET mémorise l'email
     */
    fun registerUser(
        email: String,
        pass: String,
        nom: String,
        prenom: String,
        username: String,
        interests: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    onError("Ce nom d'utilisateur est déjà pris.")
                } else {
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = task.result?.user?.uid

                                if (userId != null) {
                                    val userMap = hashMapOf(
                                        "id" to userId,
                                        "email" to email,
                                        "nom" to nom,
                                        "prenom" to prenom,
                                        "username" to username,
                                        "interests" to interests,
                                        "bio" to "",
                                        "location" to ""
                                    )

                                    firestore.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            // --- NOUVEAU : On mémorise aussi à l'inscription ---
                                            viewModelScope.launch {
                                                prefsManager.saveEmail(email)
                                            }
                                            onSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            onError(e.localizedMessage ?: "Erreur Firestore")
                                        }
                                } else { onSuccess() }
                            } else {
                                onError(task.exception?.localizedMessage ?: "Erreur d'inscription")
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onError("Erreur vérification pseudo : ${e.localizedMessage}")
            }
    }
}