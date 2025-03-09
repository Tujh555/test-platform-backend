package com.example.quiz

import com.example.common.resolveUser
import com.example.common.respondRes
import com.example.quiz.service.QuizService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.time.Instant

fun Application.quizRouting() {
    val service = QuizService()

    routing {
        authenticate {
            route("quizzes") {
                get("/all") {
                    try {
                        val limit = call.queryParameters.getOrFail("limit").toInt()
                        val cursor = Instant.parse(call.queryParameters.getOrFail("cursor"))
                        call.respondRes(service.allQuizzes(limit, cursor))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                get("/own") {
                    val user = call.principal<String>()
                        ?.let { resolveUser(it) }
                        ?: return@get call.respond(HttpStatusCode.Unauthorized)

                    try {
                        val limit = call.queryParameters.getOrFail("limit").toInt()
                        val cursor = Instant.parse(call.queryParameters.getOrFail("cursor"))
                        call.respondRes(service.ownQuizzes(limit, cursor, user.id))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                get("/search") {
                    try {
                        val limit = call.queryParameters.getOrFail("limit").toInt()
                        val cursor = Instant.parse(call.queryParameters.getOrFail("cursor"))
                        val query = call.queryParameters.getOrFail("search_by")
                        call.respondRes(service.search(limit, cursor, query))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                put("/register") {
                    val request = call.receive<RegisterQuizRequest>()
                    service.register(request)
                    call.respond(HttpStatusCode.OK)
                }

                post("/solve") {
                    val user = call.principal<String>()
                        ?.let { resolveUser(it) }
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val request = call.receive<SolveQuizRequest>()
                    call.respondRes(service.solve(request, user.id))
                }
            }
        }
    }
}