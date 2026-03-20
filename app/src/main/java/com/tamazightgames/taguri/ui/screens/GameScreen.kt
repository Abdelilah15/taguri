package com.tamazightgames.taguri.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tamazightgames.taguri.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen() {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var currentScreen by remember { mutableStateOf("game") }

    var showProfileMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var pseudo by remember { mutableStateOf("Chargement...") }
    var email by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var newPseudo by remember { mutableStateOf("") }

    var score by remember { mutableIntStateOf(0) }
    var niveau by remember { mutableIntStateOf(1) }
    var mots by remember { mutableIntStateOf(0) }
    var serieJour by remember { mutableIntStateOf(0) }
    var mesBadges by remember { mutableStateOf(listOf<String>()) }

    var chapitreActuel by remember { mutableIntStateOf(1) }
    var puzzleActuel by remember { mutableIntStateOf(1) }

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

                        score = document.getLong("score")?.toInt() ?: 0
                        niveau = document.getLong("niveau")?.toInt() ?: 1
                        mots = document.getLong("mots")?.toInt() ?: 0
                        chapitreActuel = document.getLong("chapitreActuel")?.toInt() ?: 1
                        puzzleActuel = document.getLong("puzzleActuel")?.toInt() ?: 1
                    }
                }
                .addOnFailureListener {
                    pseudo = "Erreur connexion"
                }
        }
    }

    if (currentScreen == "play") {
        // Si l'état est "play", on affiche le nouvel écran
        // Et si on clique sur retour, on remet l'état à "game"
        PlayScreen(
            niveau = puzzleActuel,
            onBackClick = { currentScreen = "game" }
        )

    } else {

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
            // --- LA ZONE DE JEU PRINCIPALE ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp), // Marge globale autour de l'écran
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 1. PARTIE HAUT : Défis & Événements (Rectangle horizontal)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3E0)), // Orange très pâle (tu pourras changer)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Défis Quotidiens & Événements\n(À venir)",
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE65100).copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Un espace flexible pour centrer le carré au milieu
                Spacer(modifier = Modifier.weight(1f))

                // --- 2. PARTIE MILIEU : LE CHAPITRE ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(), // S'adapte à la hauteur de son contenu
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Petite ombre
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        // A. LE GRAND CARRÉ INTERNE (Image + Texte)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(284.dp) // Assure que c'est un carré parfait
                        ) {
                            // L'Image de fond (Placeholder actuel)
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background), // <-- C'est ici qu'on changera l'image
                                contentDescription = "Fond du chapitre",
                                contentScale = ContentScale.Crop, // Remplit tout le carré
                                modifier = Modifier.fillMaxSize()
                            )

                            // Voile sombre pour faire ressortir le texte
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )

                            // Les Textes
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val chapitreActuel = ((niveau - 1) / 5) + 1

                                Text(
                                    text = "CHAPITRE $chapitreActuel",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "La Culture Amazigh",
                                    fontSize = 32.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 38.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Niveau $puzzleActuel",
                                    fontSize = 20.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // B. LE RECTANGLE EN BAS (Progression)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val totalJeuxChapitre = 6
                            val jeuxReussis = puzzleActuel - 1

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (i in 0 until totalJeuxChapitre) {
                                    val isCompleted = i < jeuxReussis
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isCompleted) Color(0xFF4CAF50) else Color(
                                                    0xFFE0E0E0
                                                )
                                            ) // Vert ou Gris
                                    )
                                }
                            }
                        }
                    }
                }

                // Un autre espace flexible
                Spacer(modifier = Modifier.weight(1f))

                // 3. PARTIE BAS : Bouton Commencer
                Button(
                    onClick = { currentScreen = "play" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp), // Bords bien arrondis
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text(
                        text = "COMMENCER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
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
                                                        val strokeWidth =
                                                            2.dp.toPx() // Épaisseur de la ligne
                                                        val y =
                                                            size.height - strokeWidth / 2
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
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.uid)
                                                .update("pseudo", newPseudo)
                                                .addOnSuccessListener {
                                                    pseudo =
                                                        newPseudo // On met à jour l'affichage
                                                    isEditing =
                                                        false  // On quitte le mode édition
                                                }
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        "Valider",
                                        tint = Color(0xFF4CAF50)
                                    ) // Vert
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

                        // --- LES 3 RECTANGLES DE STATISTIQUES (COULEURS FLOW) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp) // Espace entre les rectangles
                        ) {
                            // Rectangle 1 : Niveau (Bleu pastel)
                            StatCard(
                                title = "Niveau",
                                value = niveau.toString(),
                                backgroundColor = Color(0xFFE3F2FD), // Soft Blue
                                textColor = Color(0xFF1565C0),       // Dark Blue
                                modifier = Modifier.weight(1f)
                            )

                            // Rectangle 2 : Score (Orange pastel)
                            StatCard(
                                title = "Score",
                                value = score.toString(),
                                backgroundColor = Color(0xFFFFF3E0), // Soft Orange
                                textColor = Color(0xFFE65100),       // Dark Orange
                                modifier = Modifier.weight(1f)
                            )

                            // Rectangle 3 : Mots (Vert pastel)
                            StatCard(
                                title = "Mots",
                                value = mots.toString(),
                                backgroundColor = Color(0xFFE8F5E9), // Soft Green
                                textColor = Color(0xFF2E7D32),       // Dark Green
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

}

@Composable
fun StatCard(
    title: String,
    value: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)) // Bords bien arrondis
            .background(backgroundColor)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Titre (ex: "Score") en petit
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Valeur (ex: "150") en grand
            Text(
                text = value,
                fontSize = 24.sp,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}