package gp.example.utils

import gp.example.repositories.FactsRepository.RawFact

interface UrlShortener {
    fun getShortenedUrl(rawFact: RawFact): String
    fun getOriginalUrl(shortenedUrl: String): String?
}