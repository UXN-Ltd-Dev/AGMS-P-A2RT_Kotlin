package kr.co.uxn.agms_p.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.uxn.agms_p.AlwaysApplication
import kr.co.uxn.agms_p.ble.Device

class BleViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "BleViewModel"
    }

    private val respository = (application as AlwaysApplication).bleRepository

    private val _isFindDevice = MutableStateFlow(false)
    val isFindDevice = _isFindDevice.asStateFlow()

    private val _events = MutableSharedFlow<String>(replay = 0)
    val events = _events.asSharedFlow()

    private val _device = MutableStateFlow<Device>(Device())
    val device = _device.asStateFlow()

    suspend fun emit(event: String) {
        _events.emit(event)
    }

    fun updateIsFindDevice(value: Boolean) {
        _isFindDevice.value = value
        Log.e(TAG, "_isFindDevice 값 : ${_isFindDevice.value}")
    }

    fun insertDevice(device: Device) {
        _device.value = device
        Log.e(TAG, "뷰모델에 저장된 device 값 : ${_device.value}")
    }
}


