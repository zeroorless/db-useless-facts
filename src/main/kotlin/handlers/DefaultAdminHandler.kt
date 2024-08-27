package gp.example.handlers

import gp.example.repositories.StatisticsRepository

class DefaultAdminHandler(private val statisticsRepository: StatisticsRepository) : AdminHandler {

    override fun getStatistics(): List<AdminHandler.Statistics> {
        return statisticsRepository.getStatistics()
            .map { (_, v) -> AdminHandler.Statistics(v.url, v.count) }
    }
}