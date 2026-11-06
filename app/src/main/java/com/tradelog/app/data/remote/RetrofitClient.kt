package com.tradelog.app.data.remote

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Singleton Retrofit client for all TradeLog API communication.
// Pattern reference: Square Inc., "Retrofit" documentation (2024).
// Architecture: single OkHttpClient with auth + logging interceptors.
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    const val BASE_URL = "https://tradelog-api-17ra.onrender.com/"

    // Kept for backwards-compatibility — interceptor now fetches the token dynamically
    @Suppress("unused")
    var authToken: String? = null

    // Auth interceptor: fetches Firebase ID token on the OkHttp background thread and attaches it
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val user = FirebaseAuth.getInstance().currentUser
        val token = if (user != null) {
            try {
                Tasks.await(user.getIdToken(false)).token
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get Firebase ID token: ${e.message}")
                null
            }
        } else null

        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w(TAG, "No auth token — sending unauthenticated request to ${original.url}")
            original
        }
        chain.proceed(request)
    }

    // HTTP logging interceptor for debugging API responses
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Log.d(TAG, "Initialising Retrofit — base URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
