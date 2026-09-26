package com.example.tutora.data

import com.example.tutora.domain.AppError
import com.example.tutora.domain.AppResult
import com.example.tutora.domain.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadProfileImage(userId: String, bytes: ByteArray, contentType: String): AppResult<String> {
        return try {
            // Store each user's avatar under their own path so a fresh upload replaces the old file.
            val ref = storage.getReference("profile_images/$userId/avatar")
            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .build()
            ref.putBytes(bytes, metadata).await()
            val url = ref.getDownloadUrl().await().toString()
            AppResult.Success(url)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e.message ?: "Upload failed", e))
        }
    }
}
