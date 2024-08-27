package gp.example.handlers

import kotlinx.serialization.Serializable

interface FactsHandler {
    @Serializable
    data class Fact(val originalFact: String, val shortenedUrl: String)

    suspend fun putFact(): Fact
    suspend fun getFact(factId: String): Fact?
}