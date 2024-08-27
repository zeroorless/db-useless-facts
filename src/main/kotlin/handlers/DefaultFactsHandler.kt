package gp.example.handlers

import gp.example.handlers.FactsHandler.HandlerError
import gp.example.repositories.FactsRepository
import gp.example.repositories.FactsRepository.RepositoryError
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
        try {
            val rawFact = factsRepository.getFact()
            val shortenedUrl = urlShortener.getShortenedUrl(rawFact)
            val fact = FactsHandler.Fact(rawFact.text, shortenedUrl)
            factsCache.putIfAbsent(shortenedUrl, fact)
            return fact
        } catch (e: Exception) {
            throw wrapError(e)
        }
    }

    override suspend fun getFact(factId: String): FactsHandler.Fact? {
        return factsCache[factId]?.also { statisticsRepository.update(factId) }
    }

    private fun wrapError(e: Exception): HandlerError {
        return when (e) {
            is RepositoryError.OverloadedError -> HandlerError.OverloadedError(e.retryAfter)
            is RepositoryError.DataFormatError -> HandlerError.InternalError(e)
            else -> HandlerError.DownstreamError(e)
        }
    }
}