package com.example.models

import com.google.gson.annotations.SerializedName

data class AnswerDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("text")
    val text: String?
)