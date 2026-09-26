package com.example.tutora.domain

data class GeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class UserRole {
    STUDENT, TUTOR
}

data class User(
    val id: String = "",
    val authUid: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val region: String = "",
    val location: GeoPoint = GeoPoint(),
    val qualification: String = "",
    val bio: String = "",
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val contactInfo: List<String> = emptyList(),
    val favorites: List<String> = emptyList(), // Post IDs
    val blockedUserIds: List<String> = emptyList(),
    val profileImageUrl: String = ""
)

enum class PostType {
    TUITION_REQUEST, // Student looking for tutor
    TUITION_OFFER    // Tutor looking for students
}

enum class BookingStatus {
    PENDING, ACCEPTED, REJECTED, PREPARING, ON_THE_WAY, ARRIVED, COMPLETED, CANCELLED
}

data class Post(
    val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val creatorProfileImageUrl: String = "",
    val type: PostType = PostType.TUITION_REQUEST,
    val classType: String = "",
    val subjects: List<String> = emptyList(),
    val sessionLength: String = "",
    val sessionsPerWeek: Int = 0,
    val location: GeoPoint = GeoPoint(),
    val geohash: String = "",
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val description: String = "",
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val viewsCount: Int = 0
)

data class Booking(
    val id: String = "",
    val postId: String = "",
    val studentId: String = "",
    val tutorId: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val contactVisibleToTutor: Boolean = false,
    val contactVisibleToStudent: Boolean = false,
    val trackingProgress: Float = 0f,
    val sessionDate: String = "",
    val sessionTime: String = "",
    val scheduledEndTimestamp: Long = 0,
    val cancellationTimestamp: Long? = null
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatSession(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis()
)

data class Review(
    val id: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val reviewerProfileImageUrl: String = "",
    val targetUserId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Report(
    val id: String = "",
    val reporterId: String = "",
    val targetUserId: String = "",
    val targetPostId: String? = null,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
