package com.sameerasw.gumroadstats.data.repository

import com.google.gson.Gson
import com.sameerasw.gumroadstats.data.api.RetrofitClient
import com.sameerasw.gumroadstats.data.model.ErrorResponse
import com.sameerasw.gumroadstats.data.model.Payout
import com.sameerasw.gumroadstats.data.model.PayoutsResponse
import com.sameerasw.gumroadstats.data.model.User
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GumroadRepository {
    private val apiService = RetrofitClient.apiService
    private val gson = Gson()

    private fun parseErrorMessage(exception: Exception): String {
        return when (exception) {
            is HttpException -> {
                try {
                    val errorBody = exception.response()?.errorBody()?.string()
                    if (errorBody != null) {
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: getDefaultErrorMessage(exception.code())
                    } else {
                        getDefaultErrorMessage(exception.code())
                    }
                } catch (e: Exception) {
                    getDefaultErrorMessage(exception.code())
                }
            }
            is SocketTimeoutException -> "Connection timed out. Please check your internet connection."
            is UnknownHostException -> "No internet connection. Please check your network."
            else -> exception.message ?: "An unknown error occurred"
        }
    }

    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Bad request. Please check your input."
            401 -> "Invalid access token. Please check your credentials."
            402 -> "Request failed. Please try again."
            404 -> "The requested resource was not found."
            in 500..504 -> "Server error. Please try again later."
            else -> "Request failed with code $code"
        }
    }

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
            Result.failure(Exception(parseErrorMessage(e)))
        }
    }

    suspend fun getPayoutDetails(
        payoutId: String,
        accessToken: String
    ): Result<Payout> {
        return try {
            val response = apiService.getPayoutDetails(payoutId, accessToken)
            Result.success(response.payout)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e)))
        }
    }

    suspend fun getUser(
        accessToken: String
    ): Result<User> {
        return try {
            val response = apiService.getUser(accessToken)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(Exception(parseErrorMessage(e)))
        }
    }
}
