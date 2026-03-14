package com.tamazightgames.taguri.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.tamazightgames.taguri.FirestoreHelper
import com.tamazightgames.taguri.R


@Composable
// ON AJOUTE UN NOUVEAU PARAMÈTRE : isLoginMode
fun LoginScreen(
        onLoginSuccess: () -> Unit,
        onVerificationNeeded: () -> Unit,
        onBackClick: () -> Unit,
        isLoginMode: Boolean
) {
    val auth = FirebaseAuth.getInstance()
    val firestoreHelper = remember { FirestoreHelper() }
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }


    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)) // Fond gris semi-transparent
                .clickable(enabled = false) {}, // Bloque les clics en dessous
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFF9800))
        }
    }

    val screenTitle = if (isLoginMode) "Connexion" else "Créer un compte"

    Box(modifier = Modifier.fillMaxSize()) {

        // --- 1. LE BOUTON RETOUR (En haut à gauche) ---
        IconButton(
            onClick = { onBackClick() },
            modifier = Modifier
                .align(Alignment.TopStart) // Collé en haut à gauche
                .padding(
                    start = 4.dp,
                    top = 16.dp
                ),
            enabled = !isLoading
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
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                val image = if (passwordVisible)
                    painterResource(id = R.drawable.icon_visibility) // Ton icône œil
                else
                    painterResource(id = R.drawable.icon_visibilityoff) // Ton icône œil barré

                // Description pour l'accessibilité (aveugles)
                val description = if (passwordVisible) "Cacher le mot de passe" else "Voir le mot de passe"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = image, contentDescription = description)
                }
            },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- LA LOGIQUE DE CHOIX DU BOUTON ---
        if (isLoginMode) {
            // MODE CONNEXION (LOGIN)

            // LIEN "MOT DE PASSE OUBLIÉ ?"
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { showResetDialog = true }) {
                    Text("Mot de passe oublié ?", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Connexion réussie -> On vérifie/crée le profil Firestore aussi (par sécurité)
                                    val user = auth.currentUser
                                    if (user != null) {
                                        firestoreHelper.createUserProfile(user,
                                            onSuccess = {
                                                isLoading = false
                                                onLoginSuccess()
                                            },
                                            onFailure = { error ->
                                                isLoading = false
                                                // On laisse passer quand même, c'est juste la BDD qui a échoué
                                                onLoginSuccess()
                                            }
                                        )
                                    } else {
                                        isLoading = false
                                        onLoginSuccess()
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Se connecter")
            }
        } else {
            // INSCRIPTION
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    if (password.length < 8) {
                        Toast.makeText(context, "Mot de passe trop court (min 8)", Toast.LENGTH_SHORT).show()
                    } else if (password.none { !it.isLetterOrDigit() }) {
                        Toast.makeText(context, "Il faut un caractère spécial (@, #, !)", Toast.LENGTH_SHORT).show()
                    } else if (email.isNotEmpty()) {
                        isLoading = true // CHARGEMENT
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    // 1. On crée le profil dans Firestore
                                    if (user != null) {
                                        firestoreHelper.createUserProfile(user,
                                            onSuccess = {
                                                // 2. On envoie l'email de vérification
                                                user.sendEmailVerification()
                                                    .addOnCompleteListener {
                                                        isLoading = false
                                                        onVerificationNeeded()
                                                    }
                                            },
                                            onFailure = { error ->
                                                isLoading = false
                                                Toast.makeText(context, "Erreur création profil: $error", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                } else {
                                    isLoading = false
                                    if (task.exception is FirebaseAuthUserCollisionException) {
                                        Toast.makeText(context, "Cet email est déjà utilisé.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(context, "Remplissez tous les champs", Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text("S'inscrire")
            }

        }


    }

    // --- 3. FENÊTRE DE RÉCUPÉRATION (DIALOG) ---
    if (showResetDialog) {
        var resetEmail by remember { mutableStateOf(email) } // On pré-remplit avec l'email déjà tapé
        var isSending by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Récupérer le mot de passe") },
            text = {
                Column {
                    Text("Entrez votre email pour recevoir le lien de réinitialisation.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotEmpty()) {
                            isSending = true
                            // Envoi de l'email via Firebase
                            auth.sendPasswordResetEmail(resetEmail)
                                .addOnCompleteListener { task ->
                                    isSending = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Email envoyé ! Vérifiez votre boîte mail.", Toast.LENGTH_LONG).show()
                                        showResetDialog = false
                                    } else {
                                        // ICI : On vérifie si l'erreur est "Utilisateur inconnu"
                                        if (task.exception is FirebaseAuthInvalidUserException) {
                                            Toast.makeText(
                                                context,
                                                "Aucun compte n'existe avec cet email.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            // Autres erreurs (internet, etc.)
                                            Toast.makeText(
                                                context,
                                                "Erreur: ${task.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                        }
                    },
                    enabled = !isSending
                ) {
                    Text(if (isSending) "Envoi..." else "Envoyer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}