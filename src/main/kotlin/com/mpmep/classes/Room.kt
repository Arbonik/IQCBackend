package com.mpmep.classes

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class Room {
    val id: String = UUID.randomUUID().toString()
    val players = mutableListOf<DefaultWebSocketSession>()
    suspend fun addPlayer(player:DefaultWebSocketSession) {
        players.add(player)
        if (players.size <= 1) {
            players.forEach {
                val status = Json.encodeToString(GameStatus.AWAIT)
                it.outgoing.send(Frame.Text(status))
            }
        }
        if (players.size >= 2) {
            players.forEach {
                val status = Json.encodeToString(GameStatus.READY)
                it.outgoing.send(Frame.Text(status))
            }
        }
    }
}