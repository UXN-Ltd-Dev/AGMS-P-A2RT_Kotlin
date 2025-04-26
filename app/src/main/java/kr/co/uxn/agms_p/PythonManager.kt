package kr.co.uxn.agms_p

import android.annotation.SuppressLint
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import java.util.Objects


class PythonManager {
    private val mPythonModule: PyObject


    init {
        val mPython = Python.getInstance()
        mPythonModule = mPython.getModule("pyScript3")
    }

    fun calculationGlucose(timeStamp: Long, W1: Double, W2: Double): Int {
        var glucose = 0

        try {
            val temp = mPythonModule.callAttr("getGlucoseValue", timeStamp, W1, W2).toJava(
                Double::class.java
            )
            glucose = Math.round(temp).toInt()
        } catch (e: Exception) {
            Log.e(TAG, "calculationGlucose exception : " + e.message)
        }

        return glucose
    }

    fun setOnepointCalibration(timeStamp: Long, calValue: Int): Int {
        var tmp = 0
        try {
            tmp = mPythonModule.callAttr("setOnepointCalibration", timeStamp, calValue).toJava(
                Int::class.java
            )


            Log.e(TAG, "setOnepointCalibration result: $tmp")
        } catch (e: Exception) {
            Log.e(TAG, "setOnepointCalibration exception : " + e.message)
        }

        return tmp
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