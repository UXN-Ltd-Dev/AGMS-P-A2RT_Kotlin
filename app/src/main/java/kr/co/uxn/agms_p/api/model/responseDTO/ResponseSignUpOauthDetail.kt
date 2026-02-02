package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseSignUpOauthDetail(
    @SerializedName("is_success")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("email")
    val email: String
)
