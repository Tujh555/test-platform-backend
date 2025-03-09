package com.example.auth

import com.example.auth.database.Tokens
import com.example.auth.database.Users
import com.example.auth.request.AuthRequest
import com.example.auth.request.AuthResponse
import com.example.auth.request.LogoutRequest
import com.example.common.*
import com.example.models.UserDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthService {
    suspend fun register(request: AuthRequest): Response<AuthResponse> = query {
        if (resolveUser(request.email) != null) {
            return@query _error(401)
        }
        val (user, token) = insertNew(request.email, request.password)
        success { AuthResponse(user, token) }
    }

    suspend fun login(request: AuthRequest): Response<AuthResponse> = query {
        val existing = resolveUser(request.email) ?: return@query _error(402)
        if (BCrypt.checkpw(request.password, existing[Users.password]).not()) {
            return@query _error(403)
        }
        val freshToken = UUID.randomUUID().toString()
        Tokens.insert {
            it[userId] = existing[Users.id]
            it[token] = freshToken
        }
        val dto = with(existing) {
            UserDto(
                id = get(Users.id).toString(),
                name = get(Users.name).orEmpty(),
                avatar = get(Users.avatar)
            )
        }
        success { AuthResponse(dto, freshToken) }
    }

    suspend fun logout(request: LogoutRequest) {
        queryCatching { Tokens.deleteWhere { userId eq UUID.fromString(request.id) } }
    }

    private fun resolveUser(email: String) = Users
        .selectAll()
        .where { Users.email eq email }
        .firstOrNull()

    private fun insertNew(email: String, password: String): Pair<UserDto, String> {
        val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = Users.insert {
            it[Users.email] = email
            it[Users.password] = hashed
        }
        val freshToken = UUID.randomUUID().toString()
        Tokens.insert {
            it[userId] = user[Users.id]
            it[token] = freshToken
        }
        val dto = UserDto(
            id = user[Users.id].toString(),
            avatar = null,
            name = ""
        )

        return dto to freshToken
    }
}