package com.tamazightgames.taguri

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // On récupère l'outil d'authentification de Firebase
    val auth = FirebaseAuth.getInstance()

    // Variables pour stocker ce que l'utilisateur écrit (Email, Mot de passe)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") } // Pour afficher les erreurs ou succès

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp), // Prend tout l'écran avec une marge
        verticalArrangement = Arrangement.Center, // Centre verticalement
        horizontalAlignment = Alignment.CenterHorizontally // Centre horizontalement
    ) {
        Text(text = "Bienvenue !", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp)) // Espace vide

        // Champ Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ Mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(), // Cache le mot de passe
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage du message (Erreur ou Succès)
        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Les Boutons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Bouton LOGIN
            Button(onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                message = "Connexion réussie !"
                                onLoginSuccess()
                            } else {
                                message = "Erreur: ${task.exception?.message}"
                            }
                        }
                }
            }) {
                Text("Se connecter")
            }

            // Bouton SIGN UP
            OutlinedButton(onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                message = "Compte créé ! Connecte-toi."
                            } else {
                                message = "Erreur: ${task.exception?.message}"
                            }
                        }
                }
            }) {
                Text("S'inscrire")
            }
        }
    }
}

