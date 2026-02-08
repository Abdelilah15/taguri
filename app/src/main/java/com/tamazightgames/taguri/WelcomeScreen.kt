package com.tamazightgames.taguri

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException


@Composable
fun WelcomeScreen(
    callbackManager: CallbackManager,
    onEmailClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uriHandler = LocalUriHandler.current

    // --- ÉTAT DE CHARGEMENT ---
    var isLoading by remember { mutableStateOf(false) }

    // --- CONFIGURATION FACEBOOK ---
    DisposableEffect(Unit) {
        val loginManager = LoginManager.getInstance()
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                // Facebook est OK, on passe à Firebase (le chargement continue)
                val token = result.accessToken
                val credential = FacebookAuthProvider.getCredential(token.token)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        isLoading = false // FIN DU CHARGEMENT
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            // GESTION DU CONFLIT
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(context, "Un compte existe déjà avec cet email via Google ou Mot de passe.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
            override fun onCancel() {
                isLoading = false
                Toast.makeText(context, "Connexion annulée", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                isLoading = false
                Toast.makeText(context, "Erreur Facebook: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        loginManager.registerCallback(callbackManager, callback)
        onDispose { }
    }

    // --- CONFIGURATION GOOGLE ---
    val clientId = stringResource(R.string.my_web_client_id)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        isLoading = false // FIN DU CHARGEMENT
                        if (authTask.isSuccessful) {
                            Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            // GESTION DU CONFLIT
                            if (authTask.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(context, "Un compte existe déjà avec cet email via Facebook ou Mot de passe.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Erreur: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(context, "Erreur Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            isLoading = false // L'utilisateur a annulé ou fermé la fenêtre
        }
    }

    // --- INTERFACE ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.PlayArrow, "Logo", modifier = Modifier.size(100.dp), tint = Color(0xFFFF9800))
            Spacer(modifier = Modifier.height(16.dp))
            Text("TAGURI", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(60.dp))

            // EMAIL
            Button(
                onClick = { onEmailClick() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                enabled = !isLoading // Désactive le bouton si ça charge
            ) { Text("Se connecter avec Email") }

            Spacer(modifier = Modifier.height(12.dp))

            // GOOGLE
            OutlinedButton(
                onClick = {
                    isLoading = true // DÉBUT DU CHARGEMENT
                    googleLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) { Text("Continuer avec Google") }

            Spacer(modifier = Modifier.height(12.dp))

            // FACEBOOK
            OutlinedButton(
                onClick = {
                    isLoading = true // DÉBUT DU CHARGEMENT
                    LoginManager.getInstance().logInWithReadPermissions(
                        context as Activity,
                        listOf("email", "public_profile")
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) { Text("Continuer avec Facebook") }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { onSignUpClick() }, enabled = !isLoading) {
                Text("Pas de compte ? ", color = Color.Gray)
                Text("Créer un compte", textDecoration = TextDecoration.Underline)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- SECTION LÉGALE (Tout en bas) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Collé en bas
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "En continuant, vous acceptez nos",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Conditions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // Ouvre une page web (Mets ton vrai lien plus tard)
                        uriHandler.openUri("https://www.google.com")
                    }
                )
                Text(" et ", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "Politique de confidentialité",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // Ouvre une page web
                        uriHandler.openUri("https://www.google.com")
                    }
                )
            }
        }

        // --- L'INDICATEUR DE CHARGEMENT (OVERLAY) ---
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)) // Fond gris semi-transparent
                    .clickable(enabled = false) {}, // Bloque les clics en dessous
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color( 0xFFFF9800))
            }
        }
    }
}
