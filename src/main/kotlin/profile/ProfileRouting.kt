package com.example.profile

import com.example.common.resolveUser
import com.example.common.respondRes
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.profileRouting() {
    val service = ProfileService()

    routing {
        static("/root") {
            files("root")
        }

        authenticate {
            route("user") {
                post("/avatar") {
                    val user = call.principal<String>()
                        ?.let { resolveUser(it) }
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    call.receiveMultipart().forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val response = service.loadAvatar(user.id, part.provider())
                            call.respondRes(response)
                        }
                        part.dispose()
                    }
                }

                patch("/name") {
                    val user = call.principal<String>()
                        ?.let { resolveUser(it) }
                        ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                    val newName = call.receive<String>()
                    service.changeName(user.id, newName)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}

object Root {
    val folder by lazy {
        File("root").apply {
            if (exists().not()) {
                mkdirs()
            }
        }
    }
}