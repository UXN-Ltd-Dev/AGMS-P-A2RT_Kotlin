package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestEmailVerificationCode(
    @SerializedName("email")
    val email: String
)
