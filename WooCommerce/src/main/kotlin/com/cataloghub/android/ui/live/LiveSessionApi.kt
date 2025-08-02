package com.cataloghub.android.ui.live

import retrofit2.http.Body
import retrofit2.http.POST

interface LiveSessionApi {
    @POST("/api/v1/live-sessions/")
    suspend fun createLiveSession(@Body request: CreateSessionRequest): CreateSessionResponse
}
