package gp.example.repositories

import kotlinx.serialization.Serializable

interface FactsRepository {

    @Serializable
    data class RawFact(
        val id: String,
        val text: String,
        val source: String,
        val source_url: String,
        val language: String,
        val permalink: String
    )

    interface Retryable

    sealed class RepositoryError(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause) {
        data class CommunicationError(override val message: String? = null, override val cause: Throwable? = null) : RepositoryError(message, cause)
        sealed class ProtocolError() : RepositoryError() {
            data class HttpError(val status: Int, val body: String?) : ProtocolError()
        }
        data class OverloadedError(val retryAfter: Int? = null) : RepositoryError("Too many requests, please retry later.")
        data class DataFormatError(override val message: String? = null, override val cause: Throwable? = null) : RepositoryError(message, cause)
    }

    suspend fun getFact(): RawFact
}