package com.mpmep.classes

import com.mpmep.plugins.core.Game
import com.mpmep.plugins.core.generateExample
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class Room {
    val examples = List(20) {
        generateExample()
    }

    val roomState : MutableStateFlow<GameStatus> = MutableStateFlow(GameStatus.AWAIT)

    val id: String = UUID.randomUUID().toString()
    val players = mutableListOf<DefaultWebSocketSession>()
    suspend fun startGame(session:DefaultWebSocketSession) {
        val game = Game(examples, session)

        game.currentExample.onEach { example ->
            val exampleString = Json.encodeToString(example)
            session.send(Frame.Text(exampleString))
        }.launchIn(session)

        game.userMisstake.onEach { _ ->
            val statusString = Json.encodeToString(GameStatus.BAD)
            session.send(Frame.Text(statusString))
        }.launchIn(session)

        session.launch {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    game.checkAnswer(text.toInt())
                }
            }
        }
    }
    fun addPlayer(player:DefaultWebSocketSession) {
        players.add(player)
        if (players.size <= 1) {
            roomState.value = GameStatus.AWAIT
        }
        if (players.size >= 2) {
            roomState.value = GameStatus.READY
        }
    }
}