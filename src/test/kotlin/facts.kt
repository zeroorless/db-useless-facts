import gp.example.repositories.FactsRepository

object TestData {
    val testFact = FactsRepository.RawFact(
        id = "0",
        text = "Fact",
        source = "Example API",
        source_url = "https://example.com",
        language = "en",
        permalink = "https://example.com/permalink",
    )
}