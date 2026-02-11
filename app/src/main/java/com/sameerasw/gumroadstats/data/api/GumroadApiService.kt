package com.sameerasw.gumroadstats.data.api

import com.sameerasw.gumroadstats.data.model.PayoutDetailsResponse
import com.sameerasw.gumroadstats.data.model.PayoutsResponse
import com.sameerasw.gumroadstats.data.model.UserResponse
import com.sameerasw.gumroadstats.data.model.SalesResponse
import com.sameerasw.gumroadstats.data.model.SaleDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("v2/payouts/{id}")
    suspend fun getPayoutDetails(
        @Path("id") payoutId: String,
        @Query("access_token") accessToken: String
    ): PayoutDetailsResponse

    @GET("v2/user")
    suspend fun getUser(
        @Query("access_token") accessToken: String
    ): UserResponse

    @GET("v2/sales")
    suspend fun getSales(
        @Query("access_token") accessToken: String,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("product_id") productId: String? = null,
        @Query("email") email: String? = null,
        @Query("order_id") orderId: String? = null,
        @Query("page_key") pageKey: String? = null
    ): SalesResponse

    @GET("v2/sales/{id}")
    suspend fun getSaleDetails(
        @Path("id") saleId: String,
        @Query("access_token") accessToken: String
    ): SaleDetailsResponse
}
