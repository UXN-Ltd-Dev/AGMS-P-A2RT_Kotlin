package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestDeleteUserInfo(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("device")
    val device: Int = 1101
)
