package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestEmailVerificationCode(
    @SerializedName("email")
    val email: String
)
