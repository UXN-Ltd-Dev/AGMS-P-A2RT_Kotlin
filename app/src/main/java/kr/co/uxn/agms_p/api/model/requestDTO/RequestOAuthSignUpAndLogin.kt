package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestOAuthSignUpAndLogin(
    @SerializedName("auth_code")
    val authCode: String,

    @SerializedName("login_type")
    val loginType: Int
)
