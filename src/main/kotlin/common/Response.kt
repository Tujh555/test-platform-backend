package com.example.common

sealed interface Response<out T> {
    data class Success<out T>(val data: T) : Response<T>

    data class Error(val error: BaseError) : Response<Nothing>
}

data class BaseError(val code: Int, val message: String)

inline fun <T> success(factory: () -> T) = Response.Success(factory())

fun _error(code: Int, message: String = "") = Response.Error(BaseError(code, message))