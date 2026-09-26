package com.example.tutora.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(user: User, email: String, password: String): AppResult<User>
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun getCurrentUser(): AppResult<User?>
    fun observeCurrentUser(): Flow<User?>
    suspend fun getUserById(userId: String): AppResult<User?>
    suspend fun logout()
    suspend fun updateUserProfile(user: User): AppResult<Unit>
    suspend fun deleteAccount(): AppResult<Unit>
    
    // Recovery & Discovery
    suspend fun findEmailByPhoneNumber(phoneNumber: String): AppResult<String?>
    suspend fun checkUserExists(email: String): AppResult<Boolean>
    suspend fun resetPassword(email: String, newPassword: String): AppResult<Unit>
    
    // Social & Safety
    suspend fun toggleFavorite(postId: String): AppResult<Unit>
    suspend fun getReviewsForUser(userId: String): AppResult<List<Review>>
    suspend fun blockUser(targetUserId: String): AppResult<Unit>
    suspend fun unblockUser(targetUserId: String): AppResult<Unit>
    suspend fun reportUser(report: Report): AppResult<Unit>
    suspend fun rateUser(review: Review): AppResult<Unit>
}

interface PostRepository {
    suspend fun createPost(post: Post): AppResult<Unit>
    suspend fun updatePost(post: Post): AppResult<Unit>
    suspend fun deletePost(postId: String): AppResult<Unit>
    suspend fun getPostById(postId: String): AppResult<Post?>
    
    fun getPosts(
        center: GeoPoint,
        radiusInKm: Double,
        type: PostType? = null,
        classType: String? = null,
        subjects: List<String>? = null,
        tags: List<String>? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        minRating: Double? = null,
        searchQuery: String? = null,
        limit: Int = 100
    ): Flow<List<Post>>

    fun getPostsByCreatorId(creatorId: String): Flow<List<Post>>
    fun getPostsByIds(postIds: List<String>): Flow<List<Post>>
    suspend fun incrementPostViews(postId: String): AppResult<Unit>
}

interface BookingRepository {
    suspend fun createBooking(booking: Booking): AppResult<Unit>
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): AppResult<Unit>
    suspend fun setContactVisibility(bookingId: String, tutorVisible: Boolean, studentVisible: Boolean): AppResult<Unit>
    suspend fun cancelBooking(bookingId: String, immediate: Boolean): AppResult<Unit>
    suspend fun completeBooking(bookingId: String): AppResult<Unit>
    suspend fun deleteBooking(bookingId: String): AppResult<Unit>
    fun getBookingsForUser(userId: String, role: UserRole): Flow<List<Booking>>
    fun getBookingById(bookingId: String): Flow<Booking?>
}

interface ChatRepository {
    fun getChatSessions(userId: String): Flow<List<ChatSession>>
    fun getSessionById(sessionId: String): Flow<ChatSession?>
    fun getMessages(sessionId: String, limit: Int = 50): Flow<List<ChatMessage>>
    suspend fun sendMessage(sessionId: String, message: ChatMessage): AppResult<Unit>
    suspend fun createChatSession(participantIds: List<String>): AppResult<String>
    suspend fun clearChat(sessionId: String): AppResult<Unit>
    suspend fun deleteChatSession(sessionId: String): AppResult<Unit>
}

interface LocationRepository {
    suspend fun getCurrentLocation(): AppResult<GeoPoint>
    suspend fun geocode(address: String): AppResult<List<LocationResult>>
    suspend fun reverseGeocode(location: GeoPoint): AppResult<String>
}

data class LocationResult(
    val address: String,
    val location: GeoPoint
)

interface ConnectivityObserver {
    fun isOnline(): Flow<Boolean>
}

interface StorageRepository {
    suspend fun uploadProfileImage(userId: String, bytes: ByteArray, contentType: String): AppResult<String>
}
