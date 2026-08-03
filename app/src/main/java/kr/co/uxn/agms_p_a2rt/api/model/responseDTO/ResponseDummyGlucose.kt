package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseDummyGlucose(
    @SerializedName("glucose")
    val glucose: List<Int>,

    @SerializedName("time")
    val time: List<String>
)
