package com.mpmep.classes

import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    AWAIT,
    READY,
    FALSE,
    WIN,
    LOSE,
    ENEMY_ANSWERED,
}