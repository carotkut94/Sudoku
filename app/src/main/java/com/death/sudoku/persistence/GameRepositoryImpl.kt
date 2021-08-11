package com.death.sudoku.persistence

import com.death.sudoku.domain.GameStorageResult
import com.death.sudoku.domain.IGameDataStorage
import com.death.sudoku.domain.IGameRepository
import com.death.sudoku.domain.ISettingsStorage
import com.death.sudoku.domain.Settings
import com.death.sudoku.domain.SettingsStorageResult
import com.death.sudoku.domain.SudokuPuzzle

class GameRepositoryImpl(
    private val gameStorage: IGameDataStorage,
    private val settingsStorage: ISettingsStorage
): IGameRepository {
    override suspend fun saveGame(
        elapsedTime: Long,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when(val getCurrentGameResult = gameStorage.getCurrentGame()){
            is GameStorageResult.OnError -> onError(getCurrentGameResult.exception)
            is GameStorageResult.OnSuccess -> {
                gameStorage.updateGame(getCurrentGameResult.currentGame)
                onSuccess(Unit)
            }
        }
    }

    override suspend fun updateGame(
        game: SudokuPuzzle,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when(val updateGameResult: GameStorageResult = gameStorage.updateGame(game)){
            is GameStorageResult.OnError -> onError(updateGameResult.exception)
            is GameStorageResult.OnSuccess -> onSuccess(Unit)
        }
    }

    override suspend fun createNewGame(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when(val updateSettingsResult = settingsStorage.updateSettings(settings)){
            is SettingsStorageResult.OnError -> onError(updateSettingsResult.exception)
            is SettingsStorageResult.OnSuccess -> {
                when(val updateGameResult = createAndWriteNewGame(settings)){
                    is GameStorageResult.OnError -> onError(updateGameResult.exception)
                    is GameStorageResult.OnSuccess -> onSuccess(Unit)
                }
            }

        }
    }

    private suspend fun createAndWriteNewGame(settings: Settings): GameStorageResult{
        return gameStorage.updateGame(
            SudokuPuzzle(
                settings.boundary,
                settings.difficulty
            )
        )
    }

    override suspend fun updateNode(
        x: Int,
        y: Int,
        color: Int,
        elapsedTime: Long,
        onSuccess: (isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when(val result = gameStorage.updateNode(x, y, color, elapsedTime)){
            is GameStorageResult.OnError -> onError(result.exception)
            is GameStorageResult.OnSuccess -> onSuccess(
                puzzleComplete(result.currentGame)
            )
        }
    }

    private fun puzzleComplete(currentGame: SudokuPuzzle): Boolean {

    }

    override suspend fun getCurrentGame(
        onSuccess: (currentGame: SudokuPuzzle, isComplete: Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        when(val currentGameResult = gameStorage.getCurrentGame()){
            is GameStorageResult.OnError -> {
                when(val getSettingsResult = settingsStorage.getSettings()){
                    is SettingsStorageResult.OnError -> onError(getSettingsResult.exception)
                    is SettingsStorageResult.OnSuccess -> {
                        when(val updateGameResult = createAndWriteNewGame(getSettingsResult.settings)){
                            is GameStorageResult.OnError -> onError(updateGameResult.exception)
                            is GameStorageResult.OnSuccess -> onSuccess(
                                updateGameResult.currentGame,
                                puzzleComplete(updateGameResult.currentGame)
                            )
                        }
                    }
                }
            }
            is GameStorageResult.OnSuccess -> onSuccess(
                currentGameResult.currentGame,
                puzzleComplete(currentGameResult.currentGame)
            )
        }
    }

    override suspend fun getSettings(onSuccess: (Settings) -> Unit, onError: (Exception) -> Unit) {
        when(val settingsResult = settingsStorage.getSettings()) {
            is SettingsStorageResult.OnError -> onError(settingsResult.exception)
            is SettingsStorageResult.OnSuccess -> onSuccess(settingsResult.settings)
        }
    }

    override suspend fun updateSetting(
        settings: Settings,
        onSuccess: (Unit) -> Unit,
        onError: (Exception) -> Unit
    ) {
        settingsStorage.updateSettings(settings)
        onSuccess(Unit)
    }

}