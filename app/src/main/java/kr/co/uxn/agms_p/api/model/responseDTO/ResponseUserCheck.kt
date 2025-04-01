package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseUserCheck(
    @SerializedName("user_identity")
    val userIdentity: String,

    @SerializedName("is_user_exist")
    val isUserExist: Boolean
)
