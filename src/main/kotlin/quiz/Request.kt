package com.example.quiz

import com.example.models.QuizDto
import com.google.gson.annotations.SerializedName

data class RegisterQuizRequest(
    @SerializedName("quiz")
    val quiz: QuizDto,
    @SerializedName("right_answers")
    val rightAnswers: Map<Int, List<Int>>
)

class SolveQuizRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("answers")
    val answers: Map<String, List<String>>
)