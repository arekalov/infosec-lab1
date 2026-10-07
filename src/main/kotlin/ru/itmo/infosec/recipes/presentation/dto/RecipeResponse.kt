package ru.itmo.infosec.recipes.presentation.dto

import java.time.Instant

data class RecipeResponse(
    val id: Long,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: String,
    val cookMinutes: Int,
    val servings: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
