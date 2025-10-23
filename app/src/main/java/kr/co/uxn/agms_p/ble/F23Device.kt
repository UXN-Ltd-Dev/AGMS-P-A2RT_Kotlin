package kr.co.uxn.agms_p.ble

import android.icu.text.DecimalFormat
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.ble.BleManager.Companion.TEST
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.room.UserValue
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class F23Device() : Protocol {
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
    val PAYLOAD_DATA_LENGTH = BASE_REQUEST_LENGTH + 6

    // INDEX PAYLOAD DATE
    val INDEX_YEAR = BASE_REQUEST_LENGTH // 8
    val INDEX_MONTH = INDEX_YEAR + 1 // 9
    val INDEX_DAY = INDEX_MONTH + 1 // 10
    val INDEX_HOUR = INDEX_DAY + 1 // 11
    val INDEX_MINUTE = INDEX_HOUR + 1 // 12
    val INDEX_SECOND = INDEX_MINUTE + 1 // 13

    // INDEX PAYLOAD
    val BATTERY_START_SEQ = PAYLOAD_DATA_LENGTH // 14
    val BATTERY_END_SEQ = BATTERY_START_SEQ + 1
    val TMP_START_SEQ = BATTERY_END_SEQ + 1
    val TMP_END_SEQ = TMP_START_SEQ + 1
    val WE_START_SEQ = TMP_END_SEQ + 1
    val WE_MIDDLE_SEQ = WE_START_SEQ + 1
    val WE_END_SEQ = WE_MIDDLE_SEQ + 1
    val AE_START_SEQ = WE_END_SEQ + 1
    val AE_MIDDLE_SEQ = AE_START_SEQ + 1
    val AE_END_SEQ = AE_MIDDLE_SEQ + 1

    // 기타
    val START_CODE_H = 0xA0.toByte()
    val START_CODE_L = 0x81.toByte()
    val CMD_RTC = 0x41.toByte()
    val CMD_SEND = 0x42.toByte()
    val STATUS_RTC_FIRST = 0x11.toByte()
    val STATUS_RTC_TIME_ERROR = 0x12.toByte()
    val STATUS_DATA_LENGTH_ERROR = 0x02.toByte()
    val STATUS_NO_ERROR = 0x00.toByte()
    val STATUS_TIME_ERROR = 0x12.toByte()

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
                        val success: Boolean = parsingDataAndInsertDB(db, data, userId)
                        if (success) {
                            Log.e(TEST, "==============STATUS_NO_ERROR========================")
                            response = makeByteArray(STATUS_NO_ERROR, cmdId)
                        } else {
                            Log.e(TEST, "==============STATUS_TIME_ERROR========================")
                            response = makeByteArray(STATUS_TIME_ERROR, cmdId)
                        }
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

    private fun parsingDataAndInsertDB(db: AppDatabase?, data: ByteArray, userId: Int): Boolean {
        try {
            val step = 8 // tmp1_1, tmp1_2, weo1_1, weo1_2, weo1_3, weo2_1, weo2_2, weo2_3
            val pyaLoadSize = (data[INDEX_DATA_LENGTH].toUByte().toInt() shl 8) or (data[INDEX_RESERVED].toUByte().toInt())
            val nDataLength = (pyaLoadSize - 8) / step
            // (페이로드 - 앞에 짜잘한거(시간, 배터리) / 반복되는 값들 (온도 + 전류들)

            Log.d(TEST, "data의 size : ${data.size}")
            Log.d("data", "payload length : $pyaLoadSize")


            val chc1 = data[6]
            val chc2 = data[7]
            Log.d(TEST, "chc1 : $chc1, chc2 : $chc2")

            val year = data[INDEX_YEAR].toInt() + 2000
            val month = data[INDEX_MONTH].toInt()
            val day = data[INDEX_DAY].toInt()
            val hour = data[INDEX_HOUR].toInt()
            val min = data[INDEX_MINUTE].toInt()
            val sec = data[INDEX_SECOND].toInt()

            var lastWeo1 = 0.0

            Log.e(TEST, "date : ${year}년 ${month}월 ${day}일 ${hour}시 ${min}분 ${sec}초")

            val zoneId = ZoneId.of("Asia/Seoul")

            val startTime: Long =
                LocalDateTime.of((year), month, day, hour, min, sec).atZone(zoneId)
                    .toInstant().toEpochMilli()
            Log.d("time", "startTime : $startTime")

            val now = LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli()
            Log.d("time", "now : $now")

            val lastTime: Long =
                startTime + ((1000 * 10).toLong() * (nDataLength - 1))
            Log.d("time", "lastTime : $lastTime")

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
                val nTHigh: Int = 16 + i * step
                val nTLow: Int = 17 + i * step
                Log.e(TEST, "TEMP : $nTHigh : $nTLow")
                val nWHigh: Int = 18 + i * step
                val nWMiddle: Int = 19 + i * step
                val nWLow: Int = 20 + i * step
                Log.e(TEST, "WE Address : $nWHigh : $nWMiddle : $nWLow")
                val nAHigh: Int = 21 + i * 6
                val nAMiddle: Int = 22 + i * 6
                val nALow: Int = 23 + i * 6
                Log.e(TEST, "AE Address : " + nAHigh + " : " + nAMiddle + " : " + nALow)

                val time: Long = startTime + (1000 * 10 * i)

                val vTemperature = (data[nTHigh].toUByte().toInt() shl 8) or (data[nTLow].toUByte().toInt())
                val temperature = vTemperature / 100.0

                val weCurrent: Double = convertToCurrentDataDouble(data[nWHigh], data[nWMiddle], data[nWLow])
                val aeCurrent: Double = convertToCurrentDataDouble(data[nAHigh], data[nAMiddle], data[nALow])

                Log.e(TEST, "TEMP " + i + ": " + temperature)
                Log.e(TEST, "WE_Current " + i + ": " + weCurrent)
                Log.e(TEST, "AE_Current " + i + ": " + aeCurrent)

                val convertedTime = Instant.ofEpochMilli(time)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                saveData.add(
                    UserValue(
                        userId = userId,
                        weCurrent = weCurrent,
                        aeCurrent = aeCurrent,
                        temperature = temperature,
                        createdAt = convertedTime,
                        createdAtLong = time
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