package com.mpmep.plugins

import com.mpmep.classes.GameStatus
import com.mpmep.classes.Room
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
                    it.players.size <= 1
                }
                call.respond(filteredRooms)
            }
            post {
                val room = Room()
                rooms.add(room)
                call.respond("success")
            }
            webSocket("/{id?}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Room id was excepted")
                val room = rooms.find {
                    it.id == id
                } ?: throw IllegalArgumentException("Room was not found")
                room.addPlayer(this)
                val game = room.game
                for (frame in incoming){
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        game.currentExample.collect { example ->
                            val challengeStatus = game.checkAnswer(example, text.toInt())
                            val status: GameStatus = if (!challengeStatus) {
                                GameStatus.BAD
                            } else {
                                GameStatus.TRUE
                            }
                            val statusString = Json.encodeToString(status)
                            outgoing.send(Frame.Text(statusString))
                        }
                    }
                }
            }
        }
    }
}
