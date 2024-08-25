package gp.example

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
        data class HttpError(val status: Int, val body: String) : RepositoryError() {}
        sealed class NetworkError(cause: Throwable? = null) : RepositoryError(cause = cause), Retryable {
            object TimeoutError: NetworkError()
            class ConnectionError(cause: Throwable): NetworkError(cause)
        }
        data class ParsingError(override val message: String? = null, override val cause: Throwable? = null) : RepositoryError(message, cause)
    }

    suspend fun getFact(): RawFact
}