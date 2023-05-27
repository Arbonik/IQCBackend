package com.mpmep.classes

import com.mpmep.plugins.core.ExampleResponse
import com.mpmep.plugins.core.ExampleState
import com.mpmep.plugins.core.Game
import com.mpmep.plugins.core.generateExample
import com.mpmep.respond
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class Room {
    private val scope:CoroutineScope = CoroutineScope(Job())
    init {
            scope.launch {
                delay(60000)
                players.forEach {
                    finishPlayer(it, gamesMap[it] ?: throw Exception("bruh"))
                }
            }
    }

    private var playersFinished = mutableMapOf<Int, DefaultWebSocketSession>()
    private val examples = List(20) {
        generateExample()
    }

    val roomState : MutableSharedFlow<GSWS> = MutableSharedFlow()

    val id: String = UUID.randomUUID().toString()
    val players = mutableListOf<DefaultWebSocketSession>()
    val gamesMap = mutableMapOf<DefaultWebSocketSession, Game>()
    private suspend fun finishPlayer(player:DefaultWebSocketSession, playerGame:Game) {
        playersFinished[playersFinished.size + 1] = player
        roomState.emit(GSWS(GameStatus.FINISH, player))
        if(playersFinished.size >= 2) {
            val enemyGame = gamesMap.filterKeys {
                it != player
            }.toList()[0].second
            if (playerGame.quality() > enemyGame.quality()) {
                roomState.emit(GSWS(GameStatus.WIN, player))
            } else if (playerGame.quality() == enemyGame.quality()) {
                roomState.emit(GSWS(GameStatus.WIN, playersFinished[1]))
            } else {
                roomState.emit(GSWS(GameStatus.WIN, players.find { it != player }))
            }
            roomState.emit(GSWS(GameStatus.SHUTDOWN))
        }
    }
    suspend fun startGame(session:DefaultWebSocketSession) {
        val game = Game(examples, session)
        gamesMap[session] = game
        game.currentExample.onEach { example ->
            if (example is ExampleState.ExampleEnd) {
                finishPlayer(session, game)
            } else {
                session.respond(example)
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