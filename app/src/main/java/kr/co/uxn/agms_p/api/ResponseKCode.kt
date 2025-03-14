package kr.co.uxn.agms_p.api

import com.google.gson.annotations.SerializedName

data class ResponseKCode(
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @SerializedName("name")
    val name: String? = null
)
