package kr.co.uxn.agms_p

import com.google.gson.annotations.SerializedName

data class PythonData(
    @SerializedName("bgTimestamps")
    var caliTimeStamp: List<Long>,
    @SerializedName("bgValues")
    val calValueList: List<Double>,
    @SerializedName("sensorTimestamps")
    var timeStampList: List<Long>,
    @SerializedName("calculatedGlucose")
    val glucoseList: List<Double>,
)
