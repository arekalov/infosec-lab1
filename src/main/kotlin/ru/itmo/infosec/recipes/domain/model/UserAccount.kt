package ru.itmo.infosec.recipes.domain.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserAccount(
    @Column(nullable = false, unique = true, length = USERNAME_MAX)
    var username: String,

    @Column(name = "password_hash", nullable = false, length = BCRYPT_HASH_LENGTH)
    var passwordHash: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role", nullable = false, length = ROLE_MAX)
    var roles: MutableSet<String> = mutableSetOf(ROLE_USER),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
) {
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    companion object {
        const val USERNAME_MIN = 3
        const val USERNAME_MAX = 32
        const val PASSWORD_MIN = 12
        const val PASSWORD_MAX = 128
        const val ROLE_MAX = 32
        const val BCRYPT_HASH_LENGTH = 60
        const val ROLE_USER = "USER"

        const val USERNAME_PATTERN = "^[a-zA-Z0-9_.-]+$"
    }
}
