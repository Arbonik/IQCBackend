package com.mpmep.plugins

import com.mpmep.classes.GameStatus
import com.mpmep.classes.Room
import com.mpmep.respond
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Application.configureRouting() {
    val rooms = mutableListOf<Room>()
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
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Room id was excepted")
                val room = rooms.find {
                    it.id == id
                } ?: throw IllegalArgumentException("Room was not found")
                room.addPlayer(this)
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
                            room.startGame(this)
                        }
                        GameStatus.SHUTDOWN -> {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Room was closed"))
                        }
                        GameStatus.GOT_NEW_EXAMPLE -> {
                            if (gameStatus.receiver != this){
                                respond(GameStatus.GOT_NEW_EXAMPLE)
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
