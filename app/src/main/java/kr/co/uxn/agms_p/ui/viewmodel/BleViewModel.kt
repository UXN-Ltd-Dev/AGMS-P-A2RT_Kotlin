package kr.co.uxn.agms_p.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class BleViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "BleViewModel"
    }

    private val _isFindDevice = MutableStateFlow(false)
    val isFindDevice = _isFindDevice.asStateFlow()

    private val _events = MutableSharedFlow<String>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun emit(event: String) {
        _events.emit(event)
    }

    fun updateIsFindDevice(value: Boolean) {
        _isFindDevice.value = value
        Log.e(TAG, "_isFindDevice 값 : ${_isFindDevice.value}")
    }
}


