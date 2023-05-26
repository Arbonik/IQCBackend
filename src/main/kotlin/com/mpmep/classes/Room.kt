package com.mpmep.classes

import com.mpmep.plugins.core.ExampleState
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
    private var playersFinished = mutableMapOf<Int, DefaultWebSocketSession>()
    val examples = List(20) {
        generateExample()
    }

    val roomState : MutableStateFlow<GameStatus> = MutableStateFlow(GameStatus.AWAIT)

    val id: String = UUID.randomUUID().toString()
    val players = mutableListOf<DefaultWebSocketSession>()
    val games = mutableListOf<Game>()
    suspend fun startGame(session:DefaultWebSocketSession) {
        val game = Game(examples, session)
        games.add(game)
        game.currentExample.onEach { example ->
            if (example is ExampleState.ExampleEnd) {
                playersFinished[playersFinished.size + 1] = session
                roomState.value = GameStatus.FINISH(session)
                if(playersFinished.size >= 2) {
                    val enemyGame = games.find {
                        it != game
                    } ?: throw Exception("lol")
                    if (game.quality() > enemyGame.quality()) {
                        roomState.value = GameStatus.WIN(session)
                    } else if (game.quality() == enemyGame.quality()) {
                        roomState.value = GameStatus.WIN(playersFinished[1])
                    }
                    roomState.value = GameStatus.SHUTDOWN
                }
            } else {
                val enemy = players.find {
                    it != session
                } ?: throw Exception("lol")
                roomState.value = GameStatus.ENEMY_GOT_NEW_EXAMPLE(enemy)
                val exampleString = Json.encodeToString(example)
                session.send(Frame.Text(exampleString))
            }
        }.launchIn(session)

        game.userMisstake.onEach { _ ->
            roomState.value = GameStatus.FALSE(session)
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