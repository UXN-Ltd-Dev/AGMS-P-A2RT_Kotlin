package kr.co.uxn.agms_p.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service.NOTIFICATION_SERVICE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.BleConnectionState
import kr.co.uxn.agms_p.PythonManager
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.room.UserValue
import java.lang.reflect.Method
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
class BleManager(
    val context: Context,
    val mac: String,
    val userId: Int,
    val applicationContext: Context
) : BluetoothGattCallback() {

    companion object {
        @Volatile
        private var INSTANCE: BleManager? = null

        const val characteristicUuidReadT21 = "e093f3b5-00a3-a9e5-9eca-40026e0edc24"
        const val characteristicUuidWriteT21 = "e093f3b5-00a3-a9e5-9eca-40036e0edc24"
        const val serviceUuidT21 = "e093f3b5-00a3-a9e5-9eca-40016e0edc24"


        fun getInstance(
            context: Context,
            mac: String,
            userId: Int,
            applicationContext: Context
        ): BleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BleManager(context, mac, userId, applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        const val TEST = "TEST"
    }


    private var timerWhenConnected: Timer? = null
    private var timerTaskForConnected: TimerTask? = null

    lateinit var bufferWeo1: String
    lateinit var bufferWeo2: String


    var mGatt: BluetoothGatt? = null
    var isReconnect = true

//    lateinit var bluetoothDevice: BluetoothDevice
//    lateinit var bluetoothManager: BluetoothManager
//    lateinit var bluetoothAdpater: BluetoothAdapter


    val disconnectHandler = Handler(Looper.getMainLooper())
    val reconnectHandler = Handler(Looper.getMainLooper())


    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        Log.e(TEST, "onConnectionStateChange 진입")
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.e("gatt", "gatt connected!")

                // BleBridge에 상태 연결완료 전송
                BleBridge.updateState(BleConnectionState.CONNECTED)
                // afterChange값 불러오기 (sharedpreference)
//                val prefAfterChange = context.getSharedPreferences("afterChange", Context.MODE_PRIVATE)
//                val afterChange = prefAfterChange.getBoolean("afterChange", false)
//                Log.e(TAG, "onConnectionStateChange - connected -> afterChange : $afterChange")

//                context.getSharedPreferences("isChange", Context.MODE_PRIVATE)
//                    .edit()
//                    .putBoolean("isChange", false)
//                    .apply()


//                if (isConnectingTest) {
//                    mGatt?.close()
//                }


//                isChange = false
//                mGatt = gatt


//                if(isReconnect) {
//                    // mtu 517로 요청
//                    gatt?.requestMtu(517)
//                } else {
//                    gatt?.disconnect()
//                    gatt?.close()
//                }

                // mtu 517로 요청
                gatt?.requestMtu(517)

            }


            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.e("gatt", "gatt disconnected!!!")

                // 끊김 알림
                CoroutineScope(Dispatchers.IO).launch {
                    val isShowBleNoti = DataStoreManager.getNotiLostSignal().first() ?: false
                    Log.e("BLE", "BLE 연결 끊김 알림 DS로부터 불러온 설정값 : $isShowBleNoti")
                    if (isShowBleNoti) {
                        withContext(Dispatchers.Main) {
                            sendNotification(context, "센서와의 연결이 일시적으로 끊어졌어요",
                                "",
                                89
                            )
                            BleBridge.showBleConnectDialog(true )
                        }
                    }
                }


                // BleBridege에 Disconnected 상태 전송
                BleBridge.updateState(BleConnectionState.DISCONNECTED)

                // afterChange pref 변수 불러오기
                val prefAfterChange =
                    context.getSharedPreferences("afterChange", Context.MODE_PRIVATE)
                val afterChange = prefAfterChange.getBoolean("afterChange", false)

                val sharedPreferences =
                    context.getSharedPreferences("isChange", Context.MODE_PRIVATE)
                var isChange = sharedPreferences.getBoolean("isChange", true)

//                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(context, "BLE가 끊어졌습니다", Toast.LENGTH_SHORT).show()
//                }

                Log.d(TEST, "disconnect 콜백 내부 : ${gatt?.device?.name}")
                Log.d(TEST, "BLE 매니저에서 isChange 상태 : $isChange")



                reconnectHandler.postDelayed({
                    // BleBridege에 Connecting 상태 전송
                    BleBridge.updateState(BleConnectionState.CONNECTING)
                    reconnect(gatt, mac)
                }, 3000) // 일반 모드일 때, 재연결 3초 뒤에 실행

//                if (!isChange) {
//                    // isClicked : 끊겼을 때, 붉은 배경으로 바로 바꿔줄지, 딜레이를 줄지를 결정하는 변수
//                    // true -> 즉시 : 기기 목록에서 클릭해서 데이터 화면으로 넘어왔을 때
//                    // false -> 딜레이 : 끊겼다가 연결될 때(reconnect)
//                        Log.e("culture", "!testMode 안")
//                        reconnectHandler.postDelayed({
//                            reconnect(gatt, viewModel.getDevice().deviceMac!!)
//                            CoroutineScope(Dispatchers.Main).launch {
//                                viewModel.updateBleState("connecting")
//                            }
//                        }, 3000) // 일반 모드일 때, 재연결 3초 뒤에 실행
//
//                } else {
//                    isGattWork = false // 다른 기기 연결 허용

//                    gatt?.close()
//                    refreshDeviceCache(gatt)
//                    mGatt = null

//                    if (afterChange) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            Toast.makeText(
//                                context,
//                                "기존 연결이 끊겼습니다.\n새로운 기기 연결 가능",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            // 서비스 종료
//                            val serviceIntent = Intent(context, DataService::class.java)
//                            context.stopService(serviceIntent)
//                        }
//                    }
//                }
            }


            BluetoothProfile.STATE_CONNECTING -> {
                // 인식 못함
            }

        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)

        CoroutineScope(Dispatchers.Main).launch() {
//            viewModel.updateRssi(Rssi(rssi, gatt?.device.toString()))
        }
        Log.d(TEST, "rssi : $rssi, deviceName : ${gatt?.device}")
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TEST, "가트 연결이 되었고, onServicesDiscovered에 진입!")
            // ==== 서비스 조회 ====
            val services = gatt?.services
            lateinit var service: BluetoothGattService
            services?.forEach { service ->
                Log.d(TEST, "Service UUID: ${service.uuid}")
                val characteristics = service.characteristics
                characteristics.forEach { characteristic ->
                    Log.d(TEST, "Characteristic UUID: ${characteristic.uuid}")
                }
            }

            val characteristicUUID = UUID.fromString(characteristicUuidReadT21)

            // 원하는 서비스로 지정
            var found = false
            var i = 0
            while (i < services!!.size && !found) {
                val currentService = services[i]
                if (currentService.uuid.toString()
                        .equals(serviceUuidT21, ignoreCase = true)
                ) {
                    service = currentService
                    found = true
                }
                i++
            }


            val readCharacteristic = service.getCharacteristic(characteristicUUID)
            gatt.setCharacteristicNotification(readCharacteristic, true)

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    "데이터를 가져오는 중입니다.. \n잠시만 기다려주세요..",
                    Toast.LENGTH_SHORT
                ).show()

            }

        } else {
            Log.d(TEST, "가트 진입 실패!")
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)

        val data = characteristic!!.value
        val deviceName = gatt?.device?.name

        Log.d(TEST, "commandId  : ${String.format("0x%02X", data[2])}")
        Log.d(TEST, "status  : ${String.format("0x%02X", data[3])}")
        Log.e(TEST, "데이터 size : ${data.size}")
        Log.e(TEST, "data[4] length : ${java.lang.Byte.toUnsignedInt(data[4]) + 8}")

        // Check CRC Code
        val crc1 = java.lang.Byte.toUnsignedInt(data[6])
        val crc2 = java.lang.Byte.toUnsignedInt(data[7])
        Log.d(TEST, "crc1 : $crc1, crc2 : $crc2")

        val crc = (crc1 * 256) + crc2
        Log.d(TEST, "수신된 crc : $crc")

        val checkCrc = checkCrc(data)
        Log.d(TEST, "계산된 crc : $checkCrc")

        // crc 안 맞으면 crc 에러 응답
        if (crc != checkCrc) {
            Log.e(TEST, "crc 에러 발생!")
            writeAgms(gatt!!, makeByteArrayWithState(0x01.toByte())) // crc_error
            return
        } else {
            Log.e(TEST, "=== crc 체크 통과! ===")
        }

        if (data[0] == 0xA0.toByte() && data[1] == 0x81.toByte()) {
            Log.d(
                TEST,
                "data[0] : ${String.format("0x%02X", data[0])},  0xA0\n" +
                        "data[1] : ${String.format("0x%02X", data[1])},  0x81"
            )
            val commandId = data[2]
            Log.d(TEST, "data[2] commandId converted = ${0x42.toByte()}")
            Log.d(TEST, "data[2] commandId = ${data[2]}")

            when (commandId) {
                // CMD_SEND (0x42)
                0x42.toByte() -> {
                    val length = java.lang.Byte.toUnsignedInt(data[4])
                    val calLength = 8 + length
                    if (data.size != calLength) {
                        Log.e(
                            TEST,
                            "데이터 길이 맞지 않음\nlength = ${data.size}\ncal_length = ${calLength}"
                        )
                        writeAgms(
                            gatt!!,
                            makeByteArrayWithState(0x02.toByte())
                        ) // date_length_error
                    } else {
                        // 데이터 받아오기
                        Log.e(
                            TEST,
                            "=== 데이터 길이 일치! === \nlength = ${data.size}\ncal_length = ${calLength}"
                        )
                        try {
                            val checker = receiveDataAndInsert(data, deviceName)
                            if (checker == -1) {
                                // 0x12 : time error
                                writeAgms(gatt!!, makeByteArrayWithState(0x12.toByte()))
                            } else {
                                // 0x00 : no error (OK)
                                writeAgms(gatt!!, makeByteArrayWithState(0x00.toByte()))
                            }

                        } catch (e: Exception) {
                            Log.e(TEST, "데이터 받아오기 에러발생")
                            CoroutineScope(Dispatchers.Main).launch {
                                Log.e(TEST, "프로토콜2.0이 아님")
                                Log.e(TEST, "error : ${e.message}")
                                Toast.makeText(
                                    context,
                                    "해당 기기는 프로토콜2.0이 아닙니다.\n다른 기기를 선택해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
//                                isChange = true

                                gatt?.disconnect()
//                                eventViewModel.goBleList(0, true)
                            }
                        }
                    }
                }

                // CMD_RTC (0x41)
                0x41.toByte() -> {
                    // sendRTC
                    val state = data[3]
                    Log.d(TEST, "state = ${String.format("0x%02X", data[3])}")
                    if (state == 0x11.toByte() || state == 0x12.toByte()) {
                        // write 기능
                        writeAgms(gatt!!, sendRtc())
                        Log.d(TEST, "SEND_RTC!!!")
                        //    return
                    }
                }
            }
        } else {
            Log.e(TEST, "start code Error!")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        gatt?.discoverServices();
    }

    @SuppressLint("MissingPermission")
    private fun reconnect(gatt: BluetoothGatt?, address: String) {
        gatt?.close()
        refreshDeviceCache(gatt)

        // 스캔 방식
//        mGatt = gatt?.device?.connectGatt(context, false, this)
        // 논스캔 방식
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdpater = bluetoothManager.adapter
        val bluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.e(TEST, "======BLE Remote 연결 안드로이드 13 이상 시작========")
            bluetoothAdpater.getRemoteLeDevice(address, BluetoothDevice.ADDRESS_TYPE_PUBLIC)
        } else {
            Log.e(TEST, "======BLE Remote 연결 안드로이드 10~12 시작========")
            bluetoothAdpater.getRemoteDevice(address)
        }

//        mGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(context, false, this, BluetoothDevice.TRANSPORT_LE)
        } else {
            bluetoothDevice.connectGatt(context, false, this)
        }
    }

    private fun sendRtc(): ByteArray {
        Log.d("choco5732", "sendRTC 호출!!")
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

        Log.d("sendRTC", "in sendRTC data6 :${data[6]}, data7 :${data[7]}")

        return data
    }

    private fun makeByteArrayWithState(state: Byte): ByteArray {
        val data = ByteArray(8)

        data[0] = 0xA0.toByte()
        data[1] = 0x81.toByte()
        data[2] = 0x42.toByte()
        data[3] = state
        data[4] = 0x00.toByte()
        data[5] = 0x00.toByte()
        data[6] = 0x00.toByte()
        data[7] = 0x00.toByte()

        val crc: Int = checkCrc(data)

        data[6] = (crc shr 8).toByte()
        data[7] = crc.toByte()

        Log.e(TEST, "makeByteArrayWithState : ${String.format("0x%02X", state)}")

        return data
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun receiveDataAndInsert(data: ByteArray, deviceName: String?): Int {

        // 리스트 생성
        val saveData: MutableList<UserValue> = mutableListOf<UserValue>()

        // 데이터 받아오기
        val stx1 = data[0]
        val stx2 = data[1]
        Log.d(TEST, "stx1 : ${stx1}, stx2: ${stx2}")

        val commandId = data[2]
        val status = data[3]
        Log.d(TEST, "commandId : ${commandId}")
        // printf("%02X\n", 10);   // 출력 (앞의 빈자리를 0으로 채우기): 0A
        Log.d(TEST, "status : ${status}")
        // 왜 0x%02X라는 작업을 포맷하는가? 오는 파일이 16진수인가?

        Log.d(TEST, "data의 size : ${data.size}")
        val length = data[4]
        Log.d("data", "length : $length")

        val nDataLength =
            (java.lang.Byte.toUnsignedInt(data[4]) - 10) / 6

        val reserved = data[5]
        Log.d(TEST, "reversed : $reserved")

        val chc1 = data[6]
        val chc2 = data[7]
        Log.d(TEST, "chc1 : $chc1, chc2 : $chc2")

        val year = data[8].toInt()
        val month = data[9].toInt()
        val day = data[10].toInt()
        val hour = data[11].toInt()
        val min = data[12].toInt()
        val sec = data[13].toInt()

        var lastWeo1 = 0.0

        Log.d(TEST, "time : 20${year}년 ${month}월 ${day}일 ${hour}시 ${min}분 ${sec}초")

        val cal = Calendar.getInstance().apply {
            set(
                Calendar.YEAR,
                year + 2000
            ) // Assuming time1 is in the format YY and represents a year after 2000
            set(
                Calendar.MONTH,
                month - 1
            ) // Calendar.MONTH is zero-based, so we need to subtract 1
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour) // 24-hour format, range 0-23
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, sec)
            set(Calendar.MILLISECOND, 0) // Optionally set milliseconds to zero
        }

        val calTime = cal.timeInMillis
        Log.d(TEST, "calTime in milliseconds: $calTime")

        val zoneId = ZoneId.of("Asia/Seoul")
        val startTime: Long =
            LocalDateTime.of((year + 2000), month, day, hour, min, sec).atZone(zoneId)
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
            return -1
        }

        // 배터리
        val batteryLevel =
            java.lang.Byte.toUnsignedInt(data[14]) + (java.lang.Byte.toUnsignedInt(data[15]) / 100.0f * 100).roundToInt() / 100.0

        val decimalFormat = DecimalFormat("#.00")
        val battery = decimalFormat.format(batteryLevel)

//        val createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val createdAt = Instant.ofEpochMilli(lastTime)
            .atZone(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        Log.d(TEST, "battery : $battery")

        // 온도
        val temperature =
            java.lang.Byte.toUnsignedInt(data[16]) + (java.lang.Byte.toUnsignedInt(data[17]) / 100.0f * 100).roundToInt() / 100.0

        Log.d(TEST, "temperature : $temperature")

        // weo1, weo2

        val findBufferWeoCount: Int = (data.size - 18) / 6
        Log.d(TEST, "findBufferWeoCount : $findBufferWeoCount")
        Log.d(TEST, "nDataLength : $nDataLength")
        for (i in 0 until findBufferWeoCount) {
            val nWHigh: Int = 18 + i * 6
            val nWMiddle: Int = 19 + i * 6
            val nWLow: Int = 20 + i * 6
//            Log.e(TEST, "WE Address : $nWHigh : $nWMiddle : $nWLow")

            val nAHigh: Int = 21 + i * 6
            val nAMiddle: Int = 22 + i * 6
            val nALow: Int = 23 + i * 6
//            Log.e(TEST, "AE Address : " + nAHigh + " : " + nAMiddle + " : " + nALow)
            val time: Long = startTime + (1000 * 10 * i)

            val weCurrent: Double =
                convertToCurrentDataDouble(data[nWHigh], data[nWMiddle], data[nWLow])
            val aeCurrent: Double =
                convertToCurrentDataDouble(data[nAHigh], data[nAMiddle], data[nALow])

            Log.e(TEST, "WE_Current " + i + ": " + weCurrent)
            Log.e(TEST, "AE_Current " + i + ": " + aeCurrent)
//            Log.e("PYTHON", "Glucose " + i + ": " + "${PythonManager.instance.calculationGlucose(time / 1000, weCurrent, aeCurrent)}")

            val convertedTime = Instant.ofEpochMilli(time)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            saveData.add(
                UserValue(
                    userId = userId,
                    weCurrent = weCurrent,
                    aeCurrent = aeCurrent,
                    createdAt = convertedTime,
                    createdAtLong = time
                )
            )
            if (i == findBufferWeoCount - 1) {
                lastWeo1 = weCurrent
            }
        }

        // ui에 weo1 값 실시간 갱신
//        BleBridge.updateWeo1(lastWeo1)

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(context)?.dataDao()?.insertUserValue(saveData)
        }



        return 0
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


    var sRefreshMethod: Method? = null

    @Throws(NoSuchMethodException::class)
    private fun refreshMethod(): Method {
        if (sRefreshMethod == null) {
            sRefreshMethod = BluetoothGatt::class.java.getMethod("refresh")
        }
        return sRefreshMethod!!
    }

    fun refreshDeviceCache(gatt: BluetoothGatt?) {
        try {
            val method: Method = refreshMethod()
            val isRefreshSuccess = method.invoke(gatt) as Boolean
            if (isRefreshSuccess) {
                Log.d(TEST, "Bluetooth refresh cache")
            }
        } catch (e: java.lang.Exception) {
            Log.e(TEST, e.getLocalizedMessage());
            Log.e(TEST, "An exception occurred while refreshing device");
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

    fun writeAgms(gatt: BluetoothGatt, value: ByteArray) {
        Log.d(TEST, "writeAgms 실행됨!")
        val services: List<BluetoothGattService> = gatt.services
        lateinit var service: BluetoothGattService
        var found = false
        var i = 0
        while (i < services!!.size && !found) {
            val currentService = services[i]
            if (currentService.uuid.toString()
                    .equals(serviceUuidT21, ignoreCase = true)
            ) {
                service = currentService
                found = true
            }
            i++
        }
        Log.d(TEST, "in writeAgms - service : ${service.uuid.toString()}")

        val characteristicUuid = UUID.fromString(characteristicUuidWriteT21)
        val writeCharacteristic = service.getCharacteristic(characteristicUuid)
        Log.d(
            TEST,
            "in writeAgms - writeCharacteristic : ${writeCharacteristic.uuid.toString()}"
        )

        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            gatt.writeCharacteristic(
                writeCharacteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )

            //            gatt.writeCharacteristic(writeCharacteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            Log.d(TEST, "티라미수 이상 버전 write")
        } else {
            writeCharacteristic.setValue(value)
            gatt.writeCharacteristic(writeCharacteristic)
            Log.d(TEST, "기존 버전 write")
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
        val channelId = "ble_disconnect_alert_channel"

        // Oreo 이상은 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ble disconnect alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ble disconnect alert"
            }

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(context, channelId)
            .setOngoing(true)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

}
