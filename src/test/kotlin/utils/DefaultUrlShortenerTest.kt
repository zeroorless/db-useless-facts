package gp.example.utils

import TestData.testFact
import io.mockk.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultUrlShortenerTest {

    private val mockEncoder = mockk<Encoder>()
    private val urlShortener = DefaultUrlShortener(mockEncoder)

    @Test
    fun `getShortenedUrl should return a shortened URL for a given RawFact`() {
        val rawFact = testFact
        every { mockEncoder.encode(any()) } returns "a1b2"

        val shortenedUrl = urlShortener.getShortenedUrl(rawFact)

        assertEquals("a1b2", shortenedUrl)
        verify(exactly = 1) { mockEncoder.encode(any()) }
    }

    @Test
    fun `getShortenedUrl should return the same shortened URL for the same original URL`() {
        val rawFact = testFact
        every { mockEncoder.encode(any()) } returns "a1b2"

        val shortenedUrl1 = urlShortener.getShortenedUrl(rawFact)
        val shortenedUrl2 = urlShortener.getShortenedUrl(rawFact)

        assertEquals(shortenedUrl1, shortenedUrl2)
        verify(exactly = 1) { mockEncoder.encode(any()) }
    }

    @Test
    fun `getShortenedUrl should return unique shortened URLs for different original URLs`() {
        val rawFact1 = testFact
        val rawFact2 = testFact.copy(permalink = "http://example.com/permalink2")
        every { mockEncoder.encode(any()) } returns "a1b2" andThen "c3d4"

        val shortenedUrl1 = urlShortener.getShortenedUrl(rawFact1)
        val shortenedUrl2 = urlShortener.getShortenedUrl(rawFact2)

        assertEquals("a1b2", shortenedUrl1)
        assertEquals("c3d4", shortenedUrl2)
        verify(exactly = 2) { mockEncoder.encode(any()) }
    }

    @Test
    fun `getOriginalUrl should return the original URL for a given shortened URL`() {
        val rawFact = testFact
        every { mockEncoder.encode(any()) } returns "a1b2"
        urlShortener.getShortenedUrl(rawFact)
        val retrievedOriginalUrl = urlShortener.getOriginalUrl("a1b2")
        assertEquals(testFact.permalink, retrievedOriginalUrl)
    }

    @Test
    fun `getOriginalUrl should return null if the shortened URL does not exist`() {
        val nonExistentShortenedUrl = "z9y8"
        assertNull(urlShortener.getOriginalUrl(nonExistentShortenedUrl))
    }
}