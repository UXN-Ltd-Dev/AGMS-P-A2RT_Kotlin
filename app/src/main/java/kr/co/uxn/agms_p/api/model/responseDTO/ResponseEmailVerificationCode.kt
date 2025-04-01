package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseEmailVerificationCode(
    @SerializedName("isDuplicated")
    val isDuplicated: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("authentication_code")
    val authenticationCode: Int
)
