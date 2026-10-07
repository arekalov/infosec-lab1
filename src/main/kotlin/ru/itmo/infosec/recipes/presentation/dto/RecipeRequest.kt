package ru.itmo.infosec.recipes.presentation.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import ru.itmo.infosec.recipes.domain.model.Recipe

data class RecipeRequest(
    @field:NotBlank
    @field:Size(min = 3, max = Recipe.TITLE_MAX)
    val title: String,

    @field:NotBlank
    @field:Size(max = Recipe.DESCRIPTION_MAX)
    val description: String,

    @field:NotEmpty
    @field:Size(max = Recipe.INGREDIENTS_MAX_COUNT)
    val ingredients: List<@NotBlank @Size(max = Recipe.INGREDIENT_MAX) String>,

    @field:NotBlank
    @field:Size(max = Recipe.INSTRUCTIONS_MAX)
    val instructions: String,

    @field:Min(1)
    @field:Max(Recipe.COOK_MINUTES_MAX)
    val cookMinutes: Int,

    @field:Min(1)
    @field:Max(Recipe.SERVINGS_MAX)
    val servings: Int,
)
