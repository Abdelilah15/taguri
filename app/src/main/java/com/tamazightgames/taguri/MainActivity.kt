package com.tamazightgames.taguri

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.facebook.CallbackManager
import com.tamazightgames.taguri.ui.theme.TaguriTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. ON INSTALLE L'ÉCRAN DE DÉMARRAGE SYSTÈME (Avant super.onCreate)
        val splashScreen =  installSplashScreen()

        super.onCreate(savedInstanceState)

        // Variable pour dire au système : "Attends, je charge encore !"
        var isChecking = true

        // 2. ON BLOQUE L'ÉCRAN DE DÉMARRAGE TANT QUE 'isChecking' EST VRAI
        splashScreen.setKeepOnScreenCondition { isChecking }

        enableEdgeToEdge()
        setContent {
            TaguriTheme {
                var currentScreen by remember { mutableStateOf("accueil") }
                var isLoginMode by remember { mutableStateOf(true) }
                val auth = FirebaseAuth.getInstance()

                // 3. LA LOGIQUE DE DÉMARRAGE (Invisible pour l'utilisateur)
                LaunchedEffect(Unit) {
                    // Optionnel : Un petit délai pour que le logo reste visible 1 seconde
                    delay(1000)

                    val user = auth.currentUser
                    if (user != null) {
                        if (user.isEmailVerified) {
                            currentScreen = "jeu"
                        } else {
                            currentScreen = "verification"
                        }
                    } else {
                        currentScreen = "accueil"
                    }

                    // C'est fini, on libère l'écran !
                    isChecking = false
                }

                // --- GESTION DES ÉCRANS (Plus besoin de "splash" ici) ---
                when (currentScreen) {
                    "accueil" -> WelcomeScreen(
                        callbackManager = callbackManager,
                        onEmailClick = { isLoginMode = true; currentScreen = "login" },
                        onSignUpClick = { isLoginMode = false; currentScreen = "login" },
                        onLoginSuccess = { currentScreen = "jeu" }
                    )
                    "login" -> LoginScreen(
                        isLoginMode = isLoginMode,
                        onLoginSuccess = { currentScreen = "jeu" },
                        onVerificationNeeded = { currentScreen = "verification" },
                        onBackClick = { currentScreen = "accueil" }
                    )
                    "verification" -> VerificationScreen(
                        onVerificationSuccess = { currentScreen = "jeu" }
                    )
                    "jeu" -> GameScreen()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}