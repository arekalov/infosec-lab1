package ru.itmo.infosec.recipes.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // токен передаётся в заголовке, cookie-сессий нет, поэтому CSRF не нужен
            csrf { disable() }

            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }

            authorizeHttpRequests {
                authorize("/auth/register", permitAll)
                authorize("/auth/login", permitAll)
                authorize(anyRequest, authenticated)
            }

            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }

            headers {
                contentTypeOptions { }
                frameOptions { deny = true }
                contentSecurityPolicy { policyDirectives = "default-src 'none'; frame-ancestors 'none'" }
                referrerPolicy { policy = ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER }
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthFilter)

            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
            }
        }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(BCRYPT_STRENGTH)

    private companion object {
        const val BCRYPT_STRENGTH = 12
    }
}
