package com.example.auth.request

import com.example.models.UserDto
import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class AuthResponse(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("token")
    val token: String
)

data class LogoutRequest(@SerializedName("id") val id: String)