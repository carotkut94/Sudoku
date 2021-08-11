package com.death.sudoku.domain

enum class Difficulty(val modifier: Double) {
    EASY(modifier = 0.50),
    MEDIUM(modifier = 0.44),
    HARD(modifier = 0.38)
}