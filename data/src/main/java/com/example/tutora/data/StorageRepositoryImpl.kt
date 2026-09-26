package com.example.tutora.data

import com.example.tutora.domain.AppError
import com.example.tutora.domain.AppResult
import com.example.tutora.domain.StorageRepository
import com.google.firebase.database.FirebaseDatabase
import java.util.Base64
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Stores profile pictures in the Firebase Realtime Database as base64 under
 * /users/{userId}, so we don't depend on Firebase Storage.
 *
 * The returned `data:` URL is also cached on the user's Firestore document so it
 * can be rendered without an extra read.
 */
class StorageRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : StorageRepository {

    private fun dataUrl(contentType: String, base64: String): String = "data:$contentType;base64,$base64"

    override suspend fun uploadProfileImage(userId: String, bytes: ByteArray, contentType: String): AppResult<String> {
        return try {
            val base64 = Base64.getEncoder().encodeToString(bytes)
            val ref = database.getReference("users/$userId")
            ref.setValue(mapOf("mimeType" to contentType, "image" to base64)).await()
            AppResult.Success(dataUrl(contentType, base64))
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Upload failed", e))
        }
    }
}
