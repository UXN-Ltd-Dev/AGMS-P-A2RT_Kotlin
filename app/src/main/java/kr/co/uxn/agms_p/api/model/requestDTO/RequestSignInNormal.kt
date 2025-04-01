package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestSignInNormal(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val pwd: String
)
