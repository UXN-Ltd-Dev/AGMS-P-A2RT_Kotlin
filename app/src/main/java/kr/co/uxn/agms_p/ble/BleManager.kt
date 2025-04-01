/*
package kr.co.uxn.agms_p.ble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
class BleManager(val context: Context) : BluetoothGattCallback() {

    val viewModel: MainViewModel by lazy {
        com.example.protocol20datainfo.presentation.Application.mainViewModel
    }

    val eventViewModel: EventViewModel by lazy {
        com.example.protocol20datainfo.presentation.Application.eventViewModel
    }

    private var timerWhenConnected: Timer? = null
    private var timerTaskForConnected: TimerTask? = null

    lateinit var bufferWeo1: String
    lateinit var bufferWeo2: String

    private var count = 1

    lateinit var fragmentContext: Context
    lateinit var bluetoothDevice: BluetoothDevice
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdpater: BluetoothAdapter


    var onConnectionChangedCount: Int = -1

    companion object {
        @Volatile
        private var INSTANCE: BleManager? = null

        fun getInstance(context: Context): BleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BleManager(context).also { INSTANCE = it }
            }
        }

        val disconnectHandler = Handler(Looper.getMainLooper())
        val reconnectHandler = Handler(Looper.getMainLooper())
    }

    fun setContext(context: Context) {
        fragmentContext = context
    }

    fun getRssi(): Rssi? {
        return viewModel.rssi.value
    }

    fun getBleState(): String? {
        return viewModel.bleState.value
    }

    fun getData(): ProtocolData? {
        return viewModel.data.value
    }

    fun getIsShow(): Boolean? {
        return viewModel.isShow.value
    }


    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        // gatt 초기화
        mGatt = gatt
        val prefPeriod = context.getSharedPreferences("period", Context.MODE_PRIVATE)
        val period = prefPeriod.getString("period", "3")?.toLong()!!
        val convertedPeriod = period * 1000 * 60

//        val onConnectionChangedCount = viewModel.onConnectionChangedCount.value!!

        Log.d(TEST, "BleManger onConnectionStateChange에서 period의 값은 ${period}분")
        Log.e(TAG, "onConnectionStateChange 진입")
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                // afterChange값 불러오기 (sharedpreference)
                val prefAfterChange = context.getSharedPreferences("afterChange", Context.MODE_PRIVATE)
                val afterChange = prefAfterChange.getBoolean("afterChange", false)
                Log.e(TAG, "onConnectionStateChange - connected -> afterChange : $afterChange")

                if (afterChange) {
                    Log.e(TEST, "10")
                    mGatt?.disconnect()
                } else {
                    Log.e(TEST, "11")
                    context.getSharedPreferences("isChange", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isChange", false)
                        .apply()
                }

                */
