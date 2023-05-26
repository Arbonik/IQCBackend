package com.mpmep

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketSession.respond(data:Any) {
    val status = Json.encodeToString(data)
    send(Frame.Text(status))
}