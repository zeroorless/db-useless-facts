package gp.example.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientConfig(val timeout: Long)

class HttpClientFactory(val config: HttpClientConfig) {
    fun getClient(): HttpClient {
        return  HttpClient(CIO) {
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
    }
}