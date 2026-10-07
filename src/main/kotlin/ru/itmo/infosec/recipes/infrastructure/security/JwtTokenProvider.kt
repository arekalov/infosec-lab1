package ru.itmo.infosec.recipes.infrastructure.security

import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import ru.itmo.infosec.recipes.application.model.AccessToken
import ru.itmo.infosec.recipes.application.port.TokenProvider
import ru.itmo.infosec.recipes.domain.model.UserAccount
import java.time.Instant

@Component
class JwtTokenProvider(
    private val jwtEncoder: JwtEncoder,
    private val properties: JwtProperties,
) : TokenProvider {

    override fun issue(user: UserAccount): AccessToken {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(properties.issuer)
            .subject(user.username)
            .issuedAt(now)
            .expiresAt(now.plus(properties.ttl))
            .claim(JwtAuthFilter.CLAIM_ROLES, user.roles.toList())
            .build()

        return AccessToken(
            value = jwtEncoder.encode(JwtEncoderParameters.from(claims)).tokenValue,
            expiresInSeconds = properties.ttl.toSeconds(),
        )
    }
}
