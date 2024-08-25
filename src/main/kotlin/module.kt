package gp.example

import org.koin.dsl.bind
import org.koin.dsl.module

val apiModule = module {
    //FIXME Add Config for constants
    val url = "https://uselessfacts.jsph.pl/api/v2/facts/random?language=en"

    single { HttpFactsRepository(url) } bind FactsRepository::class
    single { DefaultUrlShortener() } bind UrlShortener::class
    single { DefaultStatisticsRepository() } bind StatisticsRepository::class
    single { DefaultFactsHandler(get(), get(), get()) } bind FactsHandler::class
    single { DefaultAdminHandler(get()) } bind AdminHandler::class
}