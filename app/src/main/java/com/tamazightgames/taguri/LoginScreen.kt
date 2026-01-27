package com.tamazightgames.taguri

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
// ON AJOUTE UN NOUVEAU PARAMÈTRE : isLoginMode
fun LoginScreen(
        onLoginSuccess: () -> Unit,
        onVerificationNeeded: () -> Unit,
        onBackClick: () -> Unit,
        isLoginMode: Boolean
) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // On change le titre selon le mode
    val screenTitle = if (isLoginMode) "Connexion" else "Créer un compte"

    Box(modifier = Modifier.fillMaxSize()) {

        // --- 1. LE BOUTON RETOUR (En haut à gauche) ---
        IconButton(
            onClick = { onBackClick() },
            modifier = Modifier
                .align(Alignment.TopStart) // Collé en haut à gauche
                .padding(16.dp) // Un peu de marge
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = screenTitle, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- LA LOGIQUE DE CHOIX DU BOUTON ---
        if (isLoginMode) {
            // MODE CONNEXION (LOGIN)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
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
                }
            ) {
                Text("Se connecter")
            }
        } else {
            // MODE INSCRIPTION (SIGN UP)
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    auth.currentUser?.sendEmailVerification()
                                        ?.addOnCompleteListener { emailTask ->
                                            if (emailTask.isSuccessful) {
                                                // 2. On va vers l'écran de vérification
                                                onVerificationNeeded()
                                            } else {
                                                message = "Erreur d'envoi mail: ${emailTask.exception?.message}"
                                            }
                                        }
                                } else {
                                    message = "Erreur: ${task.exception?.message}"
                                }
                            }
                    }
                }
            ) {
                Text("S'inscrire")
            }
        }
    }
}

