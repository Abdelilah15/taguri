package com.tamazightgames.taguri

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException


@Composable
// ON AJOUTE UN NOUVEAU PARAMÈTRE : isLoginMode
fun LoginScreen(
        onLoginSuccess: () -> Unit,
        onVerificationNeeded: () -> Unit,
        onBackClick: () -> Unit,
        isLoginMode: Boolean
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // On change le titre selon le mode
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
                ) // Un peu de marge
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
            modifier = Modifier.fillMaxWidth()
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
            }
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
            // MODE INSCRIPTION
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // 1. Validation de sécurité du mot de passe
                    if (password.length < 8) {
                        message = "Le mot de passe doit faire au moins 8 caractères."
                    } else if (password.none { !it.isLetterOrDigit() }) {
                        // On vérifie s'il y a au moins un caractère qui n'est NI une lettre NI un chiffre
                        message = "Ajoutez un caractère spécial (ex: @, #, !, $)."
                    } else if (email.isNotEmpty() && password.isNotEmpty()) {
                        // 2. Si tout est bon, on lance l'inscription Firebase
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    auth.currentUser?.sendEmailVerification()
                                        ?.addOnCompleteListener { emailTask ->
                                            if (emailTask.isSuccessful) {
                                                onVerificationNeeded()
                                            } else {
                                                message = "Erreur d'envoi mail: ${emailTask.exception?.message}"
                                            }
                                        }
                                } else {
                                    message = "Erreur: ${task.exception?.message}"
                                }
                            }
                    } else {
                        message = "Veuillez remplir tous les champs."
                    }
                }
            ) {
                Text("S'inscrire")
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