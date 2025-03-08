package com.example.models

import com.google.gson.annotations.SerializedName

data class QuestionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("text")
    val text: String?,
    @SerializedName("variants")
    val variants: List<AnswerDto>?,
    @SerializedName("type")
    val type: String?
)