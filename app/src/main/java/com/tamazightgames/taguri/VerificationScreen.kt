package com.tamazightgames.taguri

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VerificationScreen(onVerificationSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Email, "Email", modifier = Modifier.size(80.dp), tint = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(24.dp))

        Text("Vérifiez votre Email", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Un lien de confirmation a été envoyé à :\n${auth.currentUser?.email}",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Text("Si vous ne trouvez pas l’e-mail, vérifiez votre dossier spam ou courrier indésirable.",
            textAlign = TextAlign.Center,
            color = Color.Gray
            )

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton pour vérifier si l'utilisateur a cliqué
        Button(
            onClick = {
                isLoading = true
                // On recharge l'utilisateur pour voir s'il a validé l'email
                auth.currentUser?.reload()?.addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            Toast.makeText(context, "Email vérifié ! Bienvenue.", Toast.LENGTH_SHORT).show()
                            onVerificationSuccess()
                        } else {
                            Toast.makeText(context, "Email non validé. Cliquez sur le lien reçu.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Erreur de connexion.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("J'ai cliqué sur le lien.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour renvoyer l'email si perdu
        TextButton(onClick = {
            auth.currentUser?.sendEmailVerification()
            Toast.makeText(context, "Email renvoyé !", Toast.LENGTH_SHORT).show()
        }) {
            Text("Renvoyer l'email")
        }
    }
}