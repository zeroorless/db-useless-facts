package gp.example.handlers

import kotlinx.serialization.Serializable

interface FactsHandler {
    @Serializable
    data class Fact(val originalFact: String, val shortenedUrl: String)

    sealed class HandlerError(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause) {
        data class InternalError(override val cause: Throwable?) : HandlerError(cause = cause)
        data class DownstreamError(override val cause: Throwable?) : HandlerError(cause = cause)
        data class OverloadedError(val retryAfter: Int?) : HandlerError()
    }

    suspend fun putFact(): Fact
    suspend fun getFact(factId: String): Fact?
}