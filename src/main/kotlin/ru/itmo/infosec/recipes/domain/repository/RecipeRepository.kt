package ru.itmo.infosec.recipes.domain.repository

import ru.itmo.infosec.recipes.domain.model.PageQuery
import ru.itmo.infosec.recipes.domain.model.PageResult
import ru.itmo.infosec.recipes.domain.model.Recipe

interface RecipeRepository {

    fun findAllByOwner(username: String, page: PageQuery): PageResult<Recipe>

    fun searchByOwner(username: String, query: String, page: PageQuery): PageResult<Recipe>

    fun findByIdAndOwner(id: Long, username: String): Recipe?

    fun save(recipe: Recipe): Recipe

    fun deleteByIdAndOwner(id: Long, username: String): Boolean
}
