package com.mpmep.plugins

import org.jetbrains.exposed.sql.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val database = Database.connect(
        "jdbc:sqlite:identifier.sqlite",
        "org.sqlite.JDBC",
    )
    transaction(database) {
        SchemaUtils.create(Statistics)
    }
    routing {
        get("/statistics") {
            val data = StatisticsService.readAll()
            call.respond(data)
        }
    }
}
