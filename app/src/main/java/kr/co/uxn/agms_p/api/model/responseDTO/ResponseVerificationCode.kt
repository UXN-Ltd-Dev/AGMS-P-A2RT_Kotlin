package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseVerificationCode(
    @SerializedName("is_duplicated")
    val isDuplicated: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("email_code_id")
    val emailCodeId: Int
)
