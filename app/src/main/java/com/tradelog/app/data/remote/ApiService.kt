package com.tradelog.app.data.remote

import retrofit2.Response
import retrofit2.http.*

// Retrofit interface for all TradeLog REST API endpoints.
// Endpoint specification defined in Part 1 Planning Document Section 5.2.
// All functions are suspend functions — called from coroutines (JetBrains, 2024).
interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(): Response<UserResponse>

    // ── Trades ────────────────────────────────────────────────────────────────
    @GET("trades")
    suspend fun getTrades(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("tag") tag: String? = null,
        @Query("session") session: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<TradesListResponse>

    @POST("trades")
    suspend fun createTrade(@Body body: TradeRequest): Response<TradeResponse>

    @GET("trades/{id}")
    suspend fun getTrade(@Path("id") id: String): Response<TradeResponse>

    @PUT("trades/{id}")
    suspend fun updateTrade(
        @Path("id") id: String,
        @Body body: TradeRequest
    ): Response<TradeResponse>

    @DELETE("trades/{id}")
    suspend fun deleteTrade(@Path("id") id: String): Response<DeleteResponse>

    // ── Analytics ─────────────────────────────────────────────────────────────
    @GET("analytics/summary")
    suspend fun getAnalyticsSummary(
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<AnalyticsSummary>

    @GET("analytics/equity-curve")
    suspend fun getEquityCurve(
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<EquityCurveResponse>

    @GET("analytics/by-session")
    suspend fun getStatsBySession(): Response<SessionStats>

    @GET("analytics/by-tag")
    suspend fun getStatsByTag(): Response<TagStats>

    // ── Tags ──────────────────────────────────────────────────────────────────
    @GET("tags")
    suspend fun getTags(): Response<TagsListResponse>

    @POST("tags")
    suspend fun createTag(@Body body: TagRequest): Response<TagResponse>

    @DELETE("tags/{id}")
    suspend fun deleteTag(@Path("id") id: String): Response<DeleteResponse>

    // ── Settings ──────────────────────────────────────────────────────────────
    @GET("settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("settings")
    suspend fun updateSettings(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<SettingsResponse>
}
