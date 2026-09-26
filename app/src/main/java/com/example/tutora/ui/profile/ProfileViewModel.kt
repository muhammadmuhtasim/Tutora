package com.example.tutora.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutora.domain.*
import com.example.tutora.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay

data class ProfileUiState(
    val user: User? = null,
    val name: String = "",
    val bio: String = "",
    val qualification: String = "",
    val phoneNumber: String = "",
    val region: String = "",
    val location: GeoPoint? = null,
    val contactInfo: List<String> = emptyList(),
    val addressQuery: String = "",
    val addressResults: List<LocationResult> = emptyList(),
    val myPosts: List<Post> = emptyList(),
    val blockedUsers: List<User> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isBlocked: Boolean = false,
    val canRate: Boolean = false,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val profileImageUrl: String = "",
    val uploadingProfileImage: Boolean = false,
    val profileImageUploadError: String? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val logoutSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val bookingRepository: BookingRepository,
    private val locationRepository: LocationRepository,
    private val storageRepository: StorageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        safeLaunch {
            _uiState.update { it.copy(isLoading = true, saveSuccess = false, error = null, myPosts = emptyList()) }
            when (val result = authRepository.getCurrentUser()) {
                is AppResult.Success -> {
                    val user = result.data
                    if (user != null) {
                        _uiState.update { it.copy(
                            user = user,
                            name = user.name,
                            bio = user.bio,
                            qualification = user.qualification,
                            phoneNumber = user.phoneNumber,
                            region = user.region,
                            location = user.location,
                            contactInfo = user.contactInfo,
                            addressQuery = user.region,
                            profileImageUrl = user.profileImageUrl,
                            isLoading = false,
                            error = null
                        ) }
                        loadMyPosts(user.id)
                        loadBlockedUsers(user.blockedUserIds)
                        loadReviews(user.id)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "User not found") }
                    }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Failed to load profile") }
            }
        }
    }

    fun loadPublicProfile(userId: String) {
        safeLaunch {
            _uiState.update { it.copy(isLoading = true, error = null, myPosts = emptyList(), canRate = false) }
            val currentUserResult = authRepository.getCurrentUser()
            val currentUser = (currentUserResult as? AppResult.Success)?.data
            val currentUserId = currentUser?.id

            when (val result = authRepository.getUserById(userId)) {
                is AppResult.Success -> {
                    val user = result.data
                    _uiState.update { it.copy(
                        user = user, 
                        isLoading = false,
                        isBlocked = currentUser?.blockedUserIds?.contains(userId) == true
                    ) }
                    if (user != null) {
                        loadMyPosts(user.id)
                        if (currentUserId != null) {
                            checkRatingEligibility(currentUserId, userId)
                        }
                        loadReviews(user.id)
                    }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Failed to load user") }
            }
        }
    }

    fun toggleBlock(targetUserId: String) {
        val currentlyBlocked = _uiState.value.isBlocked
        if (currentlyBlocked) unblockUser(targetUserId) else blockUser(targetUserId)
    }

    private fun checkRatingEligibility(myId: String, targetId: String) {
        safeLaunch {
            // Check if there's any COMPLETED booking between these two
            val studentBookings = bookingRepository.getBookingsForUser(myId, UserRole.STUDENT)
            val tutorBookings = bookingRepository.getBookingsForUser(myId, UserRole.TUTOR)
            
            combine(studentBookings, tutorBookings) { studentList, tutorList ->
                val hasCompletedAsStudent = studentList.any { it.tutorId == targetId && it.status == BookingStatus.COMPLETED }
                val hasCompletedAsTutor = tutorList.any { it.studentId == targetId && it.status == BookingStatus.COMPLETED }
                hasCompletedAsStudent || hasCompletedAsTutor
            }
            .catch { _uiState.update { it.copy(canRate = false) } }
            .collectLatest { eligible ->
                _uiState.update { it.copy(canRate = eligible) }
            }
        }
    }

    private fun loadMyPosts(userId: String) {
        safeLaunch {
            postRepository.getPostsByCreatorId(userId)
                .catch { _uiState.update { it.copy(error = "Failed to load posts") } }
                .collectLatest { posts ->
                    _uiState.update { it.copy(myPosts = posts) }
                }
        }
    }

    private fun loadReviews(userId: String) {
        safeLaunch {
            when (val result = authRepository.getReviewsForUser(userId)) {
                is AppResult.Success -> _uiState.update { it.copy(reviews = result.data) }
                is AppResult.Error -> {}
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        safeLaunch {
            _uiState.update { it.copy(uploadingProfileImage = true, profileImageUploadError = null) }
            try {
                val stream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not read the selected image")
                val bytes = stream.use { it.readBytes() }
                val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val userResult = authRepository.getCurrentUser()
                val currentUser = (userResult as? AppResult.Success)?.data
                    ?: throw Exception("You are not logged in")

                when (val res = storageRepository.uploadProfileImage(currentUser.id, bytes, contentType)) {
                    is AppResult.Success -> _uiState.update { it.copy(
                        profileImageUrl = res.data,
                        user = currentUser.copy(profileImageUrl = res.data),
                        uploadingProfileImage = false
                    ) }
                    is AppResult.Error -> _uiState.update { it.copy(
                        uploadingProfileImage = false,
                        profileImageUploadError = (res.error as? AppError.Unknown)?.message ?: "Upload failed"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    uploadingProfileImage = false,
                    profileImageUploadError = e.message ?: "Upload failed"
                ) }
            }
        }
    }

    fun blockUser(targetUserId: String) {
        safeLaunch {
            authRepository.blockUser(targetUserId)
            // Refresh local user state so we know who is blocked
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val user = userResult.data
                if (user != null) {
                    _uiState.update { it.copy(user = user, isBlocked = true) }
                    loadBlockedUsers(user.blockedUserIds)
                }
            }
            _uiState.update { it.copy(error = "User blocked") }
        }
    }

    fun unblockUser(targetUserId: String) {
        safeLaunch {
            authRepository.unblockUser(targetUserId)
            val userResult = authRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                val user = userResult.data
                if (user != null) {
                    _uiState.update { it.copy(user = user, isBlocked = false) }
                    loadBlockedUsers(user.blockedUserIds)
                }
            }
            _uiState.update { it.copy(error = "User unblocked") }
        }
    }

    private fun loadBlockedUsers(userIds: List<String>) {
        safeLaunch {
            val users = userIds.mapNotNull { id ->
                val res = authRepository.getUserById(id)
                if (res is AppResult.Success) {
                    res.data
                } else null
            }
            _uiState.update { it.copy(blockedUsers = users) }
        }
    }

    fun reportUser(targetUserId: String, reason: String) {
        safeLaunch {
            val currentUserResult = authRepository.getCurrentUser()
            if (currentUserResult is AppResult.Success) {
                val currentUserId = currentUserResult.data?.id ?: return@safeLaunch
                val report = Report(
                    reporterId = currentUserId,
                    targetUserId = targetUserId,
                    reason = reason
                )
                authRepository.reportUser(report)
                _uiState.update { it.copy(error = "Report submitted") }
            }
        }
    }

    fun rateUser(targetUserId: String, rating: Int, comment: String) {
        safeLaunch {
            val currentUserResult = authRepository.getCurrentUser()
            if (currentUserResult is AppResult.Success) {
                val currentUserId = currentUserResult.data?.id ?: return@safeLaunch
                val review = Review(
                    reviewerId = currentUserId,
                    targetUserId = targetUserId,
                    rating = rating,
                    comment = comment
                )
                authRepository.rateUser(review)
                loadPublicProfile(targetUserId) // Refresh rating
            }
        }
    }

    fun onEditToggle() = _uiState.update { it.copy(isEditing = !it.isEditing) }
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onBioChange(v: String) = _uiState.update { it.copy(bio = v) }
    fun onQualificationChange(v: String) = _uiState.update { it.copy(qualification = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phoneNumber = v) }
    fun onProfileImageUrlChange(v: String) = _uiState.update { it.copy(profileImageUrl = v) }

    fun onAddressQueryChanged(query: String) {
        _uiState.update { it.copy(addressQuery = query) }
        searchAddress(query)
    }

    fun onLocationSelected(result: LocationResult) {
        _uiState.update { it.copy(
            region = result.address,
            location = result.location,
            addressQuery = result.address,
            addressResults = emptyList()
        ) }
    }

    fun onLocateMe() {
        safeLaunch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            var locResult: AppResult<GeoPoint> = AppResult.Error(AppError.Unknown("Initial state"))
            
            for (attempt in 1..2) {
                try {
                    // Increased timeout for better GPS fix reliability
                    locResult = withTimeout(15000.milliseconds) { locationRepository.getCurrentLocation() }
                    if (locResult is AppResult.Success) break
                } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                    android.util.Log.d("ProfileViewModel", "Location attempt $attempt timed out")
                } catch (e: Exception) {
                    android.util.Log.e("ProfileViewModel", "Location attempt $attempt failed", e)
                }
                if (attempt < 2) delay(1000.milliseconds)
            }

            when (locResult) {
                is AppResult.Success -> {
                    val geo = locResult.data
                    val addrResult = withTimeoutOrNull(5000.milliseconds) { 
                        locationRepository.reverseGeocode(geo) 
                    }
                    
                    if (addrResult is AppResult.Success) {
                        _uiState.update { it.copy(
                            region = addrResult.data,
                            location = geo,
                            addressQuery = addrResult.data,
                            isLoading = false
                        ) }
                    } else {
                        _uiState.update { it.copy(
                            region = "Detected Location",
                            location = geo,
                            addressQuery = "Detected Location",
                            isLoading = false,
                            error = "Location detected, but address lookup failed."
                        ) }
                    }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Failed to detect location") }
            }
        }
    }

    fun onAddContact(v: String) {
        if (v.isBlank()) return
        _uiState.update { it.copy(contactInfo = it.contactInfo + v) }
    }

    fun onRemoveContact(v: String) {
        _uiState.update { it.copy(contactInfo = it.contactInfo - v) }
    }

    fun logout() {
        safeLaunch {
            authRepository.logout()
            resetState()
            _uiState.update { it.copy(logoutSuccess = true) }
        }
    }

    fun consumeLogoutState() {
        _uiState.update { it.copy(logoutSuccess = false, deleteSuccess = false) }
    }

    fun resetState() {
        _uiState.value = ProfileUiState()
    }

    fun deleteAccount() {
        safeLaunch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.deleteAccount()) {
                is AppResult.Success -> _uiState.update { it.copy(deleteSuccess = true, isLoading = false) }
                is AppResult.Error -> {
                    val message = when (val error = result.error) {
                        is AppError.AuthError -> error.message
                        else -> "Delete failed"
                    }
                    _uiState.update { it.copy(isLoading = false, error = message) }
                }
            }
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null
    private fun searchAddress(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.update { it.copy(addressResults = emptyList()) }
            return
        }
        searchJob = safeLaunch {
            delay(500)
            when (val result = locationRepository.geocode(query)) {
                is AppResult.Success -> _uiState.update { it.copy(addressResults = result.data) }
                is AppResult.Error -> {}
            }
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        val currentUser = state.user ?: return
        
        val updatedUser = currentUser.copy(
            name = state.name,
            bio = state.bio,
            qualification = state.qualification,
            phoneNumber = state.phoneNumber,
            region = state.region,
            location = state.location ?: currentUser.location,
            contactInfo = state.contactInfo,
            profileImageUrl = state.profileImageUrl
        )

        safeLaunch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.updateUserProfile(updatedUser)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(
                        user = updatedUser,
                        isLoading = false,
                        isEditing = false,
                        error = null,
                        saveSuccess = true // To trigger navigation back to view
                    ) }
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, error = "Update failed") }
            }
        }
    }
}
