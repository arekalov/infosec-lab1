package ru.itmo.infosec.recipes.presentation.dto

import java.time.Instant

data class UserResponse(
    val id: Long,
    val username: String,
    val roles: Set<String>,
    val createdAt: Instant,
)
