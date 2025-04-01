package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestGlucose(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("glucose")
    val glucose: Int

)
