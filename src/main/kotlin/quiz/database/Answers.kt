package com.example.quiz.database

import org.jetbrains.exposed.sql.Table

object Answers : Table() {
    val id = uuid("id").autoGenerate()
    val questionId = uuid("question_id").references(Questions.id)
    val text = text("text")
    val isRight = bool("is_right")

    override val primaryKey = PrimaryKey(id)
}