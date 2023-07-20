package com.mpmep

import com.mpmep.classes.GameStatus
import com.mpmep.classes.WSServerResponse
import com.mpmep.plugins.core.ExampleState
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketSession.respond(data:Any, score:Int? = null) {
    if (data is GameStatus){
        val message = WSServerResponse(gameStatus = data, score = score)
        send(Frame.Text(Json.encodeToString(message)))
    }
    if (data is ExampleState.Example){
        val message = WSServerResponse(example = data, score = score)
        val status = Json.encodeToString(message)
        send(Frame.Text(status))
    }
}