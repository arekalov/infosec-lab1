package ru.itmo.infosec.recipes.application.port

interface LoginAttemptLimiter {

    fun isAllowed(key: String): Boolean

    fun recordFailure(key: String)

    fun reset(key: String)
}
