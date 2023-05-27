package com.mpmep

import com.mpmep.classes.GameStatus
import com.mpmep.classes.WSServerResponse
import com.mpmep.plugins.core.ExampleState
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketSession.respond(data:Any, score:Int? = null) {
    if (data is GameStatus){
        val message = if (score != null) {
            WSServerResponse(gameStatus = data, score = score)
        } else {
            WSServerResponse(gameStatus = data)
        }
        send(Frame.Text(Json.encodeToString(message)))
    }
    if (data is ExampleState.Example){
        val message = if (score != null) {
            WSServerResponse(example = data, score = score)
        } else {
            WSServerResponse(example = data)
        }
        val status = Json.encodeToString(message)
        send(Frame.Text(status))
    }
}