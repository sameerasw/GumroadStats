package com.sameerasw.gumroadstats.data.repository

import com.sameerasw.gumroadstats.data.api.RetrofitClient
import com.sameerasw.gumroadstats.data.model.PayoutsResponse

class GumroadRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getPayouts(
        accessToken: String,
        after: String? = null,
        before: String? = null,
        pageKey: String? = null,
        includeUpcoming: String? = "true"
    ): Result<PayoutsResponse> {
        return try {
            val response = apiService.getPayouts(accessToken, after, before, pageKey, includeUpcoming)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

