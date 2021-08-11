package com.death.sudoku.common

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

object ProductionDispatcherProvider: DispatcherProvider {
    override fun provideUIContext(): CoroutineContext {
        return Dispatchers.Main
    }

    override fun provideIOContext(): CoroutineContext {
        return Dispatchers.IO
    }
}