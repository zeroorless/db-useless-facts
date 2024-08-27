package gp.example.handlers

import gp.example.repositories.FactsRepository
import gp.example.repositories.StatisticsRepository
import gp.example.utils.UrlShortener
import java.util.concurrent.ConcurrentHashMap

class DefaultFactsHandler(
    private val factsRepository: FactsRepository,
    private val urlShortener: UrlShortener,
    private val statisticsRepository: StatisticsRepository
) : FactsHandler {

    val factsCache = ConcurrentHashMap<String, FactsHandler.Fact>()

    override suspend fun putFact(): FactsHandler.Fact {
        val rawFact = factsRepository.getFact()
        val shortenedUrl = urlShortener.getShortenedUrl(rawFact)
        val fact = FactsHandler.Fact(rawFact.text, shortenedUrl)
        factsCache.putIfAbsent(shortenedUrl, fact)
        return fact
    }

    override suspend fun getFact(factId: String): FactsHandler.Fact? {
        return factsCache[factId]?.also { statisticsRepository.update(factId) }
    }
}