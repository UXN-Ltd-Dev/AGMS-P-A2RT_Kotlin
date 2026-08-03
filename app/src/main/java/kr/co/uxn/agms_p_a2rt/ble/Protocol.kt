package kr.co.uxn.agms_p_a2rt.ble

import kr.co.uxn.agms_p_a2rt.room.AppDatabase

interface Protocol {
    val serviceUUID: String
    val readUUID: String
    val writeUUID: String
    fun runProtocol(db: AppDatabase?, data: ByteArray, userId: Int, userDeviceId: Int): ByteArray?
}