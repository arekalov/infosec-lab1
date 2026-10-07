package ru.itmo.infosec.recipes.infrastructure.persistence

import org.springframework.stereotype.Repository
import ru.itmo.infosec.recipes.domain.model.UserAccount
import ru.itmo.infosec.recipes.domain.repository.UserAccountRepository

@Repository
class JpaUserAccountRepository(
    private val springData: SpringDataUserAccountRepository,
) : UserAccountRepository {

    override fun findByUsername(username: String): UserAccount? = springData.findByUsername(username)

    override fun existsByUsername(username: String): Boolean = springData.existsByUsername(username)

    override fun save(user: UserAccount): UserAccount = springData.save(user)
}
