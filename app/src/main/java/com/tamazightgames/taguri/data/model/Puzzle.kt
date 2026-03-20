package com.tamazightgames.taguri.data.model

sealed class Puzzle {
    abstract val id: Int
    abstract val level: Int
    abstract val rewardPoints: Int
}