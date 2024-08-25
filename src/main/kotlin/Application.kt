package gp.example

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy.Builtins.SnakeCase
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("gp")

fun main(args: Array<String>) = EngineMain.main(args)

@OptIn(ExperimentalSerializationApi::class)
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

    install(ContentNegotiation) {
        json(Json { namingStrategy = SnakeCase })
    }

    val factsHandler by inject<FactsHandler>()

    routing {
        route("/facts") {
            post {
                val newFact = factsHandler.putFact()
                call.respond(HttpStatusCode.OK, newFact)
            }
            get("/{shortenedUrl}") {
                val shortenedUrl = call.parameters["shortenedUrl"]
                    ?: return@get call.respondText("Missing fact id.", status =  HttpStatusCode.BadRequest)

                factsHandler.getFact(shortenedUrl)?.let { call.respond(it) }
                    ?: return@get call.respond(HttpStatusCode.NotFound)
            }

        }
        route("/admin") {
            get("/statistics") {
                call.respondText("You've received statistics")
            }
        }
    }
}