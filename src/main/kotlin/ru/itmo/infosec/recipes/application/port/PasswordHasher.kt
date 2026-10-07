package ru.itmo.infosec.recipes.application.port

interface PasswordHasher {

    fun hash(rawPassword: String): String

    fun matches(rawPassword: String, hash: String): Boolean
}
