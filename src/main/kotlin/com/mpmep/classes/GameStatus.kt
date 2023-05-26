package com.mpmep.classes

import com.mpmep.plugins.core.ExampleState
import com.mpmep.plugins.core.Operate
import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    READY,
    FALSE,
    GOT_NEW_EXAMPLE,
    FINISH,
    WIN,
    LOSE,
    SHUTDOWN,
    AWAIT,
    EMPTY
}

@Serializable
data class WSServerResponse(
    val gameStatus: GameStatus = GameStatus.EMPTY,
    val example: ExampleState = ExampleState.Example(0,0, Operate.MINUS)
)