package com.mpmep.classes

import com.mpmep.plugins.Statistic
import com.mpmep.plugins.StatisticsService
import com.mpmep.plugins.core.ExampleResponse
import com.mpmep.plugins.core.ExampleState
import com.mpmep.plugins.core.Game
import com.mpmep.plugins.core.generateExample
import com.mpmep.respond
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class Room {
    private var playersFinished = mutableMapOf<Int, DefaultWebSocketSession>()
    val examples = List(20) {
        generateExample(it + 1)
    }

    val roomState : MutableSharedFlow<GSWS> = MutableSharedFlow()

    val id: String = UUID.randomUUID().toString()
    val players = mutableListOf<DefaultWebSocketSession>()
    val games = mutableListOf<Game>()
    suspend fun startGame(session:DefaultWebSocketSession, gender:String, age:String) {
        val game = Game(examples, session)
        games.add(game)

        var lastTime:Long = System.currentTimeMillis()
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
            } else if (example is ExampleState.Example) {
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
                    } else {
                        if (game.checkAnswer(response.answer)){
                            val deltaTime = System.currentTimeMillis() - lastTime
                            lastTime = System.currentTimeMillis()
                            if (game.currentExample.value is ExampleState.Example) {
                                val statistic = Statistic(
                                    age.toInt(),
                                    gender,
                                    deltaTime,
                                    (game.currentExample.value as ExampleState.Example).difficulty,
                                    (game.currentExample.value as ExampleState.Example).op
                                )
                                StatisticsService.create(statistic)
                            }
                        }
                    }
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