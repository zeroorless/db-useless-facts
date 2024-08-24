package gp.example

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    routing {
        route("/facts") {
            post {
                call.respondText("New fact request received")
            }
            get("/{shortenedUrl}") {
                call.respondText("You've requested ${call.parameters["shortenedUrl"]}")
            }

        }
        route("/admin") {
            get("/statistics") {
                call.respondText("You've received statistics")
            }
        }
    }
}