package com.example.tutora.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BookingFlowUiState(
    val step: Int = 1,
    val post: Post? = null,
    val selectedDateMillis: Long? = null,
    val selectedHour: Int? = null,
    val selectedMinute: Int? = null,
    val createdBookingId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val selectedDateText: String
        get() = selectedDateMillis?.let {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date(it))
        } ?: ""

    val selectedTimeText: String
        get() = if (selectedHour != null && selectedMinute != null) {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
            cal.set(java.util.Calendar.MINUTE, selectedMinute)
            java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(cal.time)
        } else ""
}

@HiltViewModel
class BookingFlowViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val bookingRepository: BookingRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingFlowUiState())
    val uiState: StateFlow<BookingFlowUiState> = _uiState.asStateFlow()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSuccess = false, step = 1) }
            when (val result = postRepository.getPostById(postId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(post = result.data, isLoading = false) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = "Post not found") }
                }
            }
        }
    }

    fun onDateChange(millis: Long?) = _uiState.update { it.copy(selectedDateMillis = millis) }
    fun onTimeChange(hour: Int, minute: Int) = _uiState.update { it.copy(selectedHour = hour, selectedMinute = minute) }

    fun nextStep() = _uiState.update { it.copy(step = it.step + 1) }
    fun prevStep() = _uiState.update { it.copy(step = it.step - 1) }

    fun confirmBooking(post: Post) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedDateMillis == null || state.selectedHour == null || state.selectedMinute == null) {
                _uiState.update { it.copy(error = "Please select date and time") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val user = userResult.data
                if (user != null) {
                    val bId = UUID.randomUUID().toString()
                    val startTs = calculateStartTimestamp(state.selectedDateMillis, state.selectedHour, state.selectedMinute)
                    val endTs = calculateEndTimestamp(startTs, post.sessionLength)
                    
                    val booking = Booking(
                        id = bId,
                        postId = post.id,
                        studentId = user.id,
                        tutorId = post.creatorId,
                        status = BookingStatus.PENDING,
                        timestamp = System.currentTimeMillis(),
                        sessionDate = state.selectedDateText,
                        sessionTime = state.selectedTimeText,
                        scheduledEndTimestamp = endTs
                    )

                    val bookingRes = bookingRepository.createBooking(booking)
                    if (bookingRes is AppResult.Success) {
                        chatRepository.createChatSession(listOf(user.id, post.creatorId))
                        _uiState.update { it.copy(isLoading = false, isSuccess = true, step = 3, createdBookingId = bId) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to book") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "User profile missing") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Auth error") }
            }
        }
    }

    private fun calculateStartTimestamp(dateMillis: Long, hour: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        cal.set(java.util.Calendar.MINUTE, minute)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun calculateEndTimestamp(startTs: Long, durationStr: String): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startTs
        
        // Robustly parse duration, handling decimals like "1.5 hours"
        val durationValue = durationStr.lowercase().split(" ")
            .firstOrNull { it.any { c -> c.isDigit() } }
            ?.toDoubleOrNull() ?: 1.0

        val durationMinutes = when {
            durationStr.contains("hour", true) -> (durationValue * 60).toInt()
            durationStr.contains("min", true) -> durationValue.toInt()
            else -> 60
        }
        
        cal.add(java.util.Calendar.MINUTE, durationMinutes)
        return cal.timeInMillis
    }

    fun resetState() {
        _uiState.value = BookingFlowUiState()
    }
}
