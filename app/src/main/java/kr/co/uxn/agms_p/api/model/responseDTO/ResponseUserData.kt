package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class ResponseUserData(
    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("sex")
    val sex: String,

    @SerializedName("age")
    val age: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("weight")
    val weight: Int,

    @SerializedName("diabetes_type")
    val diabetesType: String,

    @SerializedName("is_success")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String
)
