package gp.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("gp")

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(apiModule)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(call.toString(), cause)
            call.respondText(text = "Internal server error, we're working on it." , status = HttpStatusCode.InternalServerError)
        }
    }

    val factsRepository by inject<FactsRepository>()

    routing {
        route("/facts") {
            post {
                val fact = factsRepository.getFact()
                call.respondText("${fact.id} : ${fact.text}")
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