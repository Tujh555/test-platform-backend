package com.example

import com.example.auth.authRouting
import com.example.profile.profileRouting
import com.example.quiz.quizRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    authRouting()
    profileRouting()
    quizRouting()
}