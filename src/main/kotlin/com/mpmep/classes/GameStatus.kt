package com.mpmep.classes

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