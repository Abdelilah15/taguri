package com.tamazightgames.taguri

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen() {

    var showProfileMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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
                        onClick = { showProfileMenu = true },
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

        if (showProfileMenu) {
            ModalBottomSheet(
                onDismissRequest = { showProfileMenu = false },
                sheetState = sheetState,
                containerColor = Color.White // Fond du menu
            ) {
                // Contenu du menu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 48.dp), // Marge en bas
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. BOUTON FERMER (Aligné à droite)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        IconButton(onClick = { showProfileMenu = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = Color.Gray
                            )
                        }
                    }

                    // 2. IMAGE DE PROFIL (Grande)
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Mon Profil",
                        modifier = Modifier
                            .size(100.dp) // Plus grand que dans la barre
                            .clip(CircleShape),
                        tint = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. NOM DU JOUEUR
                    Text(
                        text = "Mon Nom", // Placeholder pour l'instant
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. AUTRES ÉLÉMENTS (Zone vide pour la suite)
                    Divider() // Une petite ligne de séparation
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Autres options ici...", color = Color.Gray)

                    // Exemple pour voir à quoi ça ressemble :
                    // Button(onClick = {}) { Text("Modifier le profil") }
                }
            }
        }

    }
}