package gp.example

import java.util.concurrent.ConcurrentHashMap
import gp.example.StatisticsRepository.UrlStatistics

class DefaultStatisticsRepository : StatisticsRepository {

    val statistics = ConcurrentHashMap<String, UrlStatistics>()

    override fun update(url: String) {
        statistics.merge(url, UrlStatistics(url, 1)) { oldStatistics, newStatistics ->
            UrlStatistics(url, oldStatistics.count + newStatistics.count)
        }
    }

    override fun getStatistics(): Map<String, UrlStatistics> {
        return statistics.toMap()
    }
}