package com.mpmep.plugins

import com.mpmep.classes.GameStatus
import com.mpmep.classes.Room
import com.mpmep.classes.WSServerResponse
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
                            val loseStatus = Json.encodeToString(WSServerResponse(GameStatus.LOSE))
                            val winStatus = Json.encodeToString(WSServerResponse(GameStatus.WIN))
                            if (gameStatus.receiver == this) {
                                send(Frame.Text(winStatus))
                            } else {
                                send(Frame.Text(loseStatus))
                            }
                        }
                        GameStatus.AWAIT -> {
                            val status = Json.encodeToString(WSServerResponse(GameStatus.AWAIT))
                            send(Frame.Text(status))
                        }
                        GameStatus.READY -> {
                            val status = Json.encodeToString(WSServerResponse(GameStatus.READY))
                            send(Frame.Text(status))
                            room.startGame(this)
                        }
                        GameStatus.SHUTDOWN -> {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Room was closed"))
                        }
                        GameStatus.GOT_NEW_EXAMPLE -> {
                            if (gameStatus.receiver != this){
                                val statusString = Json.encodeToString(WSServerResponse(GameStatus.GOT_NEW_EXAMPLE))
                                send(Frame.Text(statusString))
                            }
                        }
                        GameStatus.FALSE -> {
                            if (gameStatus.receiver == this) {
                                val statusString = Json.encodeToString(WSServerResponse(GameStatus.FALSE))
                                send(Frame.Text(statusString))
                            }
                        }
                        GameStatus.FINISH -> {
                            if (gameStatus.receiver == this) {
                                val statusString = Json.encodeToString(WSServerResponse(GameStatus.FINISH))
                                send(Frame.Text(statusString))
                            }
                        }
                        else ->{}
                    }
                }
            }
        }
    }
}
