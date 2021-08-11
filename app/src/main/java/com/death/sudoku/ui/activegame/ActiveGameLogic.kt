package com.death.sudoku.ui.activegame

import com.death.sudoku.common.BaseLogic
import com.death.sudoku.common.DispatcherProvider
import com.death.sudoku.domain.IGameRepository
import com.death.sudoku.domain.IStatisticsRepository
import com.death.sudoku.domain.SudokuPuzzle
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActiveGameLogic(
    private val container: ActiveGameContainer?,
    private val viewModel: ActiveGameViewModel,
    private val gameRepo: IGameRepository,
    private val statsRepo: IStatisticsRepository,
    private val dispatcher: DispatcherProvider,
) : BaseLogic<ActiveGameEvent>(), CoroutineScope {

    init {
        jobTracker = Job()
    }

    override val coroutineContext: CoroutineContext
        get() = dispatcher.provideUIContext() + jobTracker

    inline fun startCoroutineTimer(
        crossinline action: () -> Unit,
    ) = launch {
        while (true) {
            action()
            delay(1000)
        }
    }

    private var timeTrackerJob: Job? = null

    private val Long.timeOffset: Long
        get() {
            return if (this <= 0) 0 else this - 1
        }

    override fun onEvent(event: ActiveGameEvent) {
        when (event) {
            is ActiveGameEvent.OnInput -> onInput(
                event.input,
                viewModel.timerState
            )
            ActiveGameEvent.OnNewGameClicked -> onNewGameClicked()
            ActiveGameEvent.OnStart -> onStart()
            ActiveGameEvent.OnStop -> onStop()
            is ActiveGameEvent.OnTileFocused -> onTileFocused(event.x, event.y)
        }
    }

    private fun onTileFocused(x: Int, y: Int) {
        viewModel.updateFocusState(x, y)
    }

    private fun onStop() {
        if (!viewModel.isCompleteState) {
            launch {
                gameRepo.saveGame(viewModel.timerState.timeOffset,
                    { cancelStuff() }, {
                        cancelStuff()
                        container?.showError()
                    })
            }
        } else {
            cancelStuff()
        }
    }

    private fun onStart() = launch {
        gameRepo.getCurrentGame({ puzzle, isComplete ->
            viewModel.initializeBoardState(puzzle, isComplete)
            if (!isComplete) timeTrackerJob = startCoroutineTimer {
                viewModel.updateTimerState()
            }
        }, {
            container?.onNewGameClick()
        })
    }


    private fun onNewGameClicked() = launch {
        viewModel.showLoadingState()
        if (!viewModel.isCompleteState) {
            gameRepo.getCurrentGame(
                { puzzle, _ ->
                    updateWithTime(puzzle)
                },
                {
                    container?.showError()
                }
            )
        } else {
            navigateToNewGame()
        }
    }

    private fun updateWithTime(puzzle: SudokuPuzzle) = launch {
        gameRepo.updateGame(puzzle.copy(elapsedTime = viewModel.timerState.timeOffset),
            {
                navigateToNewGame()
            }, {
                container?.showError()
                navigateToNewGame()
            }
        )
    }

    private fun navigateToNewGame() {
        cancelStuff()
        container?.onNewGameClick()
    }

    private fun cancelStuff() {
        if (timeTrackerJob?.isCancelled == false) timeTrackerJob?.cancel()
        jobTracker.cancel()
    }


    private fun onInput(input: Int, elapsedTime: Long) = launch {
        var focusedTile: SudokuTile? = null
        viewModel.boardState.values.forEach {
            if (it.hasFocus) focusedTile = it
        }

        if (focusedTile != null) {
            gameRepo.updateNode(
                focusedTile!!.x,
                focusedTile!!.y,
                input,
                elapsedTime, { isComplete ->
                    focusedTile?.let {
                        viewModel.updateBoardState(
                            it.x,
                            it.y,
                            input,
                            false
                        )
                    }

                    if (isComplete) {
                        timeTrackerJob?.cancel()
                        checkIfNewRecord()
                    }
                }, {
                    container?.showError()
                }
            )
        }
    }

    private fun checkIfNewRecord() = launch {
        statsRepo.updateStatistic(
            viewModel.timerState,
            viewModel.difficulty,
            viewModel.boundary,
            { isRecord ->
                viewModel.isNewRecordState = isRecord
            },
            {
                container?.showError()
                viewModel.updateCompleteState()
            }
        )
    }
}