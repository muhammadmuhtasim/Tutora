package com.example.tutora.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class RegistrationUiState(
    val step: Int = 1,
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val phoneNumber: String = "",
    val addressQuery: String = "",
    val addressResults: List<LocationResult> = emptyList(),
    val selectedLocation: LocationResult? = null,
    val qualification: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) = _uiState.update { it.copy(name = name) }
    fun onRoleChanged(role: UserRole) = _uiState.update { it.copy(role = role) }
    fun onPhoneChanged(phone: String) = _uiState.update { it.copy(phoneNumber = phone) }
    fun onAddressQueryChanged(query: String) {
        val trimmed = query.trim()
        _uiState.update { it.copy(addressQuery = query) }
        searchAddress(trimmed)
    }
    fun onLocationSelected(result: LocationResult, advance: Boolean = false) {
        _uiState.update { it.copy(selectedLocation = result, addressQuery = result.address, addressResults = emptyList()) }
        if (advance) nextStep()
    }
    fun onQualificationChanged(q: String) = _uiState.update { it.copy(qualification = q) }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun detectCurrentLocation() {
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            var locResult: AppResult<GeoPoint> = AppResult.Error(AppError.Unknown("Initial state"))
            
            // Try to get location with two shorter attempts to be more responsive
            for (attempt in 1..2) {
                try {
                    locResult = withTimeout(8000.milliseconds) { locationRepository.getCurrentLocation() }
                    if (locResult is AppResult.Success) break
                } catch (_: TimeoutCancellationException) {
                    Log.d("RegistrationViewModel", "Location attempt $attempt timed out")
                } catch (e: Exception) {
                    Log.e("RegistrationViewModel", "Location attempt $attempt failed", e)
                }
                if (attempt < 2) delay(500.milliseconds)
            }

            when (locResult) {
                is AppResult.Success -> {
                    val geo = locResult.data
                    val addressTask = async { 
                        withTimeoutOrNull(5000.milliseconds) { locationRepository.reverseGeocode(geo) }
                    }
                    
                    val addressResult = addressTask.await()
                    
                    if (addressResult is AppResult.Success) {
                        _uiState.update { it.copy(
                            selectedLocation = LocationResult(addressResult.data, geo),
                            addressQuery = addressResult.data,
                            isLoading = false,
                            successMessage = "Location detected: ${addressResult.data}",
                        )}
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false, 
                            error = "Location found, but could not resolve address. Please search manually.",
                            selectedLocation = LocationResult("Unknown Location", geo) 
                        ) }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = "Could not detect location. Please ensure GPS is ON and permissions are granted.") }
                }
            }
        }
    }
    fun onEmailChanged(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPasswordChanged(p: String) = _uiState.update { it.copy(password = p, error = null) }

    fun nextStep() {
        val state = _uiState.value
        when (state.step) {
            1 -> {
                if (state.name.isBlank()) {
                    _uiState.update { it.copy(error = "Please enter your name") }
                    return
                }
                if (state.phoneNumber.isBlank()) {
                    _uiState.update { it.copy(error = "Please enter your phone number") }
                    return
                }
            }
            2 -> {
                if (state.selectedLocation == null) {
                    _uiState.update { it.copy(error = "Please select a location from search suggestions or use GPS") }
                    return
                }
            }
            3 -> {
                if (state.role == UserRole.TUTOR && state.qualification.isBlank()) {
                    _uiState.update { it.copy(error = "Tutors must provide their academic qualifications") }
                    return
                }
            }
        }
        _uiState.update { it.copy(step = it.step + 1, error = null) }
    }

    fun prevStep() = _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(1), error = null) }

    private var searchJob: Job? = null

    private fun searchAddress(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.update { it.copy(addressResults = emptyList()) }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(500)
            when (val result = locationRepository.geocode(query)) {
                is AppResult.Success -> _uiState.update { it.copy(addressResults = result.data) }
                is AppResult.Error -> {}
            }
        }
    }

    fun resetState() {
        searchJob?.cancel()
        _uiState.value = RegistrationUiState()
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun register() {
        val state = _uiState.value
        val trimmedName = state.name.trim()
        val trimmedPhone = state.phoneNumber.trim()
        val trimmedEmail = state.email.trim()
        val trimmedQualification = state.qualification.trim()
        
        // Basic Validations
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name") }
            return
        }

        if (state.role == UserRole.TUTOR && trimmedQualification.isBlank()) {
            _uiState.update { it.copy(error = "Tutors must provide their academic qualifications") }
            return
        }
        
        if (state.selectedLocation == null) {
            _uiState.update { it.copy(error = "Please select a location from the map or suggestions") }
            return
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$".toRegex()
        if (!emailRegex.matches(trimmedEmail)) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }

        val cleanPassword = state.password.trim()
        if (cleanPassword.length < 8 || !cleanPassword.any { it.isDigit() } || !cleanPassword.any { it.isUpperCase() }) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters long and contain at least one digit and one uppercase letter.") }
            return
        }

        val user = User(
            name = trimmedName,
            role = state.role,
            phoneNumber = trimmedPhone,
            region = state.selectedLocation.address,
            location = state.selectedLocation.location,
            qualification = trimmedQualification
        )

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.register(user, trimmedEmail, cleanPassword)) {
                is AppResult.Success -> {
                    authRepository.logout()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
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
                    _uiState.update { it.copy(isLoading = false, error = message) }
                }
            }
        }
    }
}
