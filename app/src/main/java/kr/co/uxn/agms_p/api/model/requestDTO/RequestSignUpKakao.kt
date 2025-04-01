package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestSignUpKakao(
    @SerializedName("user_identity")
    val userIdentity: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("sex")
    val sex: Int,

    @SerializedName("age")
    val age: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("weight")
    val weight: Int,

    @SerializedName("diabetes_type")
    val diabetesType: Int
)
