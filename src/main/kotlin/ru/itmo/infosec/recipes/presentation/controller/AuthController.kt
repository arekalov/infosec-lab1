package ru.itmo.infosec.recipes.presentation.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.itmo.infosec.recipes.application.service.AuthService
import ru.itmo.infosec.recipes.presentation.dto.LoginRequest
import ru.itmo.infosec.recipes.presentation.dto.RegisterRequest
import ru.itmo.infosec.recipes.presentation.dto.TokenResponse
import ru.itmo.infosec.recipes.presentation.dto.UserResponse
import ru.itmo.infosec.recipes.presentation.mapper.toResponse

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): UserResponse =
        authService.register(request.username, request.password).toResponse()

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse =
        authService.login(request.username, request.password).toResponse()
}
