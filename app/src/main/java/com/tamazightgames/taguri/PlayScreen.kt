package com.tamazightgames.taguri

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(onBackClick: () -> Unit) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var showWinDialog by remember { mutableStateOf(false) }

    // --- 1. LES DONNÉES DU NIVEAU (Simulation du JSON) ---
    val gridSize = 4 // Grille 4x4
    val grille = listOf(
        listOf('I', 'Z', 'M', 'N'), // IZM = Lion (Horizontal)
        listOf('A', 'F', 'G', 'A'),
        listOf('F', 'U', 'S', 'A'), // FUS = Main (Horizontal)
        listOf('T', 'I', 'R', 'R')
    )
    val motsATrouver = listOf("IZM", "FUS", "AFGAN", "TIRRA")

    // --- 2. LES ÉTATS DU JEU (Mémoire de l'écran) ---
    // Garde en mémoire les cases en train d'être glissées (Ligne, Colonne)
    var selectedCells by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }

    // Garde en mémoire les mots déjà trouvés
    var motsTrouves by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mots Mêlés", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Retour") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- 3. GALERIE D'IMAGES EN HAUT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Images à deviner :", fontWeight = FontWeight.Bold)
                    Text("🦁 (IZM)   |   🖐️ (FUS)", fontSize = 24.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Affiche les mots déjà trouvés
            Text(
                text = "Trouvés : ${motsTrouves.joinToString(", ")}",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. LA GRILLE INTERACTIVE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Reste un carré parfait
                    .background(Color.Transparent)
                    // C'EST ICI QU'ON DÉTECTE LE GLISSEMENT DU DOIGT
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Quand on pose le doigt, on calcule sur quelle case on est
                                val cellWidth = size.width / gridSize
                                val cellHeight = size.height / gridSize
                                val col = (offset.x / cellWidth).toInt()
                                val row = (offset.y / cellHeight).toInt()

                                if (row in 0 until gridSize && col in 0 until gridSize) {
                                    selectedCells = listOf(Pair(row, col))
                                }
                            },
                            onDrag = { change, _ ->
                                // Pendant qu'on glisse, on ajoute les cases traversées
                                val cellWidth = size.width / gridSize
                                val cellHeight = size.height / gridSize
                                val col = (change.position.x / cellWidth).toInt()
                                val row = (change.position.y / cellHeight).toInt()

                                val currentCell = Pair(row, col)
                                if (row in 0 until gridSize && col in 0 until gridSize) {
                                    if (!selectedCells.contains(currentCell)) {
                                        selectedCells = selectedCells + currentCell
                                    }
                                }
                            },
                            onDragEnd = {
                                val motForme = selectedCells.map { grille[it.first][it.second] }.joinToString("")

                                if (motsATrouver.contains(motForme) && !motsTrouves.contains(motForme)) {
                                    // Le mot est bon et pas encore trouvé !
                                    val nouvelleListe = motsTrouves + motForme
                                    motsTrouves = nouvelleListe

                                    // --- NOUVEAU : VÉRIFICATION DE VICTOIRE ---
                                    if (nouvelleListe.size == motsATrouver.size) {
                                        showWinDialog = true // On affiche le pop-up
                                    }
                                }

                                selectedCells = emptyList()
                            }
                        )
                    }
            ) {
                // On dessine visuellement les lignes et colonnes
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0 until gridSize) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (col in 0 until gridSize) {

                                val isSelected = selectedCells.contains(Pair(row, col))
                                val isAlreadyFound = motsTrouves.any { motTrouve ->
                                    // (Astuce simplifiée pour garder les mots trouvés allumés)
                                    // Dans un vrai jeu, on enregistrerait les positions exactes
                                    motTrouve.contains(grille[row][col]) &&
                                            (motTrouve == "IZM" && row == 0 || motTrouve == "FUS" && row == 2)
                                }

                                // Couleur de la case
                                val backgroundColor = when {
                                    isSelected -> Color(0xFFFFCC80) // Orange quand on glisse dessus
                                    isAlreadyFound -> Color(0xFFA5D6A7) // Vert quand c'est validé
                                    else -> Color(0xFFF5F5F5) // Gris clair normal
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(backgroundColor)
                                        .border(2.dp, if (isSelected) Color(0xFFFF9800) else Color.Transparent, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = grille[row][col].toString(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected || isAlreadyFound) Color.Black else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { /* On ne fait rien, on oblige à cliquer sur Continuer */ },
            title = {
                Text("🎉 Niveau Complété !", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            },
            text = {
                Text("Bravo ! Tu as trouvé tous les mots et gagné 5 points.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = auth.currentUser
                        if (user != null) {
                            // On ajoute 5 points et on passe au puzzle 2 !
                            db.collection("users").document(user.uid).update(
                                "score", FieldValue.increment(5),
                                "puzzleActuel", FieldValue.increment(1)
                            ).addOnSuccessListener {
                                showWinDialog = false
                                onBackClick() // On retourne au menu principal
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("Continuer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }
}