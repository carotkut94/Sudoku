package com.death.sudoku.domain

import java.util.LinkedList

data class SudokuPuzzle(
    val boundary: Int,
    val difficulty: Difficulty,
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>> = buildNewSudoku(boundary, difficulty).graph,
    var elapsedTime: Long = 0L
){
    fun getValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = graph
}