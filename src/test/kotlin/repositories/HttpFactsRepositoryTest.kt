package gp.example.repositories

import TestData.testFact
import gp.example.repositories.FactsRepository.RepositoryError
import gp.example.repositories.FactsRepository.RepositoryError.ProtocolError
import io.ktor.client.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.serialization.kotlinx.json.json
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class HttpFactsRepositoryTest {

    companion object {
        val config = HttpFactsConfig(url = "http://example.com")
    }

    @BeforeEach
    fun setUp() {}

    @Test
    fun `getFact returns parsed response on success`() = runTest {
        val factJsonString = Json.encodeToString(testFact)
        val repo = getRepository { request ->
            respond(factJsonString, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val actualRawFact = repo.getFact()
        assertEquals(testFact, actualRawFact)
    }

    @Test
    fun `getFact throws CommunicationError on HttpRequestTimeoutException`() = runTest {
        val repo = getRepository { request -> throw HttpRequestTimeoutException("example.com", 500L) }
        val exception = assertFailsWith<RepositoryError.CommunicationError> { repo.getFact() }
        assertIs<HttpRequestTimeoutException>(exception.cause)
    }

    @Test
    fun `getFact throws OverloadedError on TooManyRequests response`() = runTest {
        val retryAfter = 500
        val repo = getRepository { request ->
            respond("Too many requests", HttpStatusCode.TooManyRequests, headersOf(HttpHeaders.RetryAfter, retryAfter.toString()))
        }
        val exception = assertFailsWith<RepositoryError.OverloadedError> { repo.getFact() }
        assertEquals(retryAfter, exception.retryAfter)
    }

    @Test
    fun `getFact throws HttpError on non-success and non-TooManyRequests status`() = runTest {
        val errorMessage = "Server Error"
        val errorStatus = HttpStatusCode.InternalServerError
        val repo = getRepository { request -> respond(errorMessage, errorStatus) }
        val exception = assertFailsWith<ProtocolError.HttpError> { repo.getFact() }
        assertEquals(errorStatus.value, exception.status)
        assertEquals(errorMessage, exception.body)
    }

    @Test
    fun `getFact throws DataFormatError on response parsing failure`() = runTest {
        val invalidJson = Json.encodeToString(testFact).replace("permalink", "permanent_link")
        val repo = getRepository { request -> respond(invalidJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        assertFailsWith<RepositoryError.DataFormatError> { repo.getFact() }
    }

    private fun getRepository(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpFactsRepository {
        val mockEngine = MockEngine{ request -> handler(request) }
        val client = HttpClient(mockEngine) { install(ContentNegotiation) { json() } }
        return HttpFactsRepository(config, client)
    }
}