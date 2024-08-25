package gp.example

import gp.example.FactsRepository.RawFact

interface UrlShortener {
    fun getShortenedUrl(rawFact: RawFact): String
    fun getOriginalUrl(shortenedUrl: String): String?
}