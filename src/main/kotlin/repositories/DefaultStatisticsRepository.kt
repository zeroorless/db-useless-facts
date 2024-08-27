package gp.example.repositories

import java.util.concurrent.ConcurrentHashMap

class DefaultStatisticsRepository : StatisticsRepository {

    val statistics = ConcurrentHashMap<String, StatisticsRepository.UrlStatistics>()

    override fun update(url: String) {
        statistics.merge(url, StatisticsRepository.UrlStatistics(url, 1)) { oldStatistics, newStatistics ->
            StatisticsRepository.UrlStatistics(url, oldStatistics.count + newStatistics.count)
        }
    }

    override fun getStatistics(): Map<String, StatisticsRepository.UrlStatistics> {
        return statistics.toMap()
    }
}