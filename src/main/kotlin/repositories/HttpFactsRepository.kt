package gp.example.repositories


import gp.example.repositories.FactsRepository.RepositoryError
import gp.example.repositories.FactsRepository.RepositoryError.ProtocolError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

data class HttpFactsConfig(val url: String)

class HttpFactsRepository(val config: HttpFactsConfig, private val httpClient: HttpClient) : FactsRepository {

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