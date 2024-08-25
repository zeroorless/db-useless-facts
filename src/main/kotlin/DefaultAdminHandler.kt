package gp.example

import gp.example.AdminHandler.Statistics

class DefaultAdminHandler(private val statisticsRepository: StatisticsRepository) : AdminHandler {

    override fun getStatistics(): List<Statistics> {
        return statisticsRepository.getStatistics()
            .map { (_, v) -> Statistics(v.url, v.count) }
    }
}