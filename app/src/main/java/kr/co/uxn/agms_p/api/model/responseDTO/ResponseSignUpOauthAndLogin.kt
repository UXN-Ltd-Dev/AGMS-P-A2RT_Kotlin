package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseSignUpOauthAndLogin(
    @SerializedName("is_sign_up")
    val isSignUp: Boolean,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)

