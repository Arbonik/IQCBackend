package com.mpmep.classes

import io.ktor.websocket.*
import kotlinx.serialization.Serializable

@Serializable
sealed class GameStatus {
    @Serializable
    object READY: GameStatus()
    @Serializable
    class FALSE(val receiver:DefaultWebSocketSession?): GameStatus()
    @Serializable
    class ENEMY_GOT_NEW_EXAMPLE(val receiver:DefaultWebSocketSession?):GameStatus()
    @Serializable
    class FINISH(val receiver:DefaultWebSocketSession?):GameStatus()
    @Serializable
    class WIN(val receiver:DefaultWebSocketSession?):GameStatus()
    @Serializable
    object LOSE:GameStatus()
    @Serializable
    object SHUTDOWN:GameStatus()
    @Serializable
    object AWAIT:GameStatus()
}