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

    private val _chartTrigger = MutableStateFlow<Int>(0)
    val chartTrigger: StateFlow<Int> = _chartTrigger

    private val _showCaliDialog = MutableStateFlow<Boolean>(false)
    val showCaliDialog: StateFlow<Boolean> = _showCaliDialog

    private val _showBleConnectDialog = MutableStateFlow<Boolean>(false)
    val showBleConnectDialog: StateFlow<Boolean> = _showBleConnectDialog

    private val _showEndMeasurementDialog = MutableStateFlow<Boolean>(false)
    val showEndMeasurementDialog: StateFlow<Boolean> = _showEndMeasurementDialog

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

    fun showCaliDialog(value: Boolean) {
        _showCaliDialog.value = value
    }

    fun showBleConnectDialog(value: Boolean) {
        _showBleConnectDialog.value = value
    }

    fun showEndMeasurementDialog(value: Boolean) {
        _showEndMeasurementDialog.value = value
    }

    fun activateTrigger() {
        _chartTrigger.value = _chartTrigger.value + 1
        Log.d("CHART", "트리거 증가됨! 현재 값: ${_chartTrigger.value}")
    }
}