package com.tamazightgames.taguri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tamazightgames.taguri.ui.theme.TaguriTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaguriTheme {
                var currentScreen by remember { mutableStateOf("accueil") }
                // Nouvelle variable : est-ce qu'on veut se connecter (true) ou s'inscrire (false) ?
                var isLoginMode by remember { mutableStateOf(true) }

                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                LaunchedEffect(Unit) {
                    if (auth.currentUser != null) {
                        currentScreen = "jeu"
                    }
                }

                if (currentScreen == "accueil") {
                    WelcomeScreen(
                        onEmailClick = {
                            isLoginMode = true // On veut se connecter
                            currentScreen = "login"
                        },
                        onSignUpClick = {
                            isLoginMode = false // On veut s'inscrire
                            currentScreen = "login"
                        },
                        onLoginSuccess = {
                            currentScreen = "jeu"
                        }
                    )
                } else if (currentScreen == "login") {
                    LoginScreen(
                        isLoginMode = isLoginMode, // On envoie l'info à l'écran
                        onLoginSuccess = {
                            currentScreen = "jeu"
                        }
                    )
                } else if (currentScreen == "jeu") {
                    GameScreen()
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TaguriTheme {
        Greeting("Android")
    }
}