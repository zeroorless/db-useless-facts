package gp.example.repositories

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

data class HttpFactsConfig(val url: String, val timeout: Long)

class HttpFactsRepository(val config: HttpFactsConfig) : FactsRepository {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun getFact(): FactsRepository.RawFact {
        val response = getResponse()
        return handleResponse(response)
    }

    private suspend fun getResponse(): HttpResponse {
        try {
            return withTimeout(config.timeout) { httpClient.get(config.url) }
        } catch (_: TimeoutCancellationException) {
            throw FactsRepository.RepositoryError.NetworkError.TimeoutError
        } catch (t: Throwable) {
            throw FactsRepository.RepositoryError.NetworkError.ConnectionError(t)
        }
    }

    private suspend fun handleResponse(response: HttpResponse): FactsRepository.RawFact {
        when {
            response.status.isSuccess() -> return parseResponse(response)
            else -> throw FactsRepository.RepositoryError.HttpError(response.status.value, response.bodyAsText())
        }
    }

    private suspend fun parseResponse(response: HttpResponse): FactsRepository.RawFact {
        try {
            return response.body()
        } catch (t: Throwable) {
            throw FactsRepository.RepositoryError.ParsingError("Couldn't parse response: $response", t)
        }
    }
}