package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestEmailCode(
    @SerializedName("email_code_id")
    val emailCodeId: Int,

    @SerializedName("email_code")
    val emailCode: String
)
