package gp.example.repositories

interface StatisticsRepository {
    data class UrlStatistics(val url: String, val count: Int)

    fun update(url: String)
    fun getStatistics(): Map<String, UrlStatistics>
}