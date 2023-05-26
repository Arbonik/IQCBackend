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
                val statusString = Json.encodeToString(GameStatus.FINISH)
                session.send(Frame.Text(statusString))
                if(playersFinished.size >= 2) {
                    val loseStatus = Json.encodeToString(GameStatus.LOSE)
                    val winStatus = Json.encodeToString(GameStatus.WIN)
                    val enemy = players.find {
                        it != session
                    } ?: throw Exception("lol")
                    val enemyGame = games.find {
                        it != game
                    } ?: throw Exception("lol")
                    if (game.quality() > enemyGame.quality()) {
                        session.send(Frame.Text(winStatus))
                        enemy.send(Frame.Text(loseStatus))
                    } else if (game.quality() == enemyGame.quality()) {
                        playersFinished[1]?.send(Frame.Text(winStatus))
                        playersFinished[2]?.send(Frame.Text(loseStatus))
                    }
                }
            } else {
                val enemy = players.find {
                    it != session
                } ?: throw Exception("lol")
                val statusString = Json.encodeToString(GameStatus.ENEMY_ANSWERED)
                enemy.send(Frame.Text(statusString))
                val exampleString = Json.encodeToString(example)
                session.send(Frame.Text(exampleString))
            }
        }.launchIn(session)

        game.userMisstake.onEach { _ ->
            val statusString = Json.encodeToString(GameStatus.FALSE)
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