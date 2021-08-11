package com.death.sudoku.common

import kotlinx.coroutines.Job

abstract class BaseLogic<E> {
    protected lateinit var jobTracker: Job
    abstract fun onEvent(event: E)
}