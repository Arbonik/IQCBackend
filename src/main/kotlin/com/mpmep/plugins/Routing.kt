package com.mpmep.plugins

import com.mpmep.classes.GameStatus
import com.mpmep.classes.Room
import com.mpmep.plugins.Repository.games
import com.mpmep.plugins.Repository.rooms
import com.mpmep.plugins.core.Game
import com.mpmep.respond
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.LinkedHashSet

object Repository {
    val rooms = Collections.synchronizedSet<Room>(LinkedHashSet())
    val games = mutableMapOf<DefaultWebSocketSession, Game>()
}

fun Application.configureRouting() {

    routing {
        route("/rooms") {
            get {
                val filteredRooms = rooms.filter {
                    it.players.size == 1
                }
                call.respond(filteredRooms.map(Room::toModel))
            }
            post {

                val room = Room()
                rooms.add(room)
                call.respond(room.toModel())
            }
            webSocket("/{id?}") {
                val age = call.request.queryParameters["age"] ?: "0"
                val gender = call.request.queryParameters["gender"] ?: "NA"
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Room id was excepted")
                val room = rooms.find {
                    it.id == id
                } ?: throw IllegalArgumentException("Room was not found")
                room.addPlayer(this)
                launch {
                    closeReason.await()
                    room.deletePlayer(this@webSocket)
                }
                room.roomState.collect { gameStatus ->
                    when (gameStatus.gameStatus){
                        GameStatus.WIN -> {
                            if (gameStatus.receiver == this) {
                                respond(GameStatus.WIN)
                            } else {
                                respond(GameStatus.LOSE)
                            }
                        }
                        GameStatus.AWAIT -> {
                            respond(GameStatus.AWAIT)
                        }
                        GameStatus.READY -> {
                            respond(GameStatus.READY)
                            room.startGame(this, gender, age)
                        }
                        GameStatus.SHUTDOWN -> {
                            rooms.remove(room)
                            close(CloseReason(CloseReason.Codes.NORMAL, "Room was closed"))
                        }
                        GameStatus.GOT_NEW_EXAMPLE -> {
                            if (gameStatus.receiver != this){
                                val enemy = room.players.find {
                                    it != gameStatus.receiver
                                }
                                val enemyGame = games[enemy] ?: throw Exception()
                                respond(GameStatus.GOT_NEW_EXAMPLE, enemyGame._currentExample.value)
                            }
                        }
                        GameStatus.FALSE -> {
                            if (gameStatus.receiver == this) {
                                respond(GameStatus.FALSE)
                            }
                        }
                        GameStatus.FINISH -> {
                            if (gameStatus.receiver == this) {
                                respond(GameStatus.FINISH)
                            }
                        }
                        else ->{}
                    }
                }

            }
        }
    }
}
