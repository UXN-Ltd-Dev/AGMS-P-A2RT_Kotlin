package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestSignUpNormal(
    @SerializedName("email")
    val email: String,

    @SerializedName("pwd")
    val pwd: String,

    @SerializedName("sex")
    val sex: String,

    @SerializedName("age")
    val age: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("weight")
    val weight: Int,

    @SerializedName("diabetes_type")
    val diabetesType: String
)
