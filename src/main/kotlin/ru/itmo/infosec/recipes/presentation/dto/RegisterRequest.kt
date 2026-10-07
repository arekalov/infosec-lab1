package ru.itmo.infosec.recipes.presentation.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ru.itmo.infosec.recipes.domain.model.UserAccount

data class RegisterRequest(
    @field:Pattern(regexp = UserAccount.USERNAME_PATTERN, message = "must contain only letters, digits, . _ -")
    @field:Size(min = UserAccount.USERNAME_MIN, max = UserAccount.USERNAME_MAX)
    val username: String,

    @field:Size(min = UserAccount.PASSWORD_MIN, max = UserAccount.PASSWORD_MAX)
    val password: String,
)
