package com.death.sudoku.ui.activegame

import android.os.health.TimerStat
import android.widget.Space
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.death.sudoku.R
import com.death.sudoku.common.toTime
import com.death.sudoku.computation.sqrt
import com.death.sudoku.ui.activeGameSubtitle
import com.death.sudoku.ui.components.AppToolbar
import com.death.sudoku.ui.components.LoadingScreen
import com.death.sudoku.ui.inputButton
import com.death.sudoku.ui.mutableSudokuSquare
import com.death.sudoku.ui.readOnlySudokuSquare
import com.death.sudoku.ui.textColorDark
import com.death.sudoku.ui.textColorLight
import com.death.sudoku.ui.userInputtedNumberDark
import com.death.sudoku.ui.userInputtedNumberLight

enum class ActiveGameScreenState {
    LOADING,
    ACTIVE,
    COMPLETE
}

@Composable
fun ActiveGameScreen(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel
) {
    val contentTransitionState = remember {
        MutableTransitionState(
            ActiveGameScreenState.LOADING
        )
    }

    viewModel.subContentState = {
        contentTransitionState.targetState = it
    }

    val transition = updateTransition(contentTransitionState, label = "transition_state")

    val loadingAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300)
        }, label = "loading_spec"
    ) {
        if (it == ActiveGameScreenState.LOADING) 1f else 0f
    }

    val activeAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300)
        }, label = "loading_spec"
    ) {
        if (it == ActiveGameScreenState.ACTIVE) 1f else 0f
    }

    val completeAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300)
        }, label = "loading_spec"
    ) {
        if (it == ActiveGameScreenState.COMPLETE) 1f else 0f
    }

    Column(
        Modifier
            .background(MaterialTheme.colors.primary)
            .fillMaxHeight()
    ) {
        AppToolbar(
            modifier = Modifier.wrapContentHeight(),
            title = stringResource(id = R.string.app_name)
        ) {
            NewGameIcon(onEventHandler = onEventHandler)
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (contentTransitionState.currentState) {
                ActiveGameScreenState.ACTIVE -> Box(
                    Modifier.alpha(activeAlpha)
                ) {
                    GameContent(onEventHandler, viewModel)
                }
                ActiveGameScreenState.COMPLETE -> Box(
                    Modifier.alpha(completeAlpha)
                ) {
                    GameCompleteContent(viewModel.timerState, viewModel.isNewRecordState)
                }
                ActiveGameScreenState.LOADING -> Box(
                    Modifier.alpha(loadingAlpha)
                ) {
                    LoadingScreen()
                }
            }
        }
    }
}

@Composable
fun NewGameIcon(onEventHandler: (ActiveGameEvent) -> Unit) {
    Icon(
        imageVector = Icons.Filled.Add,
        tint = if (MaterialTheme.colors.isLight) textColorLight else textColorDark,
        contentDescription = null,
        modifier = Modifier
            .clickable {
                onEventHandler.invoke(ActiveGameEvent.OnNewGameClicked)
            }
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(36.dp)
    )
}

@Composable
fun GameContent(onEventHandler: (ActiveGameEvent) -> Unit, viewModel: ActiveGameViewModel) {
    BoxWithConstraints {
        val screenWidth = with(LocalDensity.current) {
            constraints.maxWidth.toDp()
        }
        val margin = with(LocalDensity.current) {
            when {
                constraints.maxHeight.toDp().value < 500 -> 20
                constraints.maxHeight.toDp().value < 550 -> 20
                else -> 0
            }
        }

        ConstraintLayout {
            val (board, timer, diff, inputs) = createRefs()
            Box(
                Modifier
                    .constrainAs(board) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .background(MaterialTheme.colors.surface)
                    .size(screenWidth - margin.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colors.primaryVariant
                    )
            ) {
                SudokuBoard(
                    onEventHandler,
                    viewModel,
                    screenWidth - margin.dp
                )
            }

            Row(
                Modifier
                    .wrapContentSize()
                    .constrainAs(diff) {
                        top.linkTo(board.bottom)
                        end.linkTo(parent.end)
                    }
            ) {
                (0..viewModel.difficulty.ordinal).forEach { _ ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(id = R.string.difficulty),
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 4.dp)
                    )
                }
            }

            Box(
                Modifier
                    .wrapContentSize()
                    .constrainAs(timer) {
                        top.linkTo(board.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(start = 16.dp)
            ) {
                TimerText(viewModel)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .constrainAs(inputs) {
                        top.linkTo(timer.bottom)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.boundary == 4) {
                    InputButtonRow(
                        (0..4).toList(),
                        onEventHandler
                    )
                } else {
                    InputButtonRow(
                        (0..4).toList(),
                        onEventHandler
                    )
                    InputButtonRow(
                        (5..9).toList(),
                        onEventHandler
                    )
                }
            }
        }
    }
}

