package com.example.common

import com.example.auth.database.Tokens
import com.example.auth.database.Users
import com.example.models.UserDto
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> queryCatching(block: suspend Transaction.() -> T): T? {
    val result = runCatching { newSuspendedTransaction(context = Dispatchers.IO, statement = block) }
    return result.getOrNull()
}

suspend inline fun <reified T : Any> RoutingCall.respondRes(response: Response<T>) {
    when (response) {
        is Response.Error -> respond(HttpStatusCode(response.error.code, response.error.message), response.error.message)
        is Response.Success<T> -> respond(response.data)
    }
}

suspend fun resolveUser(token: String): UserDto? {
    val user = queryCatching {
        val entity = Tokens
            .selectAll()
            .where { Tokens.token eq token }
            .firstOrNull()!!

        Users
            .selectAll()
            .where { Users.id eq entity[Tokens.userId] }
            .firstOrNull()
    }

    return user?.run {
        UserDto(
            id = get(Users.id).toString(),
            avatar = get(Users.avatar),
            name = get(Users.name).orEmpty()
        )
    }
}