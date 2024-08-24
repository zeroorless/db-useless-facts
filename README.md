# db-useless-facts

It is a simple caching service for [Useless Facts API](https://uselessfacts.jsph.pl).
It proxies fact requests to `Useless Facts API`,
caches facts and provides access to them via shortened urls, along with a some usage statistics for those cached facts.

# Service Endpoints
For more information see [Open Api Documentation](src/main/resources/openapi/documentation.yaml)
## Facts
- `POST /facts`
  - Fetches a random fact from the Useless Facts API, and stores it along with a newly generated shortened URL
  - The response contains the new fact content along with the fact's shortened `URL`:
```json
{
  "original_fact": "string",
  "shortened_url": "string"
}
```
- `GET /facts/<shortened_url>`
  - Returns a fact from the cache and increments access count.

## Statistics
- `GET /amdin/statistics`
  - Provides access statistics for all shortened URLs in the cache.

The endpoint uses `bearer token` authentication. 

**N.B.** The process of logging users in is assumed to be implemented elsewhere
and left out of scope.

Default admin token is `bearer-123` and can be overridden via an environmental variable, see #running section.

## Running Service
1. Clone the repository
2. Build `fat.jar` by running `gradle buildFatJar`
3. Run the service `java -jar build/libs/fat.jar`
   4. The admin bearer token can be overridden via environmental variable `token`, e.g.
   `token=<bearer-token> java -jar build/libs/fat.jar`