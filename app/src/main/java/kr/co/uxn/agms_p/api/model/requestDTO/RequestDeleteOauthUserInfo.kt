package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestDeleteOauthUserInfo(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("oauth_type")
    val oauthType: Int
)
