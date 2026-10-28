package com.tradelog.app.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

// StorageRepository manages Firebase Cloud Storage for trade chart screenshots.
// Screenshots are stored at: gs://tradelog.appspot.com/users/{uid}/trades/{tradeId}/screenshot.jpg
// This satisfies the POE requirement: "Use blob storage."
// Reference: Firebase, "Firebase Cloud Storage for Android" (Google LLC, 2024)
class StorageRepository {

    private val TAG = "StorageRepository"

    /** Firebase Storage instance used for all blob upload and download operations. */
    private val storage = Firebase.storage

    /**
     * Upload a chart screenshot from a local [Uri] to Firebase Storage.
     * Returns the public download URL on success for display in the app.
     *
     * @param userId   The authenticated user's UID used to namespace the storage path.
     * @param tradeId  The trade ID; used to build the unique storage path.
     * @param imageUri Local content URI of the image to upload.
     * @return [Result.success] with the download URL string, or [Result.failure] on error.
     */
    suspend fun uploadTradeScreenshot(userId: String, tradeId: String, imageUri: Uri): Result<String> {
        return try {
            Log.d(TAG, "uploadTradeScreenshot() — userId=$userId tradeId=$tradeId")
            val ref = storage.reference
                .child("users/$userId/trades/$tradeId/screenshot.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.i(TAG, "Screenshot uploaded successfully — url=$downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "uploadTradeScreenshot failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a trade's screenshot from Firebase Storage.
     * Called when a trade is deleted so orphaned blobs do not accumulate.
     * Non-fatal: returns success even if the file does not exist.
     *
     * @param userId  The authenticated user's UID.
     * @param tradeId The trade whose screenshot should be removed.
     */
    suspend fun deleteTradeScreenshot(userId: String, tradeId: String): Result<Unit> {
        return try {
            Log.d(TAG, "deleteTradeScreenshot() — tradeId=$tradeId")
            storage.reference.child("users/$userId/trades/$tradeId/screenshot.jpg").delete().await()
            Log.i(TAG, "Screenshot deleted for trade $tradeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "deleteTradeScreenshot: ${e.message}") // Non-fatal if file doesn't exist
            Result.success(Unit) // Don't fail trade deletion if screenshot missing
        }
    }

    /**
     * Upload a profile avatar image for the user.
     * Overwrites any existing avatar because the path is deterministic per user.
     *
     * @param userId   The authenticated user's UID.
     * @param imageUri Local content URI of the avatar image to upload.
     * @return [Result.success] with the download URL string, or [Result.failure] on error.
     */
    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): Result<String> {
        return try {
            Log.d(TAG, "uploadProfilePhoto() — userId=$userId")
            val ref = storage.reference.child("users/$userId/avatar.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Log.i(TAG, "Profile photo uploaded — url=$url")
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "uploadProfilePhoto failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
