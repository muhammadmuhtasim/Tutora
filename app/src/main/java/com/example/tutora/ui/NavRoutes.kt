package com.example.tutora.ui

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Splash : NavRoute
    @Serializable
    data object Login : NavRoute
    @Serializable
    data object Registration : NavRoute
    @Serializable
    data object ForgotEmail : NavRoute
    @Serializable
    data object ForgotPassword : NavRoute
    @Serializable
    data object Explorer : NavRoute
    @Serializable
    data class CreatePost(val postId: String? = null) : NavRoute // null for create, ID for edit
    @Serializable
    data object Profile : NavRoute // Current user's profile view
    @Serializable
    data object ProfileEdit : NavRoute
    @Serializable
    data object Settings : NavRoute
    @Serializable
    data object Favorites : NavRoute
    @Serializable
    data class PublicProfile(val userId: String) : NavRoute // View another user's profile
    @Serializable
    data object Bookings : NavRoute
    @Serializable
    data class BookingFlow(val postId: String) : NavRoute
    @Serializable
    data class Chat(val sessionId: String, val otherPartyName: String) : NavRoute
    @Serializable
    data object Messages : NavRoute
}
