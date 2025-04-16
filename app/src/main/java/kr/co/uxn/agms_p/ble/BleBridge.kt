package kr.co.uxn.agms_p.ble

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.co.uxn.agms_p.BleConnectionState

object BleBridge {
    private val _bleState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val bleState: StateFlow<BleConnectionState> = _bleState

    private val _weo1 = MutableStateFlow<Double>(0.0)
    val weo1: StateFlow<Double> = _weo1

    private val _temperature = MutableStateFlow<Double>(0.0)
    val temperature: StateFlow<Double> = _temperature

    private val _glucose = MutableStateFlow<Int>(0)
    val glucose: StateFlow<Int> = _glucose

    fun updateState(state: BleConnectionState) {
        _bleState.value = state
        Log.e("TEST", "ble브릿지의 bleState : $state")
    }

    fun updateWeo1(value: Double) {
        _weo1.value = value
    }

    fun updateTemperature(value: Double) {
        _temperature.value = value
    }

    fun updateGlucose(value: Int) {
        _glucose.value = value
    }
}