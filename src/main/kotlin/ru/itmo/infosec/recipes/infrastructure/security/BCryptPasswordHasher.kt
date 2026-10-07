package ru.itmo.infosec.recipes.infrastructure.security

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import ru.itmo.infosec.recipes.application.port.PasswordHasher

@Component
class BCryptPasswordHasher(
    private val passwordEncoder: PasswordEncoder,
) : PasswordHasher {

    override fun hash(rawPassword: String): String =
        checkNotNull(passwordEncoder.encode(rawPassword)) { "Password encoder returned no hash" }

    override fun matches(rawPassword: String, hash: String): Boolean =
        passwordEncoder.matches(rawPassword, hash)
}
