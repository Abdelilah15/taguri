package com.tamazightgames.taguri

// --- IMPORTATIONS OBLIGATOIRES ---
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
// ---------------------------------

class FirestoreHelper {
    // Si "FirebaseFirestore" est rouge ici, c'est que la bibliothèque n'est pas chargée
    private val db = FirebaseFirestore.getInstance()

    fun createUserProfile(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = user.uid
        val userDoc = db.collection("users").document(userId)

        // 1. On vérifie si l'utilisateur existe déjà
        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Il existe déjà, on ne fait rien
                onSuccess()
            } else {
                // 2. Il n'existe pas, on crée son profil
                val newUser = hashMapOf(
                    "email" to (user.email ?: ""),
                    "pseudo" to (user.displayName ?: "Joueur"),
                    "score" to 0,
                    "niveau" to 1,
                    "mots" to 0,
                    "chapitreActuel" to 1, // NOUVEAU
                    "puzzleActuel" to 1,   // NOUVEAU
                    "dateCreation" to System.currentTimeMillis()
                )

                // "SetOptions" a besoin de l'import en haut pour fonctionner
                userDoc.set(newUser, SetOptions.merge())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Erreur BDD") }
            }
        }.addOnFailureListener { e ->
            onFailure(e.message ?: "Erreur connexion BDD")
        }
    }
}
