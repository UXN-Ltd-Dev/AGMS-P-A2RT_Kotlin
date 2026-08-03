package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestSignUpOauthDetail(
//    @SerializedName("user_id")
//    val userId: Int,
    @SerializedName("social_type")
    val socialType: Int,

    @SerializedName("device_type_num")
    val deviceType: Int,

    @SerializedName("email")
    val email: String,

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
