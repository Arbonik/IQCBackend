package com.mpmep.classes

import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    AWAIT,
    READY,
    FALSE,
    ENEMY_ANSWERED,
    FINISH,
    WIN,
    LOSE
}