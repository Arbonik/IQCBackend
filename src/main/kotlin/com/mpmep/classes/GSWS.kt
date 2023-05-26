package com.mpmep.classes

import io.ktor.websocket.*

data class GSWS(val gameStatus: GameStatus, var receiver: DefaultWebSocketSession? = null)