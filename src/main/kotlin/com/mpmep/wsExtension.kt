package com.mpmep

import com.mpmep.classes.GameStatus
import com.mpmep.classes.WSServerResponse
import com.mpmep.plugins.core.ExampleState
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketSession.respond(data:Any) {
    if (data is GameStatus){
        val message = WSServerResponse(gameStatus = data)
        send(Frame.Text(Json.encodeToString(message)))
    }
    if (data is ExampleState.Example){
        val status = Json.encodeToString(WSServerResponse(example = data))
        send(Frame.Text(status))
    }
}