package ru.itmo.infosec.recipes.application.model

data class AccessToken(
    val value: String,
    val expiresInSeconds: Long,
)
