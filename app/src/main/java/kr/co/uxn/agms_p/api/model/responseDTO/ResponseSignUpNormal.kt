package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseSignUpNormal(
    @SerializedName("message")
    val message: String,
    @SerializedName("is_joined")
    val isJoined: Boolean
)

