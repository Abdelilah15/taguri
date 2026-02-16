package com.tamazightgames.taguri

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen() {

    var showProfileMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var pseudo by remember { mutableStateOf("Chargement...") }
    var email by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var newPseudo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // 1. L'email est facile, il est directement dans l'authentification
            email = user.email ?: ""

            // 2. Le pseudo est dans la base de données Firestore
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // On remplace "Chargement..." par le vrai pseudo
                        pseudo = document.getString("pseudo") ?: "Joueur"
                    }
                }
                .addOnFailureListener {
                    pseudo = "Erreur connexion"
                }
        }
    }

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
                onDismissRequest = {
                    showProfileMenu = false
                    isEditing = false
                },
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

                    // IMAGE DE PROFIL (Grande)
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Mon Profil",
                        modifier = Modifier
                            .size(100.dp) // Plus grand que dans la barre
                            .clip(CircleShape),
                        tint = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // NOM DU JOUEUR
                    // --- ZONE NOM MODIFIABLE ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isEditing) {
                            // --- MODE ÉDITION (Champ de texte + Bouton Valider) ---
                            BasicTextField(
                                value = newPseudo,
                                onValueChange = { newPseudo = it },
                                singleLine = true,



                                // CONFIGURATION DU STYLE
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 24.sp, // Même taille que le texte normal
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                ),

                                modifier = Modifier
                                    .widthIn(min = 40.dp) // Largeur minimum (pour ne pas disparaître si vide)
                                    .wrapContentWidth(),   // S'agrandit avec le texte

                                decorationBox = { innerTextField ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // A. Le champ de texte avec ton padding personnalisé
                                        Box(
                                            modifier = Modifier
                                                .drawBehind {
                                                    val strokeWidth = 2.dp.toPx() // Épaisseur de la ligne
                                                    val y = size.height - strokeWidth / 2
                                                    drawLine(
                                                        color = Color(0xFF1565C0), // Bleu
                                                        start = Offset(0f, y),
                                                        end = Offset(size.width, y),
                                                        strokeWidth = strokeWidth
                                                    )
                                                }
                                                .padding(bottom = 2.dp),
                                            contentAlignment = Alignment.Center

                                        ) {

                                            if (newPseudo.isEmpty()) {
                                                Text(
                                                    text = "new name",
                                                    color = Color.Gray,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            innerTextField() // C'est l'endroit où le curseur clignote
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Bouton ENREGISTRER
                            IconButton(onClick = {
                                if (newPseudo.isNotBlank()) {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user != null) {
                                        // Mise à jour Firestore
                                        FirebaseFirestore.getInstance().collection("users")
                                            .document(user.uid)
                                            .update("pseudo", newPseudo)
                                            .addOnSuccessListener {
                                                pseudo = newPseudo // On met à jour l'affichage
                                                isEditing = false  // On quitte le mode édition
                                            }
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Check, "Valider", tint = Color(0xFF4CAF50)) // Vert
                            }

                        } else {
                            // --- MODE LECTURE (Texte + Bouton Modifier) ---
                            Text(
                                text = pseudo,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Bouton CRAYON
                            IconButton(onClick = {
                                newPseudo = pseudo // On pré-remplit avec le nom actuel
                                isEditing = true   // On active le mode édition
                            }) {
                                Icon(Icons.Default.Edit, "Modifier", tint = Color.Gray)
                            }
                        }
                    }

                    if (email.isNotEmpty()) {
                        Text(
                            text = email,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

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