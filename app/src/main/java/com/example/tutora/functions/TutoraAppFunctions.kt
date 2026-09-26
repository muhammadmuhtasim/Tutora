package com.example.tutora.functions

import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import com.example.tutora.domain.*
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.util.UUID

/**
 * Tutora App Functions for AI agents.
 * Provides workflows for searching tuition posts and managing bookings.
 */
class TutoraAppFunctions @Inject constructor(
    private val postRepository: PostRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) {
    /**
     * Search for nearby tutors or tuition requests based on type and class.
     * @param context The execution context.
     * @param type The type of post to search for: "TUITION_OFFER" or "TUITION_REQUEST".
     * @param classType The class or grade level (e.g., "Grade 10").
     * @return A list of posts matching the criteria.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchPosts(
        context: AppFunctionContext,
        type: String? = null,
        classType: String? = null
    ): List<AppPost> = withContext(Dispatchers.IO) {
        val userResult = authRepository.getCurrentUser()
        val user = (userResult as? AppResult.Success)?.data 
            ?: throw IllegalStateException("User not logged in or profile missing")
            
        val postType = when (type) {
            "TUITION_OFFER" -> PostType.TUITION_OFFER
            "TUITION_REQUEST" -> PostType.TUITION_REQUEST
            else -> if (user.role == UserRole.STUDENT) PostType.TUITION_OFFER else PostType.TUITION_REQUEST
        }

        val posts = postRepository.getPosts(
            center = user.location,
            radiusInKm = 5.0,
            type = postType,
            classType = classType
        ).first()

        posts.map { it.toAppPost() }
    }

    /**
     * Create a new tuition post (request or offer).
     * @param context The execution context.
     * @param description A brief description of the tuition.
     * @param subjects A list of subjects (e.g., ["Math", "English"]).
     * @param classType The class or grade level.
     * @return The ID of the created post.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun createPost(
        context: AppFunctionContext,
        description: String,
        subjects: List<String>,
        classType: String
    ): String = withContext(Dispatchers.IO) {
        val userResult = authRepository.getCurrentUser()
        val user = (userResult as? AppResult.Success)?.data
            ?: throw IllegalStateException("User not logged in")
            
        if (user.location.latitude == 0.0 && user.location.longitude == 0.0) {
            throw IllegalStateException("Please set your location in your profile before posting.")
        }

        if (user.contactInfo.none { it.isNotBlank() }) {
            throw IllegalStateException("Please add at least one contact info in your profile before posting.")
        }

        val postId = UUID.randomUUID().toString()
        val post = Post(
            id = postId,
            creatorId = user.id,
            creatorName = user.name,
            type = if (user.role == UserRole.STUDENT) PostType.TUITION_REQUEST else PostType.TUITION_OFFER,
            classType = classType,
            subjects = subjects,
            description = description,
            location = user.location,
            geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(user.location.latitude, user.location.longitude)),
            createdAt = System.currentTimeMillis()
        )
        
        when (val result = postRepository.createPost(post)) {
            is AppResult.Success -> postId
            is AppResult.Error -> throw IllegalStateException("Failed to create post: ${result.error}")
        }
    }

    /**
     * Get the status of a specific booking.
     * @param context The execution context.
     * @param bookingId The unique identifier of the booking.
     * @return The current status of the booking.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getBookingStatus(
        context: AppFunctionContext,
        bookingId: String
    ): String = withContext(Dispatchers.IO) {
        val userResult = authRepository.getCurrentUser()
        val user = (userResult as? AppResult.Success)?.data
            ?: throw IllegalStateException("User not logged in")
            
        val bookings = bookingRepository.getBookingsForUser(user.id, user.role).first()
        val booking = bookings.find { it.id == bookingId }
            ?: throw IllegalArgumentException("Booking not found")
            
        booking.status.name
    }

    /**
     * Accept a pending booking request.
     * @param context The execution context.
     * @param bookingId The ID of the booking to accept.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun acceptBooking(
        context: AppFunctionContext,
        bookingId: String
    ): Unit = withContext(Dispatchers.IO) {
        val booking = bookingRepository.getBookingById(bookingId).first()
            ?: throw IllegalArgumentException("Booking not found")

        val currentState = when (booking.status) {
            BookingStatus.PENDING -> BookingState.Pending
            BookingStatus.ACCEPTED -> BookingState.Accepted
            BookingStatus.REJECTED -> BookingState.Rejected
            BookingStatus.PREPARING -> BookingState.Preparing
            BookingStatus.ON_THE_WAY -> BookingState.OnTheWay
            BookingStatus.ARRIVED -> BookingState.Arrived
            BookingStatus.COMPLETED -> BookingState.Completed
            BookingStatus.CANCELLED -> BookingState.Cancelled
        }

        val engine = BookingStateEngine()
        val transitionResult = engine.transition(currentState, BookingEvent.Accept)
        if (transitionResult.isFailure) {
            throw IllegalStateException("Cannot accept booking from its current state: ${booking.status}")
        }

        val result = bookingRepository.updateBookingStatus(bookingId, BookingStatus.ACCEPTED)
        if (result is AppResult.Error) {
            throw IllegalStateException("Failed to accept booking: ${result.error}")
        }
    }

    /**
     * Like a tuition post to save it to your favorites.
     * @param context The execution context.
     * @param postId The ID of the post to like.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun likePost(
        context: AppFunctionContext,
        postId: String
    ): Unit = withContext(Dispatchers.IO) {
        val result = authRepository.toggleFavorite(postId)
        if (result is AppResult.Error) {
            throw IllegalStateException("Failed to like post: ${result.error}")
        }
    }
}

/**
 * A data class representing a tuition post for AppFunctions.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppPost(
    /** Unique ID of the post */
    val id: String,
    /** Name of the person who created the post */
    val creatorName: String,
    /** The subjects offered or requested */
    val subjects: List<String>,
    /** Description of the tuition */
    val description: String,
    /** The class or grade level */
    val classType: String,
    /** The rate or amount in BDT */
    val amount: Double
)

private fun Post.toAppPost() = AppPost(
    id = id,
    creatorName = creatorName,
    subjects = subjects,
    description = description,
    classType = classType,
    amount = amount
)
