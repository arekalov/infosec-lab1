package ru.itmo.infosec.recipes.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.itmo.infosec.recipes.application.model.AccessToken
import ru.itmo.infosec.recipes.application.port.LoginAttemptLimiter
import ru.itmo.infosec.recipes.application.port.PasswordHasher
import ru.itmo.infosec.recipes.application.port.TokenProvider
import ru.itmo.infosec.recipes.domain.exception.InvalidCredentialsException
import ru.itmo.infosec.recipes.domain.exception.TooManyLoginAttemptsException
import ru.itmo.infosec.recipes.domain.exception.UsernameAlreadyTakenException
import ru.itmo.infosec.recipes.domain.model.UserAccount
import ru.itmo.infosec.recipes.domain.repository.UserAccountRepository
import java.util.UUID

@Service
class AuthService(
    private val users: UserAccountRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
    private val loginAttemptLimiter: LoginAttemptLimiter,
) {

    private val dummyHash: String by lazy { passwordHasher.hash(UUID.randomUUID().toString()) }

    @Transactional
    fun register(username: String, password: String): UserAccount {
        val normalized = username.lowercase()
        if (users.existsByUsername(normalized)) throw UsernameAlreadyTakenException()

        return users.save(UserAccount(username = normalized, passwordHash = passwordHasher.hash(password)))
    }

    fun login(username: String, password: String): AccessToken {
        val normalized = username.lowercase()
        if (!loginAttemptLimiter.isAllowed(normalized)) throw TooManyLoginAttemptsException()

        val user = users.findByUsername(normalized)
        // чтобы время ответа не выдавало, существует ли логин
        val passwordMatches = passwordHasher.matches(password, user?.passwordHash ?: dummyHash)
        if (user == null || !passwordMatches) {
            loginAttemptLimiter.recordFailure(normalized)
            throw InvalidCredentialsException()
        }

        loginAttemptLimiter.reset(normalized)
        return tokenProvider.issue(user)
    }
}
