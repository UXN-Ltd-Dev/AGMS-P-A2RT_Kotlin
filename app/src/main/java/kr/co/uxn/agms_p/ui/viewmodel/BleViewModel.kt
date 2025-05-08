package kr.co.uxn.agms_p.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kr.co.uxn.agms_p.AlwaysApplication
import kr.co.uxn.agms_p.BleConnectionState
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.ble.Device

class BleViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "BleViewModel"
    }

    private val _isFindDevice = MutableStateFlow(false)
    val isFindDevice = _isFindDevice.asStateFlow()

    private val _events = MutableSharedFlow<String>(replay = 0)
    val events = _events.asSharedFlow()

    private val _device = MutableStateFlow<Device>(Device())
    val device = _device.asStateFlow()

    private val _bleConnectStatusEvent = MutableSharedFlow<String>(replay = 0)
    val bleConnectStatusEvent = _events.asSharedFlow()

    val bleState: StateFlow<BleConnectionState> = BleBridge.bleState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BleConnectionState.DISCONNECTED
        )

    val weo1: StateFlow<Double> = BleBridge.weo1
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val temperature: StateFlow<Double> = BleBridge.temperature
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val glucose: StateFlow<Int> = BleBridge.glucose
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1
        )

    val chartTrigger: StateFlow<Int> = BleBridge.chartTrigger
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    var showCaliDialog: StateFlow<Boolean> = BleBridge.showCaliDialog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    var showBleConnectDialog: StateFlow<Boolean> = BleBridge.showBleConnectDialog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    var showEndMeasurementDialog: StateFlow<Boolean> = BleBridge.showEndMeasurementDialog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )



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


