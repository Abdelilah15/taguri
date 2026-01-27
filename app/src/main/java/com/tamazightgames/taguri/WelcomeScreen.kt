package com.tamazightgames.taguri

import androidx.compose.foundation.Image
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun WelcomeScreen(
    callbackManager: CallbackManager,
    onEmailClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit
) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var errorMessage by remember { mutableStateOf("") }

    // --- CONFIGURATION FACEBOOK (On "écoute" si la connexion réussit) ---
    DisposableEffect(Unit) {
        val loginManager = LoginManager.getInstance()
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                // 1. Facebook a dit OUI -> On récupère le "Token"
                val token = result.accessToken
                // 2. On l'échange contre un accès Firebase
                val credential = FacebookAuthProvider.getCredential(token.token)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess() // VICTOIRE !
                        } else {
                            errorMessage = "Erreur Firebase FB: ${task.exception?.message}"
                        }
                    }
            }
            override fun onCancel() { errorMessage = "Connexion annulée" }
            override fun onError(error: FacebookException) {
                errorMessage = "Erreur Facebook: ${error.message}"
            }
        }
        loginManager.registerCallback(callbackManager, callback)
        onDispose { }
    }

    // 1. Configuration de Google Sign-In
    // On demande l'email et un "ID Token" pour Firebase
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("39947719003-o5nntfp0va8ato2o1gqohmgqh0tdsilu.apps.googleusercontent.com") // Cette ID est générée par Firebase
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // 2. Le "Launcher" : C'est lui qui gère le retour de la fenêtre Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google a dit OUI, on récupère le compte
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                // On s'authentifie auprès de Firebase avec ce token
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            // VICTOIRE ! On va vers le jeu
                            onLoginSuccess()
                        } else {
                            errorMessage = "Erreur Firebase: ${authTask.exception?.message}"
                        }
                    }
            } catch (e: ApiException) {
                errorMessage = "Erreur Google: ${e.statusCode}"
                Log.e("Taguri", "Google Sign in failed", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Marge tout autour
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. LE LOGO (Place-holder pour l'instant) ---
        // On utilise une icône par défaut en attendant ton vrai logo
        Icon(
            imageVector = Icons.Default.PlayArrow, // On changera ça par ton image plus tard
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFFF9800) // Couleur Orange du jeu
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TAGURI", // Nom du jeu
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0) // Bleu foncé
        )

        Spacer(modifier = Modifier.height(60.dp)) // Grand espace

        // --- 2. LES BOUTONS ---

        // Bouton EMAIL
        Button(
            onClick = { onEmailClick() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text("Se connecter avec Email")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bouton GOOGLE
        OutlinedButton(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuer avec Google")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bouton FACEBOOK
        OutlinedButton(
            onClick = {
                LoginManager.getInstance().logInWithReadPermissions(
                    context as Activity,
                    listOf("email", "public_profile")
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continuer avec Facebook")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lien CRÉER UN COMPTE
        TextButton(onClick = { onSignUpClick() }) {
            Text("Pas de compte ? Créer un compte", color = Color.Gray)
        }
    }
}