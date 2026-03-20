package com.tamazightgames.taguri.domain.engine

import com.tamazightgames.taguri.data.model.WordSearchPuzzle

class WordSearchEngine(val puzzle: WordSearchPuzzle) {

    // On garde en mémoire les mots qu'il reste à trouver
    private val remainingWords = puzzle.wordsToFind.toMutableSet()

    // On garde en mémoire les mots déjà validés
    val foundWords = mutableSetOf<String>()

    /**
     * Cette fonction est le cœur du jeu.
     * Elle reçoit la liste des coordonnées (Ligne, Colonne) que le doigt a traversées.
     * Elle retourne le mot trouvé (ex: "IZM"), ou 'null' si ce n'est pas le bon mot.
     */
    fun validateSelection(selectedCells: List<Pair<Int, Int>>): String? {
        if (selectedCells.isEmpty()) return null

        // 1. On reconstruit le mot à partir des lettres de la grille
        val formedWord = selectedCells.map { (row, col) ->
            puzzle.grid[row][col]
        }.joinToString("")

        // 2. Astuce de jeu : on vérifie à l'endroit ET à l'envers
        // (car le joueur peut glisser de droite à gauche ou de bas en haut !)
        val reversedWord = formedWord.reversed()

        // 3. On cherche si l'un de ces deux mots est dans la liste des mots restants
        val validWord = when {
            remainingWords.contains(formedWord) -> formedWord
            remainingWords.contains(reversedWord) -> reversedWord
            else -> null
        }

        // 4. Si c'est un mot valide, on met à jour le score interne
        if (validWord != null) {
            foundWords.add(validWord)
            remainingWords.remove(validWord)
            return validWord
        }

        return null // Ce n'était pas un mot de la liste
    }

    /**
     * Vérifie si le joueur a trouvé tous les mots.
     */
    fun isGameWon(): Boolean {
        return remainingWords.isEmpty()
    }
}