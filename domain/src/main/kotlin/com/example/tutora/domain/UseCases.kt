package com.example.tutora.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(
        center: GeoPoint,
        radiusKm: Double,
        type: PostType? = null
    ): Flow<List<Post>> {
        return postRepository.getPosts(center, radiusKm, type)
    }
}

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(post: Post): AppResult<Unit> {
        return postRepository.createPost(post)
    }
}

class ManageBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend fun create(booking: Booking): AppResult<Unit> {
        return bookingRepository.createBooking(booking)
    }

    suspend fun updateStatus(bookingId: String, status: BookingStatus): AppResult<Unit> {
        return bookingRepository.updateBookingStatus(bookingId, status)
    }
}
