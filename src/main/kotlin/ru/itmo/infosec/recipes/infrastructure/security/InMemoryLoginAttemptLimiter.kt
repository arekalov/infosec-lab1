package ru.itmo.infosec.recipes.infrastructure.security

import org.springframework.stereotype.Component
import ru.itmo.infosec.recipes.application.port.LoginAttemptLimiter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryLoginAttemptLimiter : LoginAttemptLimiter {

    private data class Attempts(val count: Int, val windowStart: Instant)

    private val attempts = ConcurrentHashMap<String, Attempts>()

    override fun isAllowed(key: String): Boolean {
        val record = attempts[key] ?: return true
        if (isExpired(record)) {
            attempts.remove(key)
            return true
        }
        return record.count < MAX_ATTEMPTS
    }

    override fun recordFailure(key: String) {
        pruneIfCrowded()
        attempts.compute(key) { _, current ->
            if (current == null || isExpired(current)) {
                Attempts(count = 1, windowStart = Instant.now())
            } else {
                current.copy(count = current.count + 1)
            }
        }
    }

    override fun reset(key: String) {
        attempts.remove(key)
    }

    private fun isExpired(record: Attempts): Boolean =
        Duration.between(record.windowStart, Instant.now()) > WINDOW

    private fun pruneIfCrowded() {
        if (attempts.size < PRUNE_THRESHOLD) return
        attempts.entries.removeIf { isExpired(it.value) }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val PRUNE_THRESHOLD = 10_000
        val WINDOW: Duration = Duration.ofMinutes(5)
    }
}
