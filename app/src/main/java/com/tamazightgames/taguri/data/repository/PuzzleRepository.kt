package com.tamazightgames.taguri.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tamazightgames.taguri.data.model.Chapter
import com.tamazightgames.taguri.data.model.WordSearchPuzzle
import java.io.InputStreamReader

// Un objet simple pour représenter la structure globale du JSON
data class GameData(val chapters: List<ChapterJson>)
data class ChapterJson(val id: Int, val title: String, val description: String, val puzzles: List<WordSearchPuzzle>)

class PuzzleRepository(private val context: Context) {

    // Cette fonction lit le fichier JSON et le transforme en liste de Chapitres
    fun loadChapters(): List<Chapter> {
        return try {
            val inputStream = context.assets.open("levels.json")
            val reader = InputStreamReader(inputStream)
            val gameType = object : TypeToken<GameData>() {}.type

            val gameData: GameData = Gson().fromJson(reader, gameType)

            // On convertit le format JSON brut vers nos modèles officiels (créés à l'Étape 1)
            gameData.chapters.map { chapterJson ->
                Chapter(
                    id = chapterJson.id,
                    title = chapterJson.title,
                    description = chapterJson.description,
                    puzzles = chapterJson.puzzles
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Retourne une liste vide en cas d'erreur
        }
    }

    // Récupère un puzzle spécifique en fonction du niveau du joueur
    fun getPuzzleForLevel(level: Int): WordSearchPuzzle? {
        val chapters = loadChapters()
        for (chapter in chapters) {
            for (puzzle in chapter.puzzles) {
                if (puzzle.level == level && puzzle is WordSearchPuzzle) {
                    return puzzle
                }
            }
        }
        return null
    }
}