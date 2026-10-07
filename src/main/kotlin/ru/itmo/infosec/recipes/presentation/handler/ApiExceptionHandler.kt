package ru.itmo.infosec.recipes.presentation.handler

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ru.itmo.infosec.recipes.domain.exception.InvalidCredentialsException
import ru.itmo.infosec.recipes.domain.exception.RecipeNotFoundException
import ru.itmo.infosec.recipes.domain.exception.TooManyLoginAttemptsException
import ru.itmo.infosec.recipes.domain.exception.UsernameAlreadyTakenException

@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(RecipeNotFoundException::class)
    fun handleNotFound(ex: RecipeNotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, "Recipe not found")

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ProblemDetail =
        problem(HttpStatus.UNAUTHORIZED, "Invalid username or password")

    @ExceptionHandler(TooManyLoginAttemptsException::class)
    fun handleTooManyAttempts(ex: TooManyLoginAttemptsException): ProblemDetail =
        problem(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts, try again later")

    @ExceptionHandler(UsernameAlreadyTakenException::class)
    fun handleUsernameTaken(ex: UsernameAlreadyTakenException): ProblemDetail =
        problem(HttpStatus.CONFLICT, "Username already taken")

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException): ProblemDetail =
        problem(HttpStatus.UNAUTHORIZED, "Authentication required")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ProblemDetail =
        problem(HttpStatus.FORBIDDEN, "Access denied")

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ProblemDetail {
        log.error("Unhandled exception", ex)
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error")
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed").apply {
            setProperty(
                "errors",
                ex.bindingResult.fieldErrors
                    .groupBy { it.field }
                    .mapValues { (_, errors) -> errors.mapNotNull { it.defaultMessage }.sorted() },
            )
        }
        return ResponseEntity.badRequest().body(problem)
    }

    private fun problem(status: HttpStatus, detail: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply { title = status.reasonPhrase }
}
