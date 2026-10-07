package ru.itmo.infosec.recipes.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.itmo.infosec.recipes.application.model.RecipeCommand
import ru.itmo.infosec.recipes.domain.exception.RecipeNotFoundException
import ru.itmo.infosec.recipes.domain.model.PageQuery
import ru.itmo.infosec.recipes.domain.model.PageResult
import ru.itmo.infosec.recipes.domain.model.Recipe
import ru.itmo.infosec.recipes.domain.repository.RecipeRepository
import ru.itmo.infosec.recipes.domain.repository.UserAccountRepository

@Service
@Transactional(readOnly = true)
class RecipeService(
    private val recipes: RecipeRepository,
    private val users: UserAccountRepository,
) {

    fun list(username: String, query: String?, page: PageQuery): PageResult<Recipe> =
        if (query.isNullOrBlank()) {
            recipes.findAllByOwner(username, page)
        } else {
            recipes.searchByOwner(username, query, page)
        }

    fun get(username: String, id: Long): Recipe =
        recipes.findByIdAndOwner(id, username) ?: throw RecipeNotFoundException(id)

    @Transactional
    fun create(username: String, command: RecipeCommand): Recipe {
        val owner = users.findByUsername(username)
            ?: error("Authenticated user '$username' is missing from the database")

        return recipes.save(
            Recipe(
                title = command.title,
                description = command.description,
                ingredients = command.ingredients.toMutableList(),
                instructions = command.instructions,
                cookMinutes = command.cookMinutes,
                servings = command.servings,
                owner = owner,
            ),
        )
    }

    @Transactional
    fun update(username: String, id: Long, command: RecipeCommand): Recipe {
        val recipe = get(username, id).apply {
            title = command.title
            description = command.description
            ingredients = command.ingredients.toMutableList()
            instructions = command.instructions
            cookMinutes = command.cookMinutes
            servings = command.servings
        }
        return recipes.save(recipe)
    }

    @Transactional
    fun delete(username: String, id: Long) {
        if (!recipes.deleteByIdAndOwner(id, username)) throw RecipeNotFoundException(id)
    }
}
