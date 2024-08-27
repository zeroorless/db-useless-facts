package gp.example.authentication

interface AuthenticationService {
    fun authenticate(token: String): String?
}