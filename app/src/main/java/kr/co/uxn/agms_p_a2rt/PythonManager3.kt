package kr.co.uxn.agms_p_a2rt

import android.annotation.SuppressLint
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.google.gson.Gson
import kr.co.uxn.agms_p_a2rt.room.UserCalibration
import kr.co.uxn.agms_p_a2rt.room.UserValue
import java.util.Objects

class PythonManager3 {
    private val mPythonModule: PyObject

    init {
        val mPython = Python.getInstance()
        mPythonModule = mPython.getModule("20260609_manualcal_1kalman_bcslope_table_2pcal_kal_after_tempe_calW2_100000_2")
    }

    fun calculateGlucose(glucoseList: List<UserValue>, eventList: List<UserCalibration>): PythonData? {
        return try {
            val w1List: List<Double> = glucoseList.map { it.weCurrent }

            val w2List: List<Double> = glucoseList.map { it.aeCurrent }

            val tempList: List<Double> = glucoseList.map { it.temperature }

            val timeStampList: MutableList<Long> = glucoseList.map {
                it.createdAtLong / 1000 // 초단위로 만들어준다.
            }.toMutableList()

            val baseTime = timeStampList[0]
            for (i in timeStampList.indices) {
                timeStampList[i] -= baseTime
            }

            val calTimeStampList: MutableList<Long> = eventList.map {
                it.createdAtLong / 1000
            }.toMutableList()

            for (i in calTimeStampList.indices) {
                calTimeStampList[i] -= baseTime
            }

            val calList: List<Double> = eventList.map { it.glucoseValue }


            val parameterData = ParameterData(
                times = listOf(24.0f, 48.0f, 72.0f, 96.0f, 120.0f, 144.0f, 168.0f, 192.0f, 216.0f, 240.0f, 264.0f, 288.0f, 312.0f, 336.0f, 360.0f, 384.0f, 408.0f, 432.0f, 456.0f, 480.0f),
                sensitivities = listOf(0.5f, 0.4f, 0.46f, 0.45f, 0.35f, 0.35f, 0.35f, 0.33f, 0.3f, 0.3f, 0.23f, 0.19f, 0.17f, 0.17f, 0.17f, 0.17f, 0.17f, 0.17f, 0.17f, 0.17f),
                baseCurrents = listOf(2.938f, 4.503f, 4.219f, 3.581f, 3.28f, 3.046f, 2.604f, 2.382f, 2.471f, 1.829f, 2.634f, 1.925f, 1.673f, 1.673f, 1.673f, 1.673f, 1.673f, 1.673f, 1.673f, 1.673f),
                slopes = listOf(-2.509f, -0.436f, 0.885f, 1.386f, 0.919f, 0.562f, 0.6f, 0.524f, 0.619f, 0.436f, 0.903f, 0.418f, 0.621f, 0.621f, 0.621f, 0.621f, 0.621f, 0.621f, 0.621f, 0.621f),
                alphas = listOf(0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f)
            )

            val jsonString = mPythonModule.callAttr("custom_function", timeStampList, w1List, w2List, tempList, parameterData.alphas, calTimeStampList, calList, parameterData.baseCurrents, parameterData.sensitivities, parameterData.slopes, parameterData.times).toString()
            Log.d(TAG, "받은 jsonString: $jsonString")

            val gson = Gson()
            val pythonData = gson.fromJson(jsonString, PythonData::class.java)
            Log.d(TAG, "result: $pythonData")

            pythonData.timeStampList = pythonData.timeStampList.map { time ->
                (time + baseTime) * 1000
            }

            pythonData.caliTimeStamp = pythonData.caliTimeStamp.map { time ->
                (time + baseTime) * 1000
            }

            return pythonData
        } catch (e: Exception) {
            Log.e(TAG, "calculationGlucose exception : ${e.message}")
            return null
        }
    }

    companion object {
        private val TAG: String = PythonManager3::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        private var mInstance: PythonManager3? = null

        val instance: PythonManager3
            get() {
                if (Objects.isNull(mInstance)) {
                    synchronized(PythonManager3::class.java) {
                        if (Objects.isNull(mInstance)) {
                            mInstance = PythonManager3()
                        }
                    }
                }
                return mInstance!!
            }
    }
}
