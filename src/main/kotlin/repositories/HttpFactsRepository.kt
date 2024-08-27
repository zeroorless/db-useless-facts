package gp.example.repositories


import gp.example.repositories.FactsRepository.RepositoryError
import gp.example.repositories.FactsRepository.RepositoryError.ProtocolError
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

data class HttpFactsConfig(val url: String, val timeout: Long)

class HttpFactsRepository(val config: HttpFactsConfig) : FactsRepository {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeout
        }
        install(HttpRequestRetry) {
            //we can retry 429 here, since exponentialDelay() below respects retry-after header
            retryIf(2) {_ , response -> response.status.value == 429 }
            retryOnServerErrors(2)
            retryOnExceptionIf(2) {_, e ->
                when (e) {
                    is HttpRequestTimeoutException, is ConnectTimeoutException -> true
                    else -> false
                }
            }
            exponentialDelay()
        }
    }

    override suspend fun getFact(): FactsRepository.RawFact {
        val response = getResponse()
        return handleResponse(response)
    }

    private suspend fun getResponse(): HttpResponse {
        try {
            return httpClient.get(config.url)
        } catch (t: Throwable) {
            throw RepositoryError.CommunicationError(cause = t)
        }
    }

    private suspend fun handleResponse(response: HttpResponse): FactsRepository.RawFact {
        when {
            response.status.isSuccess() -> return parseResponse(response)
            response.status == HttpStatusCode.TooManyRequests ->
                throw RepositoryError.OverloadedError(response.headers.get(HttpHeaders.RetryAfter)?.toInt())
            else -> throw ProtocolError.HttpError(response.status.value, response.bodyAsText())
        }
    }

    private suspend fun parseResponse(response: HttpResponse): FactsRepository.RawFact {
        try {
            return response.body()
        } catch (t: Throwable) {
            throw RepositoryError.DataFormatError("Couldn't parse response: $response", t)
        }
    }
}