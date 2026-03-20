package com.tamazightgames.taguri.data.model

data class WordSearchPuzzle(
    override val id: Int,
    override val level: Int,
    override val rewardPoints: Int = 5,
    val gridSize: Int,
    val grid: List<List<Char>>,
    val wordsToFind: List<String>
) : Puzzle()