package com.example.quiz.database

import com.example.auth.database.Users
import com.example.common.instant
import org.jetbrains.exposed.sql.Table

object Quizzes : Table() {
    val id = uuid("id").autoGenerate()
    val title = text("title")
    val duration = integer("duration")
    val authorId = uuid("author_id").references(Users.id)
    val solversId = array<String>("solvers_id").default(emptyList())
    val createdAt = instant("created_at")
    val theme = text("theme")

    override val primaryKey = PrimaryKey(id)
}