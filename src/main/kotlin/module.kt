package gp.example

import io.ktor.server.application.Application
import org.koin.dsl.bind
import org.koin.dsl.module

fun initModule(application: Application) = module {
    val config = application.environment.config
    val authConfig = AuthConfig(config.property("ktor.auth.token").getString())
    val url = config.property("ktor.facts.url").getString()

    single { Base62Encoder() } bind Encoder::class
    single { DefaultAuthenticationService(authConfig) } bind AuthenticationService::class
    single { HttpFactsRepository(url) } bind FactsRepository::class
    single { DefaultUrlShortener(get()) } bind UrlShortener::class
    single { DefaultStatisticsRepository() } bind StatisticsRepository::class
    single { DefaultFactsHandler(get(), get(), get()) } bind FactsHandler::class
    single { DefaultAdminHandler(get()) } bind AdminHandler::class
}