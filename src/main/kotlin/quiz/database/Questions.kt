package com.example.quiz.database

import org.jetbrains.exposed.sql.Table

object Questions : Table() {
    val id = uuid("id").autoGenerate()
    val quizId = uuid("quiz_id").references(Quizzes.id)
    val text = text("text")
    val type = text("type")

    override val primaryKey = PrimaryKey(id)
}