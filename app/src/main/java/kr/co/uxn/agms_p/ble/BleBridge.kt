package kr.co.uxn.agms_p.ble

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.co.uxn.agms_p.BleConnectionState

object BleBridge {
    private val _bleState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val bleState: StateFlow<BleConnectionState> = _bleState

    fun updateState(state: BleConnectionState) {
        _bleState.value = state
        Log.e("TEST", "ble브릿지의 bleState : $state")
    }
}