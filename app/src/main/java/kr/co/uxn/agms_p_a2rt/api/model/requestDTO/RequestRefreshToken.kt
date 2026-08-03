package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestRefreshToken(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("refresh_token")
    val refreshToken: String
)
