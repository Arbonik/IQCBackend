package com.mpmep

import com.mpmep.plugins.core.Example
import com.mpmep.plugins.core.Game
import com.mpmep.plugins.core.Operate

//fun main() {
//    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
//        .start(wait = true)
//}
//
//fun Application.module() {
//    configureSockets()
//    configureSerialization()
//    configureRouting()
//}


fun main() {
    Game().examples.forEach {
        println(it)
    }
}



