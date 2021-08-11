package com.death.sudoku.ui.activegame.buildlogic

import android.content.Context
import com.death.sudoku.common.ProductionDispatcherProvider
import com.death.sudoku.persistence.GameRepositoryImpl
import com.death.sudoku.persistence.LocalGameStorageImpl
import com.death.sudoku.persistence.LocalSettingsStorageImpl
import com.death.sudoku.persistence.LocalStatisticsStorageImpl
import com.death.sudoku.persistence.settingsDataStore
import com.death.sudoku.persistence.statsDataStore
import com.death.sudoku.ui.activegame.ActiveGameContainer
import com.death.sudoku.ui.activegame.ActiveGameLogic
import com.death.sudoku.ui.activegame.ActiveGameViewModel

internal fun buildActiveGameLogic(
    container: ActiveGameContainer,
    viewModel: ActiveGameViewModel,
    context: Context
): ActiveGameLogic{
    return ActiveGameLogic(
        container,
        viewModel,
        GameRepositoryImpl(
            LocalGameStorageImpl(context.filesDir.path),
            LocalSettingsStorageImpl(context.settingsDataStore)
        ),
        LocalStatisticsStorageImpl(context.statsDataStore),
        ProductionDispatcherProvider
    )
}