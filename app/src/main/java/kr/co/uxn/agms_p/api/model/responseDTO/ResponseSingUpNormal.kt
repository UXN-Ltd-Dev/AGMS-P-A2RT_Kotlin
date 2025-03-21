package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseSingUpNormal(
    @SerializedName("email")
    val email: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("uuid")
    val uuid: String
)
