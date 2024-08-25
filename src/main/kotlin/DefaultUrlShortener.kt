package gp.example

import gp.example.FactsRepository.RawFact
import java.util.concurrent.ConcurrentHashMap

class DefaultUrlShortener : UrlShortener {
    // FIXME max capacity / eviction policy ?
    val originalToShortened = ConcurrentHashMap<String, String>()
    val shortenedToOriginal = ConcurrentHashMap<String, String>()

    override fun getShortenedUrl(rawFact: RawFact): String {
        val originalUrl = rawFact.permalink
        originalToShortened.computeIfAbsent(originalUrl) {
            val shortened = shortenUrl(rawFact)
            shortenedToOriginal[shortened] = originalUrl
            shortened
        }
        return originalToShortened[originalUrl]!!
    }

    override fun getOriginalUrl(shortenedUrl: String): String? {
        return shortenedToOriginal[shortenedUrl]
    }

    private fun shortenUrl(rawFact: RawFact): String {
        // TODO smarter shortening based on fact.permalink ?
        return rawFact.id
    }
}