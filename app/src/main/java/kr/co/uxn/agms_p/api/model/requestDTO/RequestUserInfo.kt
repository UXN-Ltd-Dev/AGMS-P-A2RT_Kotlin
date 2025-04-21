package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestUserInfo(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("password")
    val pwd: String
)
