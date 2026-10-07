package ru.itmo.infosec.recipes.presentation.mapper

import org.owasp.encoder.Encode
import ru.itmo.infosec.recipes.application.model.RecipeCommand
import ru.itmo.infosec.recipes.domain.model.PageResult
import ru.itmo.infosec.recipes.domain.model.Recipe
import ru.itmo.infosec.recipes.presentation.dto.PageResponse
import ru.itmo.infosec.recipes.presentation.dto.RecipeRequest
import ru.itmo.infosec.recipes.presentation.dto.RecipeResponse

fun RecipeRequest.toCommand(): RecipeCommand = RecipeCommand(
    title = title,
    description = description,
    ingredients = ingredients,
    instructions = instructions,
    cookMinutes = cookMinutes,
    servings = servings,
)

fun Recipe.toResponse(): RecipeResponse = RecipeResponse(
    id = requireNotNull(id) { "Recipe must be persisted before it is returned" },
    title = Encode.forHtml(title),
    description = Encode.forHtml(description),
    ingredients = ingredients.map(Encode::forHtml),
    instructions = Encode.forHtml(instructions),
    cookMinutes = cookMinutes,
    servings = servings,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun PageResult<Recipe>.toResponse(): PageResponse<RecipeResponse> = PageResponse(
    items = items.map { it.toResponse() },
    page = page,
    size = size,
    totalItems = totalItems,
    totalPages = totalPages,
)
