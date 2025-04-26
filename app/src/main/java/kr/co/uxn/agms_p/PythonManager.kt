package kr.co.uxn.agms_p

import android.annotation.SuppressLint
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python

class PythonManager {

    private var mPythonModule: PyObject? = null
    init {
        val mPython = Python.getInstance()
        mPythonModule = mPython.getModule("pyScript3")
    }

    companion object {
        private const val TAG = "PythonManager"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var mInstance: PythonManager? = null

        fun getInstance(): PythonManager {
            return mInstance ?: synchronized(this) {
                mInstance ?: PythonManager().also { mInstance = it }
            }
        }
    }



    fun calculationGlucose(timeStamp: Long, w1: Double, w2: Double): Int {
        return try {
            val temp = mPythonModule?.callAttr("getGlucoseValue", timeStamp, w1, w2)?.toJava(Double::class.java)
            if(temp != null) {
                return Math.round(temp).toInt()
            }else {
                -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "calculationGlucose exception: ${e.message}")
            0
        }
    }

    fun setOnePointCalibration(timeStamp: Long, calValue: Int): Int {
        return try {
            val tmp = mPythonModule?.callAttr("setOnePointCalibration", timeStamp, calValue)!!.toJava(Int::class.java)
            Log.e(TAG, "setOnePointCalibration result: $tmp")
            tmp
        } catch (e: Exception) {
            Log.e(TAG, "setOnePointCalibration exception: ${e.message}")
            0
        }
    }

}