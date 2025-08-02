package com.cataloghub.android.ui.live

import javax.inject.Inject

class LiveSessionRepository @Inject constructor(
    private val liveSessionApi: LiveSessionApi
) {
    suspend fun createLiveSession(request: CreateSessionRequest): CreateSessionResponse {
        return liveSessionApi.createLiveSession(request)
    }
}
