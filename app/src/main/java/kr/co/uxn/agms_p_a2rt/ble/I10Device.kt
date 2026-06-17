package kr.co.uxn.agms_p_a2rt.ble

import android.icu.text.DecimalFormat
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p_a2rt.ble.BleManager.Companion.TEST
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.UserValue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class I10Device() : Protocol {
    override val serviceUUID: String = "e093f3b5-00a3-a9e5-9eca-80016e0edc24"
    override val readUUID: String = "e093f3b5-00a3-a9e5-9eca-80026e0edc24"
    override val writeUUID: String = "e093f3b5-00a3-a9e5-9eca-80036e0edc24"

    // INDEX HEADER
    val INDEX_STX_START = 0
    val INDEX_STX_END = 1
    val INDEX_CMD = 2
    val INDEX_STATUS = 3
    val INDEX_DATA_LENGTH = 4
    val INDEX_RESERVED = 5
    val INDEX_CRC_START = 6
    val INDEX_CRC_END = 7

    // BASE LENGTH
    val BASE_REQUEST_LENGTH = 8
    val PAYLOAD_DATA_LENGTH = BASE_REQUEST_LENGTH + 4

    // INDEX PAYLOAD UNIX
    val INDEX_UNIX_FIRST = BASE_REQUEST_LENGTH // 8
    val INDEX_UNIX_SECOND = INDEX_UNIX_FIRST + 1 // 9
    val INDEX_UNIX_THIRD = INDEX_UNIX_SECOND + 1 // 10
    val INDEX_UNIX_FOURTH = INDEX_UNIX_THIRD + 1 // 11

    // DC
    val BATTERY_START_SEQ = PAYLOAD_DATA_LENGTH // 12
    val BATTERY_END_SEQ = BATTERY_START_SEQ + 1 // 13
    val TMP_START_SEQ = BATTERY_END_SEQ + 1 // 14
    val TMP_END_SEQ = TMP_START_SEQ + 1 // 15
    val WE_START_SEQ = TMP_END_SEQ + 1 // 16
    val WE_MIDDLE_SEQ = WE_START_SEQ + 1 // 17
    val WE_END_SEQ = WE_MIDDLE_SEQ + 1 // 18
    val AE_START_SEQ = WE_END_SEQ + 1 // 19
    val AE_MIDDLE_SEQ = AE_START_SEQ + 1 // 20
    val AE_END_SEQ = AE_MIDDLE_SEQ + 1 // 21

    // IMPEDANCE
    val START_CODE_H = 0xA0.toByte()
    val START_CODE_L = 0x81.toByte()
    val CMD_RTC = 0x41.toByte()
    val CMD_SEND = 0x42.toByte()
    val CMD_IMPEDANCE = 0x45.toByte()
    val STATUS_RTC_FIRST = 0x11.toByte()
    val STATUS_RTC_TIME_ERROR = 0x12.toByte()
    val STATUS_DATA_LENGTH_ERROR = 0x02.toByte()
    val STATUS_NO_ERROR = 0x00.toByte()
    val STATUS_TIME_ERROR = 0x12.toByte()
    val STATUS_UNKNOWN = 0x05.toByte()

    val BASE_TIME: Long = 1767193200 // 26년 1월 1일 00시 00분 00초;

    override fun runProtocol(db: AppDatabase?, data: ByteArray, userId: Int): ByteArray? {
        val crc1 = java.lang.Byte.toUnsignedInt(data[INDEX_CRC_START])
        val crc2 = java.lang.Byte.toUnsignedInt(data[INDEX_CRC_END])
        Log.d(TEST, "crc1 : $crc1, crc2 : $crc2")

        val crc = (crc1 * 256) + crc2
        Log.d(TEST, "수신된 crc : $crc")

        val calCrc = checkCrc(data)
        Log.d(TEST, "계산된 crc : $calCrc")

        val cmdId = data[INDEX_CMD]
        val statusId = data[INDEX_STATUS]

        // CRC CHECK
        if (crc != calCrc) {
            Log.e(TEST, "=== crc 에러 발생! ===")
            return makeByteArray(cmdId, statusId)
        } else {
            Log.e(TEST, "=== crc 체크 통과! ===")
        }

        // Start Code CHECK
        if (data[INDEX_STX_START] == START_CODE_H && data[INDEX_STX_END] == START_CODE_L) {
            Log.e("TEST", "Product ID : ${String.format("0x%02X ", data[INDEX_STX_START])}")

            var response: ByteArray? = null
            when (cmdId) {
                CMD_RTC -> {
                    Log.d(TEST, "RTC REQUEST!")
                    if (statusId == STATUS_RTC_FIRST || statusId == STATUS_RTC_TIME_ERROR) {
                        response = sendRtc()
                    } else {
                        response = null
                    }
                }
                CMD_SEND -> {
                    val dataLength = (data[INDEX_DATA_LENGTH].toUByte().toInt() shl 8) or data[INDEX_RESERVED].toUByte().toInt()
                    val calDataLength = dataLength + BASE_REQUEST_LENGTH // header + payload

                    if (data.size != calDataLength) {
                        Log.e(TEST, "데이터 길이 맞지 않음\ndata.size = ${data.size}\ncalLength = ${calDataLength}")
                        response = makeByteArray(CMD_SEND, STATUS_DATA_LENGTH_ERROR)
                    } else {
                        // 본격 파싱 및 db에 넣는 작업은 여기서 작성해야함.
                        Log.e(TEST, "======DATA_LENGTH 일치!==========")
                        val success: Boolean = InsertDC(db, data, userId)
                        if (success) {
                            Log.e(TEST, "==============DC_NO_ERROR========================")
                            response = makeByteArray(STATUS_NO_ERROR, cmdId)
                        } else {
                            Log.e(TEST, "==============DC_TIME_ERROR========================")
                            response = makeByteArray(STATUS_TIME_ERROR, cmdId)
                        }
                    }
                }
                CMD_IMPEDANCE -> {
                    Log.d(TEST, "IMPEDANCE START!")
                    val success: Boolean = insertImpedance(db, data, userId)
                    if (success) {
                        response = makeByteArray(STATUS_NO_ERROR, cmdId)
                        Log.e(TEST, "==============IMPEDANCE_NO_ERROR========================")
                    } else {
                        response = makeByteArray(STATUS_UNKNOWN, cmdId)
                        Log.e(TEST, "==============IMPEDANCE_ERROR========================")
                    }

                }
            }
            return response
        } else {
            Log.e(TEST, "start code Error!")
            return null
        }
    }

    fun checkCrc(data: ByteArray): Int {
        val iData = IntArray(data.size)
        var temp = 0xFFFF
        var flag: Int

        for (i in iData.indices) {
            // 6   7
            if (i == 6 || i == 7) {
                iData[i] = java.lang.Byte.toUnsignedInt(0x00.toByte())
                continue
            }
            iData[i] = java.lang.Byte.toUnsignedInt(data[i])
        }

        for (iDatum in iData) {
            temp = temp xor iDatum
            for (j in 1..8) {
                flag = temp and 0x0001
                temp = temp shr 1
                if (flag == 1) temp = temp xor 0xA001
            }
        }

        val temp2 = temp shr 8
        temp = (temp shl 8) or temp2
        temp = temp and 0xFFFF

        return temp
    }

    private fun makeByteArray(status: Byte, cmd: Byte): ByteArray {
        val data = ByteArray(BASE_REQUEST_LENGTH)

        data[INDEX_STX_START] = 0xA0.toByte()
        data[INDEX_STX_END] = 0x81.toByte()
        data[INDEX_CMD] = cmd
        data[INDEX_STATUS] = status
        data[INDEX_DATA_LENGTH] = 0x00.toByte()
        data[INDEX_RESERVED] = 0x00.toByte()
        data[INDEX_CRC_START] = 0x00.toByte()
        data[INDEX_CRC_END] = 0x00.toByte()

        val crc: Int = checkCrc(data)

        data[INDEX_CRC_START] = (crc shr 8).toByte()
        data[INDEX_CRC_END] = crc.toByte()

        return data
    }

    private fun sendRtc(): ByteArray {
        Log.d("choco5732", "========Send RTC Running=============")
        val rtc = Calendar.getInstance()

        val year = rtc[Calendar.YEAR] - 2000
        val month = rtc[Calendar.MONTH] + 1
        val date = rtc[Calendar.DATE]

        val hour24 = rtc[Calendar.HOUR_OF_DAY]
        val minute = rtc[Calendar.MINUTE]
        val second = rtc[Calendar.SECOND]

        val data = ByteArray(14)

        data[0] = 0xA0.toByte() // stx_start
        data[1] = 0x81.toByte() // stx_end
        data[2] = 0x41.toByte() // cmd
        data[3] = 0x00.toByte() // state
        data[4] = 0x06.toByte() // length
        data[5] = 0x00.toByte() // reversed
        data[6] = 0x00.toByte() // crc_start
        data[7] = 0x00.toByte() // crc_end

        // time
        data[8] = year.toByte()
        data[9] = month.toByte()
        data[10] = date.toByte()
        data[11] = hour24.toByte()
        data[12] = minute.toByte()
        data[13] = second.toByte()

        Log.e(TEST, "now time: $year-$month-$date $hour24:$minute:$second")

        val crc: Int = checkCrc(data)

        data[6] = (crc shr 8).toByte()
        data[7] = crc.toByte()

        Log.e("choco5732", "rtc now time: $year-$month-$date $hour24:$minute:$second")

        return data
    }

    private fun InsertDC(db: AppDatabase?, data: ByteArray, userId: Int): Boolean {
        try {
            val step = 8 // tmp1_1, tmp1_2, weo1_1, weo1_2, weo1_3, weo2_1, weo2_2, weo2_3
            val pyaLoadSize = (data[INDEX_DATA_LENGTH].toUByte().toInt() shl 8) or (data[INDEX_RESERVED].toUByte().toInt())
            val nDataLength = (pyaLoadSize - 6) / step // 6의 의미 : unix 4byte + 배터리 2byte
            // (페이로드 - 앞에 짜잘한거(시간, 배터리) / 반복되는 값들 (온도 + 전류들)

            Log.d(TEST, "data의 size : ${data.size}")
            Log.d("data", "payload length : $pyaLoadSize")


            val chc1 = data[6]
            val chc2 = data[7]
            Log.d(TEST, "chc1 : $chc1, chc2 : $chc2")


            // LSB time
            val rawTime = (java.lang.Byte.toUnsignedLong(data[INDEX_UNIX_FIRST])) or
                    (java.lang.Byte.toUnsignedLong(data[INDEX_UNIX_SECOND]) shl 8) or
                    (java.lang.Byte.toUnsignedLong(data[INDEX_UNIX_THIRD]) shl 16) or
                    (java.lang.Byte.toUnsignedLong(data[INDEX_UNIX_FOURTH]) shl 24)

            Log.d(TEST, "rawTime : $rawTime" )

            val time =  (rawTime + BASE_TIME) * 1000 // sec -> milli

            // 2. 한국 타임존("Asia/Seoul")을 적용하여 로그로 확인
            val instant = Instant.ofEpochMilli(time)
            val koreaZoneId = ZoneId.of("Asia/Seoul")
            val kstTime = instant.atZone(koreaZoneId)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedString = kstTime.format(formatter)
            Log.e(TEST, "변환된 한국 시간 time: " + formattedString)

            var lastWeo1 = 0.0
            val startTime = time
            val lastTime = startTime + ((1000 * 10).toLong() * (nDataLength - 1))
            Log.d("time", "lastTime : $lastTime")

            val now = System.currentTimeMillis()
            // 타입 오차 허용값 = 10분
            val checkTime: Long = now + (60 * 1000 * 10)
            Log.d("time", "checkTime : $checkTime")

            // time error check
            if (lastTime > checkTime) {
                Log.e(TEST, "Time Check Error")
                return false
            }

            // 리스트 생성
            val saveData: MutableList<UserValue> = mutableListOf<UserValue>()

            // 배터리
            val vBattery = (data[BATTERY_START_SEQ].toUByte().toInt() shl 8) or (data[BATTERY_END_SEQ].toUByte().toInt())
            val decimalFormat = DecimalFormat("#.00")
            val battery = decimalFormat.format(vBattery)
            Log.d(TEST, "battery : $battery")

            // tmp, weo1, weo2
            Log.d(TEST, "nDataLength : $nDataLength")
            for (i in 0 until nDataLength) {
                val nTHigh: Int = 14 + i * step
                val nTLow: Int = 15 + i * step
//                Log.e(TEST, "TEMP : $nTHigh : $nTLow")
                val nWHigh: Int = 16 + i * step
                val nWMiddle: Int = 17 + i * step
                val nWLow: Int = 18 + i * step
//                Log.e(TEST, "WE Address : $nWHigh : $nWMiddle : $nWLow")
                val nAHigh: Int = 19 + i * step
                val nAMiddle: Int = 20 + i * step
                val nALow: Int = 21 + i * step
//                Log.e(TEST, "AE Address : " + nAHigh + " : " + nAMiddle + " : " + nALow)

                val dataTime: Long = startTime + (1000 * 10 * i)

                val vTemperature = (data[nTHigh].toUByte().toInt() shl 8) or (data[nTLow].toUByte().toInt())
                val temperature = vTemperature / 100.0

                val weCurrent: Double = convertToCurrentDataDouble(data[nWHigh], data[nWMiddle], data[nWLow])
                val aeCurrent: Double = convertToCurrentDataDouble(data[nAHigh], data[nAMiddle], data[nALow])

                Log.e(TEST, "TEMP " + i + ": " + temperature)
                Log.e(TEST, "WE_Current " + i + ": " + weCurrent)
                Log.e(TEST, "AE_Current " + i + ": " + aeCurrent)

                val convertedTime = Instant.ofEpochMilli(dataTime)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                saveData.add(
                    UserValue(
                        userId = userId,
                        weCurrent = weCurrent,
                        aeCurrent = aeCurrent,
                        temperature = temperature,
                        createdAt = convertedTime,
                        createdAtLong = dataTime
                    )
                )
                if (i == nDataLength - 1) {
                    lastWeo1 = weCurrent
                }
            }

            // ui에 weo1 값 실시간 갱신
//        BleBridge.updateWeo1(lastWeo1)

            CoroutineScope(Dispatchers.IO).launch {
                db?.dataDao()?.insertUserValue(saveData)
            }

            return true
        } catch(e: Exception) {
            Log.e(TEST, "DB Error: ${e.message}")
            return false
        }
    }

    private fun insertImpedance(db: AppDatabase?, data: ByteArray, userId: Int): Boolean {
        try {

        }
    }
    private fun convertToCurrentDataDouble(
        weoFirst: Byte,
        weoSecond: Byte,
        weoLast: Byte
    ): Double {
        // 부호 비트 추출
        val sign = if ((weoFirst.toInt() and 0x80) == 0) 1 else -1

        // 지수부 추출 (15비트만 추출)
        val exponent =
            (((java.lang.Byte.toUnsignedInt(weoFirst) and 0x7F) shl 8) or java.lang.Byte.toUnsignedInt(
                weoSecond
            ))

        // 가수부 추출
        val mantissa = java.lang.Byte.toUnsignedInt(weoLast)

        val changeMantissa = Math.round((mantissa / 100.0) * 100.0).toDouble() / 100.0

        // double 값 계산
        var result = sign * (exponent + changeMantissa)

        return result
    }


}