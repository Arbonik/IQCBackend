package com.mpmep.classes

import com.mpmep.plugins.core.ExampleResponse
import com.mpmep.plugins.core.ExampleState
import com.mpmep.plugins.core.Game
import com.mpmep.plugins.core.generateExample
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class Room {
    private var playersFinished = mutableMapOf<Int, DefaultWebSocketSession>()
    val examples = List(20) {
        generateExample()
    }

    val roomState : MutableSharedFlow<GSWS> = MutableSharedFlow()

    val id: String = UUID.randomUUID().toString()
    val players = mutableListOf<DefaultWebSocketSession>()
    val games = mutableListOf<Game>()
    suspend fun startGame(session:DefaultWebSocketSession) {
        val game = Game(examples, session)
        games.add(game)
        game.currentExample.onEach { example ->
            if (example is ExampleState.ExampleEnd) {
                playersFinished[playersFinished.size + 1] = session
                roomState.emit(GSWS(GameStatus.FINISH, session))
                if(playersFinished.size >= 2) {
                    val enemyGame = games.find {
                        it != game
                    } ?: throw Exception("lol")
                    if (game.quality() > enemyGame.quality()) {
                        roomState.emit(GSWS(GameStatus.WIN, session))
                    } else if (game.quality() == enemyGame.quality()) {
                        roomState.emit(GSWS(GameStatus.WIN, playersFinished[1]))
                    } else {
                        roomState.emit(GSWS(GameStatus.WIN, players.find { it != session }))
                    }
                    roomState.emit(GSWS(GameStatus.SHUTDOWN))
                }
            } else {
                val exampleString = Json.encodeToString(example)
                session.send(Frame.Text(exampleString))
                roomState.emit(GSWS(GameStatus.GOT_NEW_EXAMPLE, session))
            }
        }.launchIn(session)

        game.userMisstake.onEach { _ ->
            roomState.emit(GSWS(GameStatus.FALSE,session))
        }.launchIn(session)

        session.launch {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val response = Json.decodeFromString<ExampleResponse>(text)
                    if (response.isSkip) {
                        game.skip()
                    } else game.checkAnswer(response.answer)
                }
            }
        }
    }
    fun addPlayer(player:DefaultWebSocketSession) {
        players.add(player)
        if (players.size <= 1) {
            player.launch {
                roomState.emit(GSWS(GameStatus.AWAIT))
            }
        }
        if (players.size >= 2) {
            player.launch {
                roomState.emit(GSWS(GameStatus.READY))
            }
        }
    }
    fun toModel() = RoomRespond(id)
}