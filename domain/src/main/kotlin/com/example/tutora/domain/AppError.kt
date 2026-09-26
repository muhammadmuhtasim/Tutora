package com.example.tutora.domain

sealed interface AppError {
    data object NoConnectivity : AppError
    data object NetworkError : AppError
    data object PermissionDenied : AppError
    data class AuthError(val message: String) : AppError
    data class DatabaseError(val message: String) : AppError
    data class ServerRejected(val reason: String) : AppError
    data class Unknown(val message: String, val cause: Throwable? = null) : AppError
}

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
}
