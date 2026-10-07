package ru.itmo.infosec.recipes.domain.repository

import ru.itmo.infosec.recipes.domain.model.UserAccount

interface UserAccountRepository {

    fun findByUsername(username: String): UserAccount?

    fun existsByUsername(username: String): Boolean

    fun save(user: UserAccount): UserAccount
}
