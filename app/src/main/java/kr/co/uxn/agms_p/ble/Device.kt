package kr.co.uxn.agms_p.ble

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val deviceMac : String? = null,
    val device : BluetoothDevice? = null
) : Parcelable