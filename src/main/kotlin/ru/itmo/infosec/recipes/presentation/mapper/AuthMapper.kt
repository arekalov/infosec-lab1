package ru.itmo.infosec.recipes.presentation.mapper

import ru.itmo.infosec.recipes.application.model.AccessToken
import ru.itmo.infosec.recipes.domain.model.UserAccount
import ru.itmo.infosec.recipes.presentation.dto.TokenResponse
import ru.itmo.infosec.recipes.presentation.dto.UserResponse

fun UserAccount.toResponse(): UserResponse = UserResponse(
    id = requireNotNull(id) { "User must be persisted before it is returned" },
    username = username,
    roles = roles.toSet(),
    createdAt = createdAt,
)

fun AccessToken.toResponse(): TokenResponse = TokenResponse(
    accessToken = value,
    expiresIn = expiresInSeconds,
)
