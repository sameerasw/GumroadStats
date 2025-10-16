package com.sameerasw.gumroadstats.data.api

import com.sameerasw.gumroadstats.data.model.PayoutsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GumroadApiService {
    @GET("v2/payouts")
    suspend fun getPayouts(
        @Query("access_token") accessToken: String,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("page_key") pageKey: String? = null,
        @Query("include_upcoming") includeUpcoming: String? = "true"
    ): PayoutsResponse
}

