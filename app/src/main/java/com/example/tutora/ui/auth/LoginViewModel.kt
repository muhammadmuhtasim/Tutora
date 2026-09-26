package com.example.tutora.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun login() {
        val state = _uiState.value
        val trimmedEmail = state.email.trim()
        val password = state.password.trim()

        if (trimmedEmail.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address.") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your password.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = authRepository.login(trimmedEmail, password)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AppResult.Error -> {
                    val message = when (val error = result.error) {
                        is AppError.Unknown -> error.message
                        is AppError.ServerRejected -> error.reason
                        AppError.NoConnectivity -> "No internet connection"
                        AppError.NetworkError -> "Network error"
                        AppError.PermissionDenied -> "Permission denied"
                        is AppError.AuthError -> error.message
                        is AppError.DatabaseError -> error.message
                    }
                    val sanitizedMessage = if (message.isNullOrBlank() || message.contains("PERMISSION_DENIED", ignoreCase = true)) {
                        "Invalid email or password. Please try again."
                    } else message
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = sanitizedMessage
                    ) }
                }
            }
        }
    }

    private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
        value = block(value)
    }
}
