package com.example.tutora.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tutora.ui.theme.TutoraStyles
import com.example.tutora.ui.components.FloatingBackButton
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.AppResult
import com.example.tutora.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, error = null, step = 1) }
    fun onNewPasswordChange(v: String) = _uiState.update { it.copy(newPassword = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }

    fun reset() {
        val cleanEmail = _uiState.value.email.trim()
        if (cleanEmail.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.checkUserExists(cleanEmail)) {
                is AppResult.Success -> {
                    if (result.data) {
                        _uiState.update { it.copy(isLoading = false, step = 2) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Account email not found in our records.") }
                    }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Verification failed. Please check your network connection.") }
            }
        }
    }

    fun confirm() {
        val state = _uiState.value
        val cleanEmail = state.email.trim()
        val newPassword = state.newPassword.trim()
        val confirmPassword = state.confirmPassword.trim()
        
        if (newPassword.isBlank()) {
            _uiState.update { it.copy(error = "Password field cannot be empty.") }
            return
        }
        if (newPassword.length < 8 || !newPassword.any { it.isDigit() } || !newPassword.any { it.isUpperCase() }) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters long and contain at least one digit and one uppercase letter.") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match. Please try again.") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Effectively persist the new password to the shadow field
            when (val res = authRepository.resetPassword(cleanEmail, newPassword)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Failed to update password.") }
            }
        }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val step: Int = 1,
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                "Reset Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (uiState.isSuccess) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Success! Your password has been successfully updated.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Back to Login")
                        }
                    }
                }
            } else {
                if (uiState.step == 1) {
                    Text("Enter your email to locate your account.", modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = TutoraStyles.outlinedTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { viewModel.reset() }
                        )
                    )
                    Button(
                        onClick = viewModel::reset,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TutoraStyles.primaryButtonColors(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Verify Email")
                    }
                } else {
                    Text("Account located! Please enter your new password below.", modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = viewModel::onNewPasswordChange,
                        label = { Text("New Password") },
                        supportingText = { Text("At least 8 chars, 1 digit, 1 uppercase letter", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        colors = TutoraStyles.outlinedTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        colors = TutoraStyles.outlinedTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.confirm() }
                        )
                    )
                    
                    Button(
                        onClick = viewModel::confirm,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TutoraStyles.primaryButtonColors(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Update Password")
                    }
                }
            }

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        uiState.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        FloatingBackButton(onBack = onNavigateBack)
    }
}
