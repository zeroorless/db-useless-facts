package gp.example

import gp.example.FactsHandler.Fact
import java.util.concurrent.ConcurrentHashMap

class DefaultFactsHandler(
    private val factsRepository: FactsRepository,
    private val urlShortener: UrlShortener
) : FactsHandler {

    val factsCache = ConcurrentHashMap<String, Fact>()

    override suspend fun putFact(): Fact {
        val rawFact = factsRepository.getFact()
        val shortenedUrl = urlShortener.getShortenedUrl(rawFact)
        val fact = Fact(rawFact.text, shortenedUrl)
        factsCache.putIfAbsent(shortenedUrl, fact)
        return fact
    }

    override suspend fun getFact(factId: String): Fact? {
        // TODO update counters
        return factsCache[factId]
    }
}