package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseUserInfo(
    @SerializedName("is_success")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String
)
