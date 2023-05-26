package com.mpmep.plugins

import com.mpmep.classes.GameStatus
import com.mpmep.classes.Room
import com.mpmep.plugins.core.Game
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    val rooms = mutableListOf<Room>()
    routing {
        route("/rooms") {
            get {
                val filteredRooms = rooms.filter {
                    it.players.size <= 1
                }
                call.respond(filteredRooms)
            }
            post {
                val room = Room()
                rooms.add(room)
                call.respond("${room.id}")
            }
            webSocket("/{id?}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Room id was excepted")
                val room = rooms.find {
                    it.id == id
                } ?: throw IllegalArgumentException("Room was not found")
                room.addPlayer(this)
                room.roomState.collectLatest { gameStatus ->
                    when (gameStatus){
                        GameStatus.AWAIT -> {
                            val status = Json.encodeToString(GameStatus.AWAIT)
                            send(Frame.Text(status))
                        }
                        GameStatus.READY -> {
                            val status = Json.encodeToString(GameStatus.READY)
                            send(Frame.Text(status))
                            room.startGame(this)
                        }
                        GameStatus.SHUTDOWN -> {
                            room.players.forEach {
                                it.close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                            }
                        }
                        else ->{}
                    }
                }
            }
        }
    }
}
