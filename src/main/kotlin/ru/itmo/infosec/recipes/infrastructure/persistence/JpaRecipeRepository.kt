package ru.itmo.infosec.recipes.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import ru.itmo.infosec.recipes.domain.model.PageQuery
import ru.itmo.infosec.recipes.domain.model.PageResult
import ru.itmo.infosec.recipes.domain.model.Recipe
import ru.itmo.infosec.recipes.domain.repository.RecipeRepository

@Repository
class JpaRecipeRepository(
    private val springData: SpringDataRecipeRepository,
) : RecipeRepository {

    override fun findAllByOwner(username: String, page: PageQuery): PageResult<Recipe> =
        springData.findByOwnerUsername(username, page.toPageable()).toPageResult()

    override fun searchByOwner(username: String, query: String, page: PageQuery): PageResult<Recipe> =
        springData.search(username, query, page.toPageable()).toPageResult()

    override fun findByIdAndOwner(id: Long, username: String): Recipe? =
        springData.findByIdAndOwnerUsername(id, username)

    override fun save(recipe: Recipe): Recipe = springData.save(recipe)

    override fun deleteByIdAndOwner(id: Long, username: String): Boolean =
        springData.deleteByIdAndOwnerUsername(id, username) > 0

    private fun PageQuery.toPageable(): PageRequest =
        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

    private fun Page<Recipe>.toPageResult(): PageResult<Recipe> = PageResult(
        items = content,
        page = number,
        size = size,
        totalItems = totalElements,
        totalPages = totalPages,
    )
}
