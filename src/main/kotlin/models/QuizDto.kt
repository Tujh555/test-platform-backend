package com.example.models

import com.google.gson.annotations.SerializedName

class QuizDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("duration")
    val durationMinutes: Int?,
    @SerializedName("questions")
    val questions: List<QuestionDto>?,
    @SerializedName("author")
    val author: UserDto?,
    @SerializedName("solved_count")
    val solvedCount: Int?,
    @SerializedName("last_solvers")
    val lastSolvers: List<UserDto>?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("theme")
    val theme: String?
)