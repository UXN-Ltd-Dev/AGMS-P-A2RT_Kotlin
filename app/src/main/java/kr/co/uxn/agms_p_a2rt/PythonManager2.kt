package kr.co.uxn.agms_p_a2rt

import android.annotation.SuppressLint
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.google.gson.Gson
import kr.co.uxn.agms_p_a2rt.room.UserCalibration
import kr.co.uxn.agms_p_a2rt.room.UserValue
import java.util.Objects

class PythonManager2 {
    private val mPythonModule: PyObject

    init {
        val mPython = Python.getInstance()
        mPythonModule = mPython.getModule("20260609_manualcal_1kalman_bcslope_table_2pcal_kal_after_tempe_calW2")
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
                times = listOf(0.0f, 24.0f, 48.0f, 72.0f, 96.0f, 120.0f, 144.0f, 168.0f, 192.0f, 216.0f, 240.0f, 264.0f, 288.0f, 312.0f, 336.0f, 360.0f),
                sensitivities = listOf(0.99f, 0.99f, 1.12f, 0.790f, 0.800f, 0.57f, 0.56f, 0.44f, 0.44f, 0.44f, 0.44f, 0.44f, 0.44f, 0.44f, 0.44f, 0.44f),
                baseCurrents = listOf(4.345f, 4.345f, 0.975f, 1.298f, 0.700f, 1.100f, 1.152f, 1.375f, 1.375f, 1.375f, 1.375f, 1.375f, 1.375f, 1.375f, 1.375f, 1.375f),
                slopes = listOf(5.096f, 5.096f, 1.489f, 0.600f, 0.560f, 0.368f, 0.228f, 0.168f, 0.168f, 0.168f, 0.168f, 0.168f, 0.168f, 0.168f, 0.168f, 0.168f),
                alphas = listOf(0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f, 0.037f)
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
        private val TAG: String = PythonManager2::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        private var mInstance: PythonManager2? = null

        val instance: PythonManager2
            get() {
                if (Objects.isNull(mInstance)) {
                    synchronized(PythonManager2::class.java) {
                        if (Objects.isNull(mInstance)) {
                            mInstance = PythonManager2()
                        }
                    }
                }
                return mInstance!!
            }
    }
}
