package gp.example

import gp.example.FactsRepository.RawFact
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class DefaultUrlShortener(private val encoder: Encoder) : UrlShortener {
    // TODO max capacity / eviction policy ?
    private val originalToShortened = ConcurrentHashMap<String, String>()
    private val shortenedToOriginal = ConcurrentHashMap<String, String>()
    val lastId = AtomicLong(0)

    override fun getShortenedUrl(rawFact: RawFact): String {
        val originalUrl = rawFact.permalink
        originalToShortened.computeIfAbsent(originalUrl) {
            val shortened = encoder.encode(lastId.andIncrement)
            shortenedToOriginal[shortened] = originalUrl
            shortened
        }
        return originalToShortened[originalUrl]!!
    }

    override fun getOriginalUrl(shortenedUrl: String): String? {
        return shortenedToOriginal[shortenedUrl]
    }
}