package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseSignInNormal(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("uuid")
    val uuid: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("is_joined")
    val isJoined: Boolean
)
