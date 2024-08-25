package gp.example

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import gp.example.FactsRepository.RepositoryError
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

class HttpFactsRepository(val url: String) : FactsRepository {
    companion object {
        const val timeout = 500L
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun getFact(): FactsRepository.Fact {
        val response = getResponse()
        return handleResponse(response)
    }

    private suspend fun getResponse(): HttpResponse {
        try {
            return withTimeout(timeout) { httpClient.get(url) }
        } catch (_: TimeoutCancellationException) {
            throw RepositoryError.NetworkError.TimeoutError
        } catch (t: Throwable) {
            throw RepositoryError.NetworkError.ConnectionError(t)
        }
    }

    private suspend fun handleResponse(response: HttpResponse): FactsRepository.Fact {
        when {
            response.status.isSuccess() -> return parseResponse(response)
            else -> throw RepositoryError.HttpError(response.status.value, response.bodyAsText())
        }
    }

    private suspend fun parseResponse(response: HttpResponse): FactsRepository.Fact {
        try {
            return response.body()
        } catch (t: Throwable) {
            throw RepositoryError.ParsingError("Couldn't parse response: $response", t)
        }
    }
}