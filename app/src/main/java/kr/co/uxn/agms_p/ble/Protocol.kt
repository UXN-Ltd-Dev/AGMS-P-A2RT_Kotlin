package kr.co.uxn.agms_p.ble

import kr.co.uxn.agms_p.room.AppDatabase

interface Protocol {
    val serviceUUID: String
    val readUUID: String
    val writeUUID: String
    fun runProtocol(db: AppDatabase?, data: ByteArray, userId: Int): ByteArray?
}