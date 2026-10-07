package ru.itmo.infosec.recipes.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import ru.itmo.infosec.recipes.domain.model.UserAccount

interface SpringDataUserAccountRepository : JpaRepository<UserAccount, Long> {

    fun findByUsername(username: String): UserAccount?

    fun existsByUsername(username: String): Boolean
}