@Composable
fun InputButtonRow(numbers: List<Int>, onEventHandler: (ActiveGameEvent) -> Unit) {
    Row {
        numbers.forEach {
            SudokuInputButton(
                onEventHandler,
                it
            )
        }
    }

    Spacer(modifier = Modifier.size(2.dp))
}

@Composable
fun SudokuInputButton(onEventHandler: (ActiveGameEvent) -> Unit, number: Int) {
    TextButton(
        onClick = {
            onEventHandler.invoke(ActiveGameEvent.OnInput(number))
        },
        border = BorderStroke(
            ButtonDefaults.OutlinedBorderSize, MaterialTheme.colors.onPrimary
        ),
        modifier = Modifier
            .requiredSize(56.dp)
            .padding(2.dp)
    ) {
        Text(
            text = number.toString(),
            style = inputButton.copy(
                color = MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TimerText(viewModel: ActiveGameViewModel) {
    var timeState by remember {
        mutableStateOf("")
    }

    viewModel.subTimerState = {
        timeState = it.toTime()
    }

    Text(
        modifier = Modifier.requiredHeight(36.dp),
        text = timeState,
        style = activeGameSubtitle.copy(
            color = MaterialTheme.colors.secondary
        )
    )
}

@Composable
fun SudokuBoard(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel,
    size: Dp
) {
    val boundary = viewModel.boundary
    val tileOffset = size.value / boundary

    var boardState by remember {
        mutableStateOf(viewModel.boardState, neverEqualPolicy())
    }
    viewModel.subBoardState = {
        boardState = it
    }

    SudokuTextFields(
        onEventHandler,
        tileOffset,
        boardState
    )

    BoardGrid(
        boundary,
        tileOffset
    )
}

@Composable
fun BoardGrid(boundary: Int, tileOffset: Float) {
    (1 until boundary).forEach {
        val width = if (it % boundary.sqrt == 0) 3.dp else 1.dp
        Divider(
            color = MaterialTheme.colors.primaryVariant,
            modifier = Modifier
                .absoluteOffset((tileOffset * it).dp, 0.dp)
                .fillMaxHeight()
                .width(width)
        )

        val height = if (it % boundary.sqrt == 0) 3.dp else 1.dp
        Divider(
            color = MaterialTheme.colors.primaryVariant,
            modifier = Modifier
                .absoluteOffset((tileOffset * it).dp, 0.dp)
                .fillMaxWidth()
                .height(height)
        )
    }
}

@Composable
fun SudokuTextFields(
    onEventHandler: (ActiveGameEvent) -> Unit,
    tileOffset: Float,
    boardState: HashMap<Int, SudokuTile>
) {
    boardState.values.forEach { tile ->
        var text = tile.value.toString()
        if (!tile.readOnly) {
            if (text == "0") text = ""

            Text(
                text = text,
                style = mutableSudokuSquare(tileOffset = tileOffset).copy(
                    color = if (MaterialTheme.colors.isLight) userInputtedNumberLight else userInputtedNumberDark
                ),
                modifier = Modifier
                    .absoluteOffset(
                        (tileOffset * (tile.x - 1)).dp,
                        (tileOffset * (tile.y - 1)).dp
                    )
                    .size(tileOffset.dp)
                    .background(
                        if (tile.hasFocus) MaterialTheme.colors.onPrimary.copy(
                            alpha = .25f
                        ) else MaterialTheme.colors.surface
                    )
                    .clickable {
                        onEventHandler.invoke(ActiveGameEvent.OnTileFocused(tile.x, tile.y))
                    }
            )
        } else {
            Text(
                text = text,
                style = readOnlySudokuSquare(tileOffset = tileOffset),
                modifier = Modifier
                    .absoluteOffset(
                        (tileOffset * (tile.x - 1)).dp,
                        (tileOffset * (tile.y - 1)).dp
                    )
                    .size(tileOffset.dp)
            )
        }
    }
}


@Composable
fun GameCompleteContent(timerState: Long, isNewRecordState: Boolean){
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ){
            Image(
                contentDescription = stringResource(id = R.string.game_complete),
                imageVector = Icons.Filled.EmojiEvents,

            )
        }
    }
}