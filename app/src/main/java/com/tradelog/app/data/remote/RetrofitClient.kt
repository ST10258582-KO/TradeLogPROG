package com.tradelog.app.data.remote

import android.util.Log
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

    // TODO: Replace BASE_URL with deployed Azure App Service endpoint after deployment.
    // e.g. "https://tradelog-api.azurewebsites.net/"
    const val BASE_URL = "http://10.0.2.2:8080/"

    // Auth token — set after Firebase sign-in (FR-02). Injected into every request header.
    var authToken: String? = null

    // Auth interceptor: attaches Bearer token to all outgoing requests
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = authToken
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
