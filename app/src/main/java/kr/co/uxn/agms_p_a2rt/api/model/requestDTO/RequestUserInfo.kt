package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestUserInfo(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val pwd: String,

    @SerializedName("device")
    val device: Int = 1101
)
