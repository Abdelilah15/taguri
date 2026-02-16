package com.tamazightgames.taguri

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen() {
    Scaffold(
        // --- LA BARRE DU HAUT ---
        topBar = {
            TopAppBar(

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White, // Fond blanc (ou Color(0xFF1565C0) pour bleu, etc.)
                    actionIconContentColor = Color(0xFF1565C0), // Couleur des icônes (Bleu)
                    navigationIconContentColor = Color(0xFF1565C0) // Couleur de l'icône profil (Bleu)
                ),

                title = { }, // Pas de titre au milieu, on laisse vide

                // 1. À GAUCHE : L'IMAGE DE PROFIL
                navigationIcon = {
                    IconButton(
                        onClick = { /* TODO: Ouvrir le profil */ },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        // On utilise une icône ronde par défaut pour l'instant
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape), // Rend l'image bien ronde
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },

                // 2. À DROITE : LES BOUTONS (Notifications + Liste)
                actions = {
                    // Bouton Notifications
                    IconButton(onClick = { /* TODO: Afficher notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bouton Liste
                    IconButton(onClick = { /* TODO: Ouvrir le menu/liste */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Liste",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // --- LE CONTENU DU JEU ---
        // On utilise 'innerPadding' pour que le jeu ne soit pas caché sous la barre
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Zone de jeu (Le plateau s'affichera ici)")
        }
    }
}