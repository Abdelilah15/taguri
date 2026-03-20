package com.tamazightgames.taguri.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tamazightgames.taguri.ui.viewmodel.PlayViewModel
import com.tamazightgames.taguri.ui.viewmodel.PlayViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(niveau: Int, onBackClick: () -> Unit) {
    val context = LocalContext.current

    // 1. INSTANCIATION DU CHEF D'ORCHESTRE (VIEWMODEL)
    val viewModel: PlayViewModel = viewModel(
        factory = PlayViewModelFactory(context, niveau)
    )

    // 2. OBSERVATION DES ÉTATS (L'écran se redessine si ça change)
    val puzzle by viewModel.puzzle.collectAsState()
    val selectedCells by viewModel.selectedCells.collectAsState()
    val foundWords by viewModel.foundWords.collectAsState()
    val isGameWon by viewModel.isGameWon.collectAsState()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Niveau $niveau", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Retour") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->

        // Si le puzzle n'est pas encore chargé depuis le JSON, on met un cercle d'attente
        if (puzzle == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
            return@Scaffold
        }

        val currentPuzzle = puzzle!!
        val gridSize = currentPuzzle.gridSize

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Galerie d'images en haut (statique pour l'instant)
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

            // Compteur de mots trouvés dynamiques
            Text(
                text = "Trouvés : ${foundWords.joinToString(", ")} / ${currentPuzzle.wordsToFind.size}",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // LA GRILLE INTERACTIVE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Reste un carré parfait
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            // On envoie juste les coordonnées au ViewModel !
                            onDragStart = { offset ->
                                val cellWidth = size.width / gridSize
                                val cellHeight = size.height / gridSize
                                val col = (offset.x / cellWidth).toInt()
                                val row = (offset.y / cellHeight).toInt()
                                if (row in 0 until gridSize && col in 0 until gridSize) {
                                    viewModel.startSelection(row, col)
                                }
                            },
                            onDrag = { change, _ ->
                                val cellWidth = size.width / gridSize
                                val cellHeight = size.height / gridSize
                                val col = (change.position.x / cellWidth).toInt()
                                val row = (change.position.y / cellHeight).toInt()
                                if (row in 0 until gridSize && col in 0 until gridSize) {
                                    viewModel.onCellSelected(row, col)
                                }
                            },
                            onDragEnd = {
                                viewModel.endSelection()
                            }
                        )
                    }
            ) {
                // DESSIN DES CASES
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0 until gridSize) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (col in 0 until gridSize) {
                                val isSelected = selectedCells.contains(Pair(row, col))
                                val currentLetter = currentPuzzle.grid[row][col]

                                // Simplification visuelle : si la lettre fait partie d'un mot trouvé, on la colorie
                                val isAlreadyFound = foundWords.any { it.contains(currentLetter) }

                                val backgroundColor = when {
                                    isSelected -> Color(0xFFFFCC80) // Orange quand on glisse
                                    isAlreadyFound -> Color(0xFFA5D6A7) // Vert quand validé
                                    else -> Color(0xFFF5F5F5) // Gris normal
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
                                        text = currentLetter.toString(),
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

    // 3. LE POP-UP DE VICTOIRE (Se déclenche tout seul si isGameWon devient 'true')
    if (isGameWon) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("🎉 Niveau Complété !", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            },
            text = {
                // On récupère les points dynamiquement depuis le JSON !
                Text("Bravo ! Tu as gagné ${puzzle?.rewardPoints ?: 0} points.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = auth.currentUser
                        if (user != null) {
                            // On ajoute les vrais points du niveau et on passe au niveau suivant
                            db.collection("users").document(user.uid).update(
                                "score", FieldValue.increment((puzzle?.rewardPoints ?: 0).toLong()),
                                "puzzleActuel", FieldValue.increment(1)
                            ).addOnSuccessListener {
                                onBackClick()
                            }
                        } else {
                            onBackClick() // Sécurité si on joue hors ligne/sans compte
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
