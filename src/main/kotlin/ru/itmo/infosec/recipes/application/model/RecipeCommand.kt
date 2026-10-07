package ru.itmo.infosec.recipes.application.model

data class RecipeCommand(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: String,
    val cookMinutes: Int,
    val servings: Int,
)
