package kr.co.uxn.agms_p

import android.annotation.SuppressLint
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kr.co.uxn.agms_p.api.model.requestDTO.RequestDataValue
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEventListData
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseDataValue
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseEventData
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseGetGlucose
import java.util.Objects


class PythonManager {
    private val mPythonModule: PyObject


    init {
        val mPython = Python.getInstance()
        mPythonModule = mPython.getModule("pyScript")
    }

    fun calculateGlucose(glucoseList: List<RequestDataValue>, eventList: List<RequestEventListData>): List<ResponseGetGlucose> {
        return try {
            val jsonString = mPythonModule.callAttr("getGlucoseList", glucoseList, eventList).toString()
            Log.d(TAG, "받은 jsonString: $jsonString")

            val gson = com.google.gson.Gson()
            val result = gson.fromJson(jsonString, Array<ResponseGetGlucose>::class.java).toList()
            Log.d(TAG, "result: $result")

            return result
        } catch (e: Exception) {
            Log.e(TAG, "calculationGlucose exception : ${e.message}")
            emptyList()
        }
    }

    companion object {
        private val TAG: String = PythonManager::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        private var mInstance: PythonManager? = null

        val instance: PythonManager
            get() {
                if (Objects.isNull(mInstance)) {
                    synchronized(PythonManager::class.java) {
                        if (Objects.isNull(mInstance)) {
                            mInstance = PythonManager()
                        }
                    }
                }
                return mInstance!!
            }
    }
}