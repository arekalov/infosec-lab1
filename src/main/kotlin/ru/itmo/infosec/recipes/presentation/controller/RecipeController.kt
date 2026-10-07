package ru.itmo.infosec.recipes.presentation.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.itmo.infosec.recipes.application.service.RecipeService
import ru.itmo.infosec.recipes.domain.model.PageQuery
import ru.itmo.infosec.recipes.presentation.dto.PageResponse
import ru.itmo.infosec.recipes.presentation.dto.RecipeRequest
import ru.itmo.infosec.recipes.presentation.dto.RecipeResponse
import ru.itmo.infosec.recipes.presentation.mapper.toCommand
import ru.itmo.infosec.recipes.presentation.mapper.toResponse
import java.net.URI

@RestController
class RecipeController(private val recipeService: RecipeService) {

    @GetMapping("/api/data")
    fun data(
        @AuthenticationPrincipal username: String,
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE") size: Int,
    ): PageResponse<RecipeResponse> {
        val pageQuery = PageQuery(page = page.coerceAtLeast(0), size = size.coerceIn(1, MAX_PAGE_SIZE))
        return recipeService.list(username, q, pageQuery).toResponse()
    }

    @PostMapping("/api/recipes")
    fun create(
        @AuthenticationPrincipal username: String,
        @Valid @RequestBody request: RecipeRequest,
    ): ResponseEntity<RecipeResponse> {
        val created = recipeService.create(username, request.toCommand()).toResponse()
        return ResponseEntity.created(URI.create("/api/recipes/${created.id}")).body(created)
    }

    @GetMapping("/api/recipes/{id}")
    fun get(
        @AuthenticationPrincipal username: String,
        @PathVariable id: Long,
    ): RecipeResponse = recipeService.get(username, id).toResponse()

    @PutMapping("/api/recipes/{id}")
    fun update(
        @AuthenticationPrincipal username: String,
        @PathVariable id: Long,
        @Valid @RequestBody request: RecipeRequest,
    ): RecipeResponse = recipeService.update(username, id, request.toCommand()).toResponse()

    @DeleteMapping("/api/recipes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal username: String,
        @PathVariable id: Long,
    ) = recipeService.delete(username, id)

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}
