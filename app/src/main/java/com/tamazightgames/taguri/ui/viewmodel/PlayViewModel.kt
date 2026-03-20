package com.tamazightgames.taguri.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tamazightgames.taguri.data.model.WordSearchPuzzle
import com.tamazightgames.taguri.data.repository.PuzzleRepository
import com.tamazightgames.taguri.domain.engine.WordSearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayViewModel(private val repository: PuzzleRepository, private val levelToLoad: Int) : ViewModel() {

    // Notre "Cerveau"
    private var engine: WordSearchEngine? = null

    // --- ÉTATS OBSERVABLES PAR L'INTERFACE (UI) ---

    // Le puzzle actuel (pour dessiner la grille)
    private val _puzzle = MutableStateFlow<WordSearchPuzzle?>(null)
    val puzzle: StateFlow<WordSearchPuzzle?> = _puzzle.asStateFlow()

    // Les cases actuellement survolées par le doigt
    private val _selectedCells = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val selectedCells: StateFlow<List<Pair<Int, Int>>> = _selectedCells.asStateFlow()

    // Les mots déjà validés
    private val _foundWords = MutableStateFlow<Set<String>>(emptySet())
    val foundWords: StateFlow<Set<String>> = _foundWords.asStateFlow()

    // L'état de victoire
    private val _isGameWon = MutableStateFlow(false)
    val isGameWon: StateFlow<Boolean> = _isGameWon.asStateFlow()

    init {
        loadLevel()
    }

    private fun loadLevel() {
        // On charge le puzzle depuis le JSON
        val loadedPuzzle = repository.getPuzzleForLevel(levelToLoad)
        if (loadedPuzzle != null) {
            _puzzle.value = loadedPuzzle
            // On initialise le moteur avec ce puzzle
            engine = WordSearchEngine(loadedPuzzle)
        }
    }

    // --- ACTIONS DÉCLENCHÉES PAR L'INTERFACE ---

    fun startSelection(row: Int, col: Int) {
        _selectedCells.value = listOf(Pair(row, col))
    }

    fun onCellSelected(row: Int, col: Int) {
        val currentList = _selectedCells.value.toMutableList()
        val newCell = Pair(row, col)
        if (!currentList.contains(newCell)) {
            currentList.add(newCell)
            _selectedCells.value = currentList
        }
    }

    fun endSelection() {
        val currentEngine = engine ?: return

        // On demande au moteur de valider le mot tracé
        val validWord = currentEngine.validateSelection(_selectedCells.value)

        if (validWord != null) {
            // Le mot est correct ! On met à jour l'UI
            _foundWords.value = currentEngine.foundWords.toSet()

            // A-t-on gagné la partie ?
            if (currentEngine.isGameWon()) {
                _isGameWon.value = true
            }
        }

        // On efface la trace du doigt quoi qu'il arrive
        _selectedCells.value = emptyList()
    }
}

// Une Factory est nécessaire car notre ViewModel a besoin du `Context` pour lire le fichier JSON
class PlayViewModelFactory(private val context: Context, private val level: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayViewModel::class.java)) {
            val repository = PuzzleRepository(context)
            @Suppress("UNCHECKED_CAST")
            return PlayViewModel(repository, level) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}