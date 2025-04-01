package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseGoogleIdToken(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("result_code")
    val resultCode: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("is_signup")
    val isSignup: Boolean
)
