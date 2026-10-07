package ru.itmo.infosec.recipes.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.itmo.infosec.recipes.domain.model.Recipe

interface SpringDataRecipeRepository : JpaRepository<Recipe, Long> {

    fun findByOwnerUsername(username: String, pageable: Pageable): Page<Recipe>

    fun findByIdAndOwnerUsername(id: Long, username: String): Recipe?

    fun deleteByIdAndOwnerUsername(id: Long, username: String): Long

    @Query(
        """
        SELECT r FROM Recipe r
        WHERE r.owner.username = :username
          AND LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%'))
        """,
    )
    fun search(
        @Param("username") username: String,
        @Param("query") query: String,
        pageable: Pageable,
    ): Page<Recipe>
}
