package com.death.sudoku.ui.activegame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import com.death.sudoku.R
import com.death.sudoku.common.makeToast
import com.death.sudoku.ui.GraphSudokuTheme
import com.death.sudoku.ui.activegame.buildlogic.buildActiveGameLogic
import kotlin.math.log

class ActiveGameActivity : AppCompatActivity(), ActiveGameContainer {

    private lateinit var logic: ActiveGameLogic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ActiveGameViewModel()
        setContent {
            GraphSudokuTheme {
                ActiveGameScreen(
                    onEventHandler = logic::onEvent,
                    viewModel
                )
            }
        }

        logic = buildActiveGameLogic(this, viewModel, applicationContext)
    }

    override fun showError() = makeToast(getString(R.string.generic_error))

    override fun onNewGameClick() {
        startActivity(
            Intent(
                this,
                NewGameActivity::class.java
            )
        )
    }

    override fun onStart() {
        super.onStart()
        logic.onEvent(ActiveGameEvent.OnStart)
    }

    override fun onStop() {
        super.onStop()
        logic.onEvent(ActiveGameEvent.OnStop)
    }
}