package ru.itmo.infosec.recipes.application.port

import ru.itmo.infosec.recipes.application.model.AccessToken
import ru.itmo.infosec.recipes.domain.model.UserAccount

interface TokenProvider {

    fun issue(user: UserAccount): AccessToken
}
