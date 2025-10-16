package com.sameerasw.gumroadstats.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val success: Boolean,
    val user: User
)

data class User(
    val bio: String?,
    val name: String,
    @SerializedName("twitter_handle")
    val twitterHandle: String?,
    @SerializedName("user_id")
    val userId: String,
    val email: String?,
    val url: String?
)

