package gp.example

import gp.example.authentication.AuthenticationService
import gp.example.handlers.AdminHandler
import gp.example.handlers.FactsHandler
import gp.example.koin.initModule
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.swaggerUI
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

    val token = environment.config.property("ktor.auth.token").getString()
    logger.info("Authorisation token: $token")

    install(Koin) {
        slf4jLogger()
        modules(initModule(this@module))
    }

    val factsHandler by inject<FactsHandler>()
    val adminHandler by inject<AdminHandler>()
    val authenticationService by inject<AuthenticationService>()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(call.toString(), cause)
            when (cause) {
                is FactsHandler.HandlerError.OverloadedError ->  {
                    cause.retryAfter?.let { call.response.headers.append(HttpHeaders.RetryAfter, it.toString()) }
                    call.respond(HttpStatusCode.TooManyRequests)
                }
                is FactsHandler.HandlerError.DownstreamError -> call.respond(HttpStatusCode.ServiceUnavailable)
                else -> call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    install(ContentNegotiation) {
        json(Json { namingStrategy = SnakeCase })
    }

    install(Authentication) {
        bearer("auth-bearer") {
            realm = "Access to /admin endpoints"
            authenticate { tokenCredential ->
                authenticationService.authenticate(tokenCredential.token)?.let {
                    UserIdPrincipal(it)
                }
            }
        }
    }

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
        authenticate("auth-bearer") {
            route("/admin") {
                get("/statistics") {
                    if (call.principal<UserIdPrincipal>()?.name == "admin") {
                        call.respond(adminHandler.getStatistics())
                    } else {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }

        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}