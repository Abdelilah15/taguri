package com.tamazightgames.taguri.data.model

data class Chapter(
    val id: Int,
    val title: String,
    val description: String,
    val puzzles: List<Puzzle>
)