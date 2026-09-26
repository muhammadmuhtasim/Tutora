package com.example.tutora.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import com.example.tutora.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class BookingUiState(
    val bookings: List<Booking> = emptyList(),
    val otherParties: Map<String, User> = emptyMap(), // userId -> User
    val isLoading: Boolean = false,
    val currentUserId: String = "",
    val userRole: UserRole = UserRole.STUDENT,
    val error: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    private var bookingsJob: kotlinx.coroutines.Job? = null

    private fun loadBookings() {
        bookingsJob?.cancel()
        bookingsJob = safeLaunch(onError = { error ->
            _uiState.update { it.copy(isLoading = false, error = "Failed to load bookings: ${error.message}") }
        }) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val user = userResult.data
                if (user != null) {
                    _uiState.update { it.copy(userRole = user.role, currentUserId = user.id) }
                    
                    bookingRepository.getBookingsForUser(user.id, user.role)
                        .catch { error ->
                            _uiState.update { it.copy(isLoading = false, error = "Failed to load bookings. Please check your internet or contact support.") }
                        }
                        .collect { bookings ->
                            _uiState.update { it.copy(bookings = bookings, isLoading = false) }
                            // Pre-fetch other party names/profiles if needed
                            fetchOtherParties(bookings, user.id)
                        }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Login required") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Login required") }
            }
        }
    }

    private fun fetchOtherParties(bookings: List<Booking>, currentUserId: String) {
        val otherIds = bookings.map { 
            if (it.studentId == currentUserId) it.tutorId else it.studentId 
        }.distinct()
        
        viewModelScope.launch {
            val parties = otherIds.map { id ->
                async { id to authRepository.getUserById(id) }
            }.awaitAll().mapNotNull { pair ->
                val result = pair.second
                if (result is AppResult.Success) {
                    result.data?.let { pair.first to it }
                } else null
            }.toMap()
            
            _uiState.update { it.copy(otherParties = parties) }
        }
    }

    fun approveBooking(bookingId: String) {
        safeLaunch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.ACCEPTED)
            bookingRepository.setContactVisibility(bookingId, tutorVisible = true, studentVisible = true)
        }
    }

    fun rejectBooking(bookingId: String) {
        safeLaunch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.REJECTED)
        }
    }

    fun completeBooking(bookingId: String) {
        safeLaunch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
        }
    }

    fun startPreparing(bookingId: String) {
        safeLaunch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.PREPARING)
        }
    }

    fun startJourney(bookingId: String) {
        safeLaunch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.ON_THE_WAY)
        }
    }

    fun markArrived(bookingId: String) {
        safeLaunch {
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.ARRIVED)
        }
    }

    fun cancelBooking(bookingId: String, immediate: Boolean) {
        safeLaunch {
            bookingRepository.cancelBooking(bookingId, immediate)
        }
    }

    fun deleteBooking(bookingId: String) {
        safeLaunch {
            bookingRepository.deleteBooking(bookingId)
        }
    }

    fun onChatClicked(booking: Booking, onResolved: (String) -> Unit) {
        safeLaunch {
            val result = chatRepository.createChatSession(listOf(booking.studentId, booking.tutorId))
            if (result is AppResult.Success) {
                onResolved(result.data)
            }
        }
    }

    fun resetState() {
        bookingsJob?.cancel()
        _uiState.value = BookingUiState()
    }
}
