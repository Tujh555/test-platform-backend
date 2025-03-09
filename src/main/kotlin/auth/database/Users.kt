package com.example.auth.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = uuid("id").autoGenerate()
    val email = text("email").uniqueIndex()
    val password = text("password")
    val name = text("name").nullable().default(null)
    val avatar = text("avatar").nullable().default(null)

    override val primaryKey = PrimaryKey(id)
}

object Tokens : Table() {
    val id = integer("id").autoIncrement()
    val userId = uuid("user_id").references(Users.id)
    val token = text("token").uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}