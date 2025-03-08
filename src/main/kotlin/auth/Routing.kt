package com.example.auth

import com.example.auth.request.AuthRequest
import com.example.auth.request.LogoutRequest
import com.example.common.resolveUser
import com.example.common.respondRes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.authRouting() {
    val service = AuthService()

    install(Authentication) {
        bearer {
            authenticate { credentials ->
                val user = resolveUser(credentials.token)

                if (user != null) {
                    credentials.token
                } else {
                    null
                }
            }
        }
    }

    routing {
        route("auth") {
            post("/register") {
                val request = call.receive<AuthRequest>()
                val response = service.register(request)
                call.respondRes(response)
            }
        }

        post("/login") {
            val request = call.receive<AuthRequest>()
            println("login; request = $request")
            val response = service.login(request)
            println("response = $response")
            call.respondRes(response)
        }

        post("/logout") {
            try {
                val request = call.receive<LogoutRequest>()
                service.logout(request)
                call.respond("")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        }
    }
}