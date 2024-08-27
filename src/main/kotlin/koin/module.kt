package gp.example.koin

import gp.example.utils.Base62Encoder
import gp.example.repositories.DefaultStatisticsRepository
import gp.example.utils.DefaultUrlShortener
import gp.example.utils.Encoder
import gp.example.repositories.FactsRepository
import gp.example.repositories.HttpFactsRepository
import gp.example.repositories.StatisticsRepository
import gp.example.utils.UrlShortener
import gp.example.authentication.AuthConfig
import gp.example.authentication.AuthenticationService
import gp.example.authentication.DefaultAuthenticationService
import gp.example.handlers.AdminHandler
import gp.example.handlers.DefaultAdminHandler
import gp.example.handlers.DefaultFactsHandler
import gp.example.handlers.FactsHandler
import gp.example.repositories.HttpFactsConfig
import io.ktor.server.application.Application
import org.koin.dsl.bind
import org.koin.dsl.module

fun initModule(application: Application) = module {
    val config = application.environment.config
    val authConfig = AuthConfig(config.property("ktor.auth.token").getString())
    val uselessFactsUrl = config.property("ktor.facts.url").getString()
    val uselessFactsTimeout = config.property("ktor.facts.timeout").getString().toLong()
    val uselessFactsConfig = HttpFactsConfig(uselessFactsUrl, uselessFactsTimeout)

    single { Base62Encoder() } bind Encoder::class
    single { DefaultAuthenticationService(authConfig) } bind AuthenticationService::class
    single { HttpFactsRepository(uselessFactsConfig) } bind FactsRepository::class
    single { DefaultUrlShortener(get()) } bind UrlShortener::class
    single { DefaultStatisticsRepository() } bind StatisticsRepository::class
    single { DefaultFactsHandler(get(), get(), get()) } bind FactsHandler::class
    single { DefaultAdminHandler(get()) } bind AdminHandler::class
}