package ru.itmo.infosec.recipes.domain.exception

class RecipeNotFoundException(id: Long) : RuntimeException("Recipe $id not found")