/**
                 * 필요없어 보여서 주석처리
                 *
                 *//*

//                if (isConnectingTest) {
//                    mGatt?.close()
//                }

                if (testMode) {
                    // test모드일 경우, 연결 될 때마다 카운트 증가
                    Log.d(TEST, "BLEMANAGER STATE_CONNECTED 테스트모드 진입")
                    var count = viewModel.testCount.value
                    if (count != null) {
                        count++
                        GlobalScope.launch(Dispatchers.Main) {
                            viewModel.updateTestCount(count)
                        }
                    }
                }

//                isChange = false
//                mGatt = gatt
                // mtu 517로 요청
                gatt?.requestMtu(517)
                // rssi읽기 요청. 성공하면 rssi값 onReadRemoteRssi로 메서드가 호출됨.
                // 타이머를 이용해 5초 간격으로 실행

                if (timerTaskForConnected == null) {
                    timerTaskForConnected = object : TimerTask() {
                        override fun run() {
                            gatt?.readRemoteRssi()
                        }
                    }
                    timerWhenConnected = Timer()
                    timerWhenConnected!!.schedule(timerTaskForConnected, 0, 5000)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    Log.e("gatt", "gatt connected!")
                    viewModel.updateIsShow(false)
                    viewModel.updateBleState("connected")

                    val deviceName = gatt?.device?.name
                    if (deviceName != null) {
                        viewModel.updateDeviceName(deviceName!!)
                        Log.d("miracle", "뷰모델 업데이트 디바이스명 성공")
                    }

                    // 기기명 저장
                    latestConnectedDevice = mGatt?.device?.name ?: "emptyDevice"
                    Log.d("gatt", "latestConnectedDevice : $latestConnectedDevice")
                }

                // 항상 true가 나오는건 아니다.
                // 따라서 false일 경우 다시 connect하는 로직 추후에 작성할 것

            }

            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.e("choco5744", "1")
                gatt?.disconnect()
                Log.e("gatt", "gatt disconnected!!!")
                timerWhenConnected?.cancel()
                timerTaskForConnected = null
                timerWhenConnected = null

                // afterChange pref 변수 불러오기
                val prefAfterChange = context.getSharedPreferences("afterChange", Context.MODE_PRIVATE)
                val afterChange = prefAfterChange.getBoolean("afterChange", false)

                val sharedPreferences = context.getSharedPreferences("isChange", Context.MODE_PRIVATE)
                var isChange = sharedPreferences.getBoolean("isChange", true)

                Log.d("TEST", "BleManager Disconnected에서, isChange : $isChange, afterChange : $afterChange")
                CoroutineScope(Dispatchers.Main).launch {
                    // 끊겼을 경우 아래코드 주석 처리하면 데이터 안사라짐
                    viewModel.updateIsShow(false)
                    viewModel.updateBleState("disconnected")
                    Toast.makeText(context, "gatt 연결 끊어짐!", Toast.LENGTH_SHORT).show()
                    Log.e("choco5744", "2")
                }

                Log.d("test", "disconnect 콜백 내부 : ${mGatt?.device?.name}")
                Log.d("test", "BLE 매니저에서 isChange 상태 : $isChange")

                if (!isChange) {
                    Log.e("choco5744", "4")
                    // isClicked : 끊겼을 때, 붉은 배경으로 바로 바꿔줄지, 딜레이를 줄지를 결정하는 변수
                    // true -> 즉시 : 기기 목록에서 클릭해서 데이터 화면으로 넘어왔을 때
                    // false -> 딜레이 : 끊겼다가 연결될 때(reconnect)
                    isClicked = false
                    if (!testMode) {
                        Log.e("choco5744", "5")
                        Log.e("culture", "!testMode 안")
                        reconnectHandler.postDelayed({
                            reconnect(mGatt, viewModel.getDevice().deviceMac!!)
                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.updateBleState("connecting")
                            }
                        }, 3000) // 일반 모드일 때, 재연결 3초 뒤에 실행
                    } else {
                        // 테스트 모드일 때,
                        if (viewModel.testCount.value == -1) {
                            Log.e(TEST, "testCount : ${viewModel.testCount.value}")
                            reconnectHandler.postDelayed({
                                reconnect(mGatt, viewModel.getDevice().deviceMac!!)
                                CoroutineScope(Dispatchers.Main).launch {
                                    viewModel.updateBleState("connecting")
                                }
                            }, 3000)
                        } else {
                            Log.e(TEST, "testCount -1이 아닐 때 : ${viewModel.testCount.value}")
                            reconnectHandler.postDelayed({
                                reconnect(mGatt, viewModel.getDevice().deviceMac!!)
                                CoroutineScope(Dispatchers.Main).launch {
                                    viewModel.updateBleState("connecting")
                                }
                            }, convertedPeriod)
                        }
                    }
                } else {
                    Log.e("choco5744", "7")
                    Log.d("TEST", "isChange true로 빠짐")
                    isGattWork = false // 다른 기기 연결 허용

                    mGatt?.close()
                    gatt?.close()
                    refreshDeviceCache(mGatt)
                    refreshDeviceCache(gatt)
                    mGatt = null

                    if (afterChange) {
                        Log.e("choco5744", "8")
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "기존 연결이 끊겼습니다.\n새로운 기기 연결 가능",Toast.LENGTH_SHORT).show()
                            // 서비스 종료
                            val serviceIntent = Intent(context, DataService::class.java)
                            context.stopService(serviceIntent)
                        }
                    }
                }
            }

            BluetoothProfile.STATE_CONNECTING -> {
                // 인식 못함
            }
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)

        CoroutineScope(Dispatchers.Main).launch() {
            viewModel.updateRssi(Rssi(rssi, gatt?.device.toString()))
        }
        Log.d(DATA, "rssi : $rssi, deviceName : ${gatt?.device}")
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("choco5732", "가트 연결이 되었고, onServicesDiscovered에 진입!")
            // ==== 서비스 조회 ====
            val services = gatt?.services
            lateinit var service: BluetoothGattService
            services?.forEach { service ->
                Log.d("choco5732", "Service UUID: ${service.uuid}")
                val characteristics = service.characteristics
                characteristics.forEach { characteristic ->
                    Log.d(TAG, "Characteristic UUID: ${characteristic.uuid}")
                }
            }

            val characteristicUUID = UUID.fromString(MainActivity.characteristicUuidReadT21)

            // 원하는 서비스로 지정
            var found = false
            var i = 0
            while (i < services!!.size && !found) {
                val currentService = services[i]
                if (currentService.uuid.toString()
                        .equals(MainActivity.serviceUuidT21, ignoreCase = true)
                ) {
                    service = currentService
                    found = true
                }
                i++
            }

            Log.d(TAG, "찾은 서비스는 ${service.toString()}입니다.")

            val readCharacteristic = service.getCharacteristic(characteristicUUID)
//                val finaldescriptor = finalCharacteristic.getDescriptor()
            gatt.setCharacteristicNotification(readCharacteristic, true)

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    "데이터를 가져오는 중입니다.. \n잠시만 기다려주세요..",
                    Toast.LENGTH_SHORT
                ).show()

                val deviceName: String = gatt.device.name
                Log.d(TAG, "디바이스네임 : $deviceName")
                val deviceAddress: String = gatt.device.address
                Log.d(TAG, "디바이스주소 : $deviceAddress")
//                    viewModel.updateDeviceName(deviceName)
                viewModel.updateDeviceAddress(deviceAddress)

            }

        } else {
            Log.d("choco5732", "가트 진입 실패!")
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)

        // period 가져오기
        val prefPeriod = context.getSharedPreferences("period", Context.MODE_PRIVATE)
        val period = prefPeriod.getString("period", "3")?.toLong()!!
        val convertedPeriod = period * 1000 * 60
        if (testMode) {
            // onConnectionChangedCount 변수를 뷰모델로부터 가져와서 +1 하고 저장
            onConnectionChangedCount = viewModel.onConnectionChangedCount.value!!
            Log.d("jaeyong", "onConnectionChangedCount : $onConnectionChangedCount")
            // +1
            val updateCount = onConnectionChangedCount + 1
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.updateOnConnectionCount(updateCount)
            }
            // +1 한거 불러오기
//        onConnectionChangedCount = viewModel.onConnectionChangedCount.value!!
        }


        if (testMode && onConnectionChangedCount == -1) {
                Log.e("jaeyong", "==시작==  period : $period")
                Log.e("jaeyong", "if문 진입")
                disconnectHandler.postDelayed({
                    mGatt?.disconnect()
                    Log.e("jaeyong", "연결 끊음, convertedPeriod : $convertedPeriod")
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.updateOnConnectionCount(-1)
                    }
                }, convertedPeriod)
            }

        val data = characteristic!!.value
        val deviceName = gatt?.device?.name

        Log.d(TAG, "commandId  : ${String.format("0x%02X", data[2])}")
        Log.d(TAG, "status  : ${String.format("0x%02X", data[3])}")
        Log.e(TAG, "데이터 size : ${data.size}")
        Log.e(TAG, "data[4] length : ${java.lang.Byte.toUnsignedInt(data[4]) + 8}")

        // Check CRC Code
        val crc1 = java.lang.Byte.toUnsignedInt(data[6])
        val crc2 = java.lang.Byte.toUnsignedInt(data[7])
        Log.d("data", "crc1 : $crc1, crc2 : $crc2")

        val crc = (crc1 * 256) + crc2
        Log.d("data", "수신된 crc : $crc")

        val checkCrc = checkCrc(data)
        Log.d("data", "계산된 crc : $checkCrc")

        // crc 안 맞으면 crc 에러 응답
        if (crc != checkCrc) {
            Log.e("data", "crc 에러 발생!")
            writeAgms(gatt!!, makeByteArrayWithState(0x01.toByte())) // crc_error
            return
        } else {
            Log.e("data", "=== crc 일치! ===")
        }

        if (data[0] == 0xA0.toByte() && data[1] == 0x81.toByte()) {
            Log.d(
                "data",
                "data[0] : ${String.format("0x%02X", data[0])},  0xA0\n" +
                        "data[1] : ${String.format("0x%02X", data[1])},  0x81"
            )
            val commandId = data[2]
            Log.d("data", "data[2] commandId converted = ${0x42.toByte()}")
            Log.d("data", "data[2] commandId = ${data[2]}")

            when (commandId) {
                // CMD_SEND (0x42)
                0x42.toByte() -> {
                    val length = java.lang.Byte.toUnsignedInt(data[4])
                    val calLength = 8 + length
                    if (data.size != calLength) {
                        Log.e(
                            TAG,
                            "데이터 길이 맞지 않음\nlength = ${data.size}\ncal_length = ${calLength}"
                        )
                        writeAgms(
                            gatt!!,
                            makeByteArrayWithState(0x02.toByte())
                        ) // date_length_error
                    } else {
                        // 데이터 받아오기
                        Log.e(
                            TAG,
                            "=== 데이터 길이 일치! === \nlength = ${data.size}\ncal_length = ${calLength}"
                        )
                        try {
                            val checker = receiveDataAndInsert(data, deviceName)
                            if (checker != -1) {
                                // 0x00 : no error (OK)
                                writeAgms(gatt!!, makeByteArrayWithState(0x00.toByte()))
                            } else {
                                // 0x12 : time error
                                writeAgms(gatt!!, makeByteArrayWithState(0x12.toByte()))
                            }

                        } catch (e: Exception) {
                            Log.e("jaeyong", "데이터 받아오기 에러발생")
                            CoroutineScope(Dispatchers.Main).launch {
                                Log.e(TAG, "프로토콜2.0이 아님")
                                Log.e(TAG, "error : ${e.message}")
                                Toast.makeText(
                                    context,
                                    "해당 기기는 프로토콜2.0이 아닙니다.\n다른 기기를 선택해주세요.",
                                    Toast.LENGTH_LONG
                                ).show()
//                                isChange = true

                                gatt?.disconnect()
                                eventViewModel.goBleList(0, true)
                            }
                        }
                    }
                }

                // CMD_RTC (0x41)
                0x41.toByte() -> {
                    // sendRTC
                    val state = data[3]
                    Log.d(DATA, "state = ${String.format("0x%02X", data[3])}")
                    if (state == 0x11.toByte() || state == 0x12.toByte()) {
                        // write 기능
                        writeAgms(gatt!!, sendRtc())
                        Log.d(TAG, "SEND_RTC!!!")
                        //    return
                    }
                }
            }
        } else {
            Log.e(DATA, "start code Error!")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        gatt?.discoverServices();
    }

    @SuppressLint("MissingPermission")
    private fun reconnect(gatt: BluetoothGatt?, address: String) {
        mGatt?.close()
        gatt?.close()
        refreshDeviceCache(mGatt)
        refreshDeviceCache(gatt)
        mGatt = null

        // 스캔 방식
//        mGatt = gatt?.device?.connectGatt(context, false, this)
        // 논스캔 방식
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdpater = bluetoothManager.adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.e(TEST, "======BLE Remote 연결 안드로이드 13 이상 시작========")
            bluetoothDevice = bluetoothAdpater.getRemoteLeDevice(address, BluetoothDevice.ADDRESS_TYPE_PUBLIC)
        } else {
            Log.e(TEST, "======BLE Remote 연결 안드로이드 10~12 시작========")
            bluetoothDevice = bluetoothAdpater.getRemoteDevice(address)
        }

        mGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        Log.e(TAG, "now time: $year-$month-$date $hour24:$minute:$second")

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

        Log.e(DATA, "makeByteArrayWithState : ${String.format("0x%02X", state)}")

        return data
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun receiveDataAndInsert(data: ByteArray, deviceName: String?): Int {
        // 카운트
        Log.d(DATA, "${count} 번째 불러온 데이터")
        count++

        // 데이터 받아오기
        val stx1 = data[0]
        val stx2 = data[1]
        Log.d(DATA, "stx1 : ${stx1}, stx2: ${stx2}")

        val commandId = data[2]
        val status = data[3]
        Log.d(DATA, "commandId : ${commandId}")
        // printf("%02X\n", 10);   // 출력 (앞의 빈자리를 0으로 채우기): 0A
        Log.d(DATA, "status : ${status}")
        // 왜 0x%02X라는 작업을 포맷하는가? 오는 파일이 16진수인가?

        Log.d(DATA, "data의 size : ${data.size}")
        val length = data[4]
        Log.d("data", "length : $length")

        val nDataLength =
            (java.lang.Byte.toUnsignedInt(data[4]) - 10) / 6

        val reserved = data[5]
        Log.d(DATA, "reversed : $reserved")

        val chc1 = data[6]
        val chc2 = data[7]
        Log.d(DATA, "chc1 : $chc1, chc2 : $chc2")

        val time1 = data[8].toInt()
        val time2 = data[9].toInt()
        val time3 = data[10].toInt()
        val time4 = data[11].toInt()
        val time5 = data[12].toInt()
        val time6 = data[13].toInt()

        Log.d(DATA, "time : 20${time1}년 ${time2}월 ${time3}일 ${time4}시 ${time5}분 ${time6}초 ")

        val cal = Calendar.getInstance().apply {
            set(
                Calendar.YEAR,
                time1 + 2000
            ) // Assuming time1 is in the format YY and represents a year after 2000
            set(
                Calendar.MONTH,
                time2 - 1
            ) // Calendar.MONTH is zero-based, so we need to subtract 1
            set(Calendar.DAY_OF_MONTH, time3)
            set(Calendar.HOUR_OF_DAY, time4) // 24-hour format, range 0-23
            set(Calendar.MINUTE, time5)
            set(Calendar.SECOND, time6)
            set(Calendar.MILLISECOND, 0) // Optionally set milliseconds to zero
        }

        val calTime = cal.timeInMillis
        Log.d(DATA, "calTime in milliseconds: $calTime")

        val zoneId = ZoneId.of("Asia/Seoul")
        val startTime: Long =
            LocalDateTime.of((time1 + 2000), time2, time3, time4, time5, time6).atZone(zoneId)
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
            Log.e(DATA, "Time Check Error")
            return -1
        }

        // 배터리
        val batteryLevel =
            java.lang.Byte.toUnsignedInt(data[14]) + (java.lang.Byte.toUnsignedInt(data[15]) / 100.0f * 100).roundToInt() / 100.0

        val decimalFormat = DecimalFormat("#.00")
        val battery = decimalFormat.format(batteryLevel)

        AppDatabase.getInstance(context)?.dataDao()?.insert(
            Data(
                time = startTime,
                deviceName = deviceName,
                dataType = "battery",
                dataValue = battery.toDouble()
            )
        )

        Log.d(DATA, "battery : $battery")

        // 온도
        val temperature =
            java.lang.Byte.toUnsignedInt(data[16]) + (java.lang.Byte.toUnsignedInt(data[17]) / 100.0f * 100).roundToInt() / 100.0

        AppDatabase.getInstance(context)?.dataDao()?.insert(
            Data(
                time = startTime,
                deviceName = deviceName,
                dataType = "temperature",
                dataValue = temperature
            )
        )

        Log.d(DATA, "temperature : $temperature")

        // weo1, weo2

        val we1a = data[18]
        val we1b = data[19]
        val we1c = data[20]
        val we2a = data[21]
        val we2b = data[22]
        val we2c = data[23]

        val weo1 = convertToCurrentDataDouble(we1a, we1b, we1c)
        val weo2 = convertToCurrentDataDouble(we2a, we2b, we2c)

        // nDataLength
        // val nDataLength =
        // (java.lang.Byte.toUnsignedInt(data[4]) - 10) / 6

        val findBufferWeoCount: Int = (data.size - 18) / 6
        Log.d("test", "findBufferWeoCount : $findBufferWeoCount")
        Log.d("test", "nDataLength : $nDataLength")
        for (i in 0 until findBufferWeoCount) {


            if (findBufferWeoCount > 5) {
            Log.d(
                DATA,
                "${i + 1}번째 : weo1 : " + convertToCurrentDataDouble(
                    data[18 + i * 6 + 0],
                    data[18 + i * 6 + 1],
                    data[18 + i * 6 + 2]
                ) + " weo2 : " + convertToCurrentDataDouble(
                    data[18 + i * 6 + 3],
                    data[18 + i * 6 + 4],
                    data[18 + i * 6 + 5]
                )
            )
            bufferWeo1 =
                convertToCurrentDataDouble(
                    data[18 + i * 6 + 0],
                    data[18 + i * 6 + 1],
                    data[18 + i * 6 + 2]
                )
            bufferWeo2 =
                convertToCurrentDataDouble(
                    data[18 + i * 6 + 3],
                    data[18 + i * 6 + 4],
                    data[18 + i * 6 + 5]
                )
            val time: Long = startTime + (1000 * 10 * i)

            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(context)?.dataDao()?.insert(
                    Data(
                        time = time,
                        deviceName = deviceName,
                        dataType = "weo1",
                        dataValue = bufferWeo1.toDouble()
                    )
                )

                AppDatabase.getInstance(context)?.dataDao()?.insert(
                    Data(
                        time = time,
                        deviceName = deviceName,
                        dataType = "weo2",
                        dataValue = bufferWeo2.toDouble()
                    )
                )

                AppDatabase.getInstance(context)?.dataDao()?.insertCsv(
                    Csv(
                        time = time,
                        deviceName = deviceName ?: "no device",
                        weo1 = bufferWeo1,
                        weo2 = bufferWeo2,
                        temperature = null,
                        battery = null
                    )
                )
            }


            // bufferWeo 값 테스트
            Log.d("DATA", "buffer테스트weo1 : $bufferWeo1")
            Log.d("DATA", "buffer테스트weo2 : $bufferWeo2")
            } else {
                Log.d(
                    DATA,
                    "${i + 1}번째 : weo1 : " + convertToCurrentDataDouble(
                        data[18 + i * 6 + 0],
                        data[18 + i * 6 + 1],
                        data[18 + i * 6 + 2]
                    ) + " weo2 : " + convertToCurrentDataDouble(
                        data[18 + i * 6 + 3],
                        data[18 + i * 6 + 4],
                        data[18 + i * 6 + 5]
                    )
                )
                bufferWeo1 =
                    convertToCurrentDataDouble(
                        data[18 + i * 6 + 0],
                        data[18 + i * 6 + 1],
                        data[18 + i * 6 + 2]
                    )
                bufferWeo2 =
                    convertToCurrentDataDouble(
                        data[18 + i * 6 + 3],
                        data[18 + i * 6 + 4],
                        data[18 + i * 6 + 5]
                    )
                val time: Long = startTime + (1000 * 10 * i)

                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance(context)?.dataDao()?.insert(
                        Data(
                            time = time,
                            deviceName = deviceName,
                            dataType = "weo1",
                            dataValue = bufferWeo1.toDouble()
                        )
                    )

                    AppDatabase.getInstance(context)?.dataDao()?.insert(
                        Data(
                            time = time,
                            deviceName = deviceName,
                            dataType = "weo2",
                            dataValue = bufferWeo2.toDouble()
                        )
                    )

                    AppDatabase.getInstance(context)?.dataDao()?.insertCsv(
                        Csv(
                            time = time,
                            deviceName = deviceName ?: "no device",
                            weo1 = bufferWeo1,
                            weo2 = bufferWeo2,
                            temperature = temperature,
                            battery = battery.toDouble()
                        )
                    )
                }
            }
        }

        val finalData = ProtocolData(
            stx1 = stx1, stx2 = stx2, command = commandId,
            status = status, length = length, reserved = reserved,
            time = calTime,
            temperature = temperature, battery = battery.toDouble(), count = count,
            deviceName = deviceName,
            weo1 = bufferWeo1, weo2 = bufferWeo2
        )

//        AppDatabase.getInstance(context)?.dataDao()?.insertCsv(
//            Csv(
//                time = startTime,
//                deviceName = deviceName ?: "no device",
//                weo1 = bufferWeo1,
//                weo2 = bufferWeo2,
//                temperature = temperature,
//                battery = battery.toDouble()
//            )
//        )

        Log.d("data", "cal byte time : ${finalData.time}")
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.KOREA
        ) // Use Locale.KOREA for Korean locale
        val readableDate = dateFormat.format(calTime)
        Log.d("data", "convert byte time : $readableDate")

        // 뷰모델에 데이터 업데이트
        viewModel.updateData(finalData)
        return 0
    }


    private fun convertToCurrentDataDouble(
        weoFirst: Byte,
        weoSecond: Byte,
        weoLast: Byte
    ): String {
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

        var formattedResult = String.format("%.2f", result)
        Log.e("FORMAT", "formattedResult : $formattedResult")


        // 결과 반환 ( -0.0nA가 값으로 나왔을 경우 0nA로 표기)
        if (result == -0.0) {
            return "0"
        } else {
            return formattedResult
        }
    }

    fun getDevice(): Device {
        val device = viewModel.getDevice()
        return device
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
}*/
