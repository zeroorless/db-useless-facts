package gp.example

interface AuthenticationService {
    fun authenticate(token: String): String?
}