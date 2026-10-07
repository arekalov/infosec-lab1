package ru.itmo.infosec.recipes.presentation.dto

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)
