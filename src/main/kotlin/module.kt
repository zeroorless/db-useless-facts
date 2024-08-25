package gp.example

import org.koin.dsl.bind
import org.koin.dsl.module

val apiModule = module {
    val url = "https://uselessfacts.jsph.pl/api/v2/facts/random?language=en"
    single { HttpFactsRepository(url) } bind FactsRepository::class
}