package com.example.models

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("name")
    val name: String,
)