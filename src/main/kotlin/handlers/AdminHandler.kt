package gp.example.handlers

import kotlinx.serialization.Serializable

interface AdminHandler {
    @Serializable
    data class Statistics(val shortenedUrl: String, val accessCount: Int)

    fun getStatistics(): List<Statistics>
}