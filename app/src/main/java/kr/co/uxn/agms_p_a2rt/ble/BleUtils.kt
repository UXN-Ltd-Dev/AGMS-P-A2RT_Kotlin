package kr.co.uxn.agms_p_a2rt.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager

object BleUtils {

    const val STATUS_BLE_ENABLED = 0
    const val STATUS_BLUETOOTH_NOT_AVAILABLE = 1
    const val STATUS_BLE_NOT_AVAILABLE = 2
    const val STATUS_BLUETOOTH_DISABLED = 3
    val TAG = BleUtils::class.java.simpleName

    fun getBleStatus(context: Context): Int {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return STATUS_BLE_NOT_AVAILABLE
        }

        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            return STATUS_BLUETOOTH_NOT_AVAILABLE
        }

        if (!bluetoothAdapter.isEnabled) {
            return STATUS_BLUETOOTH_DISABLED
        }

        return STATUS_BLE_ENABLED
    }
}