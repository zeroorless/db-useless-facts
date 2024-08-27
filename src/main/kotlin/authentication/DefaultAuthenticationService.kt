package gp.example.authentication

data class AuthConfig(val adminToken: String)

class DefaultAuthenticationService(private val authConfig: AuthConfig) : AuthenticationService {

    override fun authenticate(token: String): String? {
        return if (authConfig.adminToken == token) "admin" else null
    }
}