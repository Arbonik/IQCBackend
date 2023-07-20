package com.mpmep.classes

import com.mpmep.plugins.core.ExampleState
import com.mpmep.plugins.core.Operate
import kotlinx.serialization.Serializable

@Serializable
data class WSServerResponse(
    val gameStatus: GameStatus = GameStatus.EMPTY,
    val example: ExampleState = ExampleState.Example(0, 0, Operate.MINUS),
    val score: Int? = null
)