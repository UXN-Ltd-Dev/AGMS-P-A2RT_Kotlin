package kr.co.uxn.agms_p_a2rt.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.BleConnectionState
import kr.co.uxn.agms_p_a2rt.GuestList
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.notification.AlertChannel
import kr.co.uxn.agms_p_a2rt.notification.AppNotificationManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.UserValue
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
    val userDeviceId: Int,
    val applicationContext: Context,
    val protocol: Protocol
) : BluetoothGattCallback() {

    companion object {
        @Volatile
        private var INSTANCE: BleManager? = null

//        const val characteristicUuidReadT21 = "e093f3b5-00a3-a9e5-9eca-40026e0edc24"
//        const val characteristicUuidWriteT21 = "e093f3b5-00a3-a9e5-9eca-40036e0edc24"
//        const val serviceUuidT21 = "e093f3b5-00a3-a9e5-9eca-40016e0edc24"
        const val characteristicUuidReadF23 = "e093f3b5-00a3-a9e5-9eca-80026e0edc24"
        const val characteristicUuidWriteF23 = "e093f3b5-00a3-a9e5-9eca-80036e0edc24"
        const val serviceUuidF23 = "e093f3b5-00a3-a9e5-9eca-80016e0edc24"

        fun getInstance(
            context: Context,
            mac: String,
            userId: Int,
            userDeviceId: Int,
            applicationContext: Context,
            protocol: Protocol
        ): BleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BleManager(context, mac, userId, userDeviceId, applicationContext, protocol).also {
                    INSTANCE = it
                }
            }
        }

        const val TEST = "TEST"

        /** GATT 작업 한 건을 몇 번까지 다시 시도할지. */
        private const val MAX_GATT_OPERATION_ATTEMPTS = 3

        /** 재시도 사이에 두는 틈. 곧바로 다시 걸면 스택이 같은 이유로 또 거절한다. */
        private const val GATT_OPERATION_RETRY_DELAY_MS = 200L

        /**
         * 연결은 유지되는 것처럼 보이는데 이 시간 동안 패킷이 하나도 없으면
         * 죽은 연결로 보고 강제로 끊었다가 다시 붙는다.
         *
         * 스택이 끊김을 인지한 경우는 isDeviceActuallyConnected() 가 잡아내지만,
         * 스택은 연결됐다고 하는데 센서만 조용한 경우는 그것으로 잡히지 않는다.
         * 측정 간격이 1분이므로 10분이면 정상 상황에서 오작동할 여지가 없다.
         */
        private const val DATA_STALL_TIMEOUT_MS = 10 * 60 * 1000L

        /** 연결 시도가 이 시간 안에 끝나지 않으면 그 GATT 를 버리고 다시 건다. */
        private const val CONNECT_TIMEOUT_MS = 30_000L

        /** 끊긴 뒤 다시 걸기까지 두는 틈. */
        private const val RECONNECT_DELAY_MS = 3_000L

        private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }


    private var timerWhenConnected: Timer? = null
    private var timerTaskForConnected: TimerTask? = null

    lateinit var bufferWeo1: String
    lateinit var bufferWeo2: String


    /**
     * 연결을 유지할 생각인가. stopConnections() 로 내리고 resumeConnections() 로 올린다.
     *
     * 바깥에서 직접 쓰지 못하게 막아 둔다. 이 값만 바꾸고 GATT 는 그대로 두면
     * 둘이 어긋나기 때문이다.
     */
    @Volatile
    var isReconnect = true
        private set

    private enum class GattConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    /** 큐에 넣는 GATT 작업. 안드로이드 GATT 는 한 번에 하나만 받는다. */
    private sealed class GattOperation(open val label: String) {
        data class DescriptorWrite(
            val descriptor: BluetoothGattDescriptor,
            val value: ByteArray,
            override val label: String
        ) : GattOperation(label)

        data class CharacteristicWrite(
            val characteristic: BluetoothGattCharacteristic,
            val value: ByteArray,
            override val label: String
        ) : GattOperation(label)
    }

    /**
     * GATT 상태를 한 자물쇠 아래 둔다.
     *
     * 연결·해제·교체가 콜백 스레드와 서비스 스레드에서 동시에 들어온다. 전에는
     * companion 의 mGatt 를 서비스가 바깥에서 대입하기도 해서, 끊김 콜백이 두 번 오면
     * connectGatt() 가 두 번 불리고 GATT 객체가 누수됐다.
     */
    private val gattLock = Any()

    @Volatile
    private var gattConnectionState = GattConnectionState.DISCONNECTED
    private var currentGatt: BluetoothGatt? = null

    private val bleScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectJob: Job? = null
    private var connectTimeoutJob: Job? = null
    private val gattOperationQueue =
        GattOperationQueue<GattOperation>(MAX_GATT_OPERATION_ATTEMPTS)

    /**
     * 마지막으로 센서 패킷을 받은 시각. SystemClock.elapsedRealtime() 기준이라
     * 사용자가 기기 시계를 바꿔도 흔들리지 않는다.
     *
     * 0 이면 아직 연결된 적이 없다는 뜻이라 워치독이 동작하지 않는다.
     */
    @Volatile
    private var lastPacketAt: Long = 0L

//    lateinit var bluetoothDevice: BluetoothDevice
//    lateinit var bluetoothManager: BluetoothManager
//    lateinit var bluetoothAdpater: BluetoothAdapter


    val disconnectHandler = Handler(Looper.getMainLooper())
    val reconnectHandler = Handler(Looper.getMainLooper())

    fun resumeConnections() {
        synchronized(gattLock) {
            isReconnect = true
        }
    }

    /**
     * 블루투스 스택에 이 MAC 이 실제로 연결되어 있는지 직접 물어본다.
     *
     * gattConnectionState 는 onConnectionStateChange 콜백으로만 갱신되므로
     * 실제 상태와 어긋날 수 있다. 이 함수는 그 기억을 검증하는 용도다.
     *
     * 조회에 실패하면 true 를 돌려 판단을 보류한다. false 로 처리하면
     * 멀쩡히 붙어 있는 연결을 끊어 버릴 수 있어 보수적으로 둔다.
     */
    private fun isDeviceActuallyConnected(bluetoothManager: BluetoothManager): Boolean {
        return runCatching {
            bluetoothManager
                .getConnectedDevices(BluetoothProfile.GATT)
                .any { it.address.equals(mac, ignoreCase = true) }
        }.getOrElse { error ->
            Log.w(TEST, "실제 연결 상태 조회 실패. 판단을 보류한다.", error)
            true
        }
    }

    /**
     * 연결은 살아 있는 것으로 보이는데 오래도록 패킷이 없는 상태인지 본다.
     *
     * isDeviceActuallyConnected() 는 "스택도 끊긴 걸 아는" 경우를 잡는다.
     * 이 함수는 "스택은 연결됐다고 하는데 센서만 조용한" 경우를 잡는다. 층이 다르다.
     */
    private fun isDataStalled(): Boolean {
        val last = lastPacketAt
        if (last == 0L) return false // 아직 연결된 적이 없다. 판단할 근거가 없다.
        return SystemClock.elapsedRealtime() - last >= DATA_STALL_TIMEOUT_MS
    }

    /**
     * 연결을 거는 **유일한 입구**.
     *
     * 서비스·블루투스 켜짐·재연결 예약이 모두 이 문을 지난다. 전에는 세 군데가 각자
     * connectGatt() 를 불러서, 이미 붙어 있는데 또 거는 일을 막을 방법이 없었다.
     *
     * 주기적으로 불리므로 워치독도 여기서 함께 돈다. 별도 타이머가 필요 없다.
     */
    @SuppressLint("MissingPermission")
    fun ensureConnected() {
        if (!isReconnect) {
            Log.d(TEST, "BLE connection is disabled. Skip connectGatt().")
            return
        }

        val stalledGatt = synchronized(gattLock) {
            if (gattConnectionState == GattConnectionState.CONNECTED && isDataStalled()) {
                currentGatt
            } else {
                null
            }
        }
        if (stalledGatt != null) {
            val idleMinutes = (SystemClock.elapsedRealtime() - lastPacketAt) / 60_000
            Log.w(TEST, "연결은 유지되는데 ${idleMinutes}분째 패킷이 없다. 강제로 끊고 재연결한다.")
            restartGattAfterOperationFailure(stalledGatt, "데이터 수신 끊김 (${idleMinutes}분)")
            return
        }

        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // 어댑터가 내려가면 OS 레벨에서 GATT 링크가 모두 사라지므로 currentGatt 는
            // 껍데기다. 그대로 두면 상태가 CONNECTED 로 굳어 재연결 가드에 계속 걸린다.
            synchronized(gattLock) {
                // 락을 잡은 뒤 다시 확인한다. 그 사이 블루투스가 켜지고 다른 스레드가
                // 새 GATT 를 만들었을 수 있는데, 그것까지 닫으면 방금 살아난 연결을 죽인다.
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    runCatching { currentGatt?.close() }
                    currentGatt = null
                    gattConnectionState = GattConnectionState.DISCONNECTED
                }
            }
            Log.d(TEST, "Bluetooth is disabled. Skip connectGatt().")
            return
        }

        var createdGatt: BluetoothGatt? = null
        try {
            val bluetoothDevice = if (Build.VERSION.SDK_INT >= TIRAMISU) {
                bluetoothAdapter.getRemoteLeDevice(mac, BluetoothDevice.ADDRESS_TYPE_PUBLIC)
            } else {
                bluetoothAdapter.getRemoteDevice(mac)
            }

            synchronized(gattLock) {
                // 위의 빠른 검사는 참고일 뿐이다. stopConnections() 와 같은 락 안에서
                // 다시 확인해야, 멈추라는 지시 뒤에 새 연결이 걸리는 일을 막을 수 있다.
                if (!isReconnect) {
                    Log.d(TEST, "BLE connection was disabled while preparing GATT.")
                    return
                }

                // gattConnectionState 는 앱이 기억하는 값일 뿐 실제 연결 상태가 아니다.
                // 센서가 조용히 사라지면(배터리 방전, 범위 밖, 착용 이탈) 안드로이드가
                // STATE_DISCONNECTED 를 주지 않는 경우가 있는데, 이 값을 되돌리는 코드가
                // 그 콜백 안에만 있다. 그러면 상태가 CONNECTED 로 굳어 스스로 복구할 수
                // 없게 된다. 그래서 가드에 걸리기 전에 스택에 직접 물어본다.
                if (gattConnectionState == GattConnectionState.CONNECTED &&
                    !isDeviceActuallyConnected(bluetoothManager)
                ) {
                    Log.w(TEST, "상태는 CONNECTED 인데 스택에는 연결이 없다. 초기화하고 재연결한다.")
                    runCatching { currentGatt?.close() }
                    currentGatt = null
                    gattConnectionState = GattConnectionState.DISCONNECTED
                    clearGattOperationQueue("끊긴 CONNECTED 상태 정리")
                }

                if (gattConnectionState == GattConnectionState.CONNECTING ||
                    gattConnectionState == GattConnectionState.CONNECTED
                ) {
                    Log.d(TEST, "GATT is already $gattConnectionState. Skip duplicate connection.")
                    return
                }

                runCatching { currentGatt?.close() }
                connectTimeoutJob?.cancel()
                connectTimeoutJob = null
                currentGatt = null
                clearGattOperationQueue("새 GATT 연결 시작")
                gattConnectionState = GattConnectionState.CONNECTING

                createdGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bluetoothDevice.connectGatt(context, false, this, BluetoothDevice.TRANSPORT_LE)
                } else {
                    bluetoothDevice.connectGatt(context, false, this)
                }
                currentGatt = createdGatt
            }

            BleBridge.updateState(BleConnectionState.CONNECTING)

            val started = createdGatt
            if (started == null) {
                synchronized(gattLock) {
                    if (currentGatt == null &&
                        gattConnectionState == GattConnectionState.CONNECTING
                    ) {
                        gattConnectionState = GattConnectionState.DISCONNECTED
                    }
                }
                scheduleReconnect()
            } else {
                scheduleConnectionTimeout(started)
            }
        } catch (e: Exception) {
            val attemptedGatt = createdGatt
            val failedGatt = synchronized(gattLock) {
                when {
                    attemptedGatt != null && currentGatt === attemptedGatt -> {
                        currentGatt = null
                        gattConnectionState = GattConnectionState.DISCONNECTED
                        attemptedGatt
                    }

                    attemptedGatt == null &&
                        currentGatt == null &&
                        gattConnectionState == GattConnectionState.CONNECTING -> {
                        gattConnectionState = GattConnectionState.DISCONNECTED
                        null
                    }

                    else -> null
                }
            }
            runCatching { failedGatt?.close() }
            Log.e(TEST, "connectGatt() failed: ${e.message}", e)
            scheduleReconnect()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopConnections() {
        val gattToClose: BluetoothGatt?
        synchronized(gattLock) {
            // 멈춤 정책과 GATT 비우기를 한 묶음으로 publish 한다.
            isReconnect = false
            reconnectJob?.cancel()
            reconnectJob = null
            connectTimeoutJob?.cancel()
            connectTimeoutJob = null
            gattConnectionState = GattConnectionState.DISCONNECTING
            gattToClose = currentGatt
            currentGatt = null
        }
        clearGattOperationQueue("BLE 연결 중지")

        runCatching { gattToClose?.disconnect() }
        runCatching { gattToClose?.close() }

        synchronized(gattLock) {
            // 락 밖에서 닫는 사이 resumeConnections() 가 새 연결을 만들었다면 덮지 않는다.
            if (!isReconnect && currentGatt == null) {
                gattConnectionState = GattConnectionState.DISCONNECTED
            }
        }
    }

    /** 이 콜백이 지금 쓰는 GATT 에서 온 것인가. 낡은 객체의 콜백을 걸러낸다. */
    private fun isCurrentGatt(gatt: BluetoothGatt?): Boolean {
        return gatt != null && synchronized(gattLock) { currentGatt === gatt }
    }

    private fun scheduleReconnect() {
        synchronized(gattLock) {
            // 이미 예약돼 있으면 두 번 걸지 않는다. 끊김 콜백이 연달아 오는 일이 있다.
            if (!isReconnect || reconnectJob?.isActive == true) return

            reconnectJob = bleScope.launch {
                delay(RECONNECT_DELAY_MS)

                val shouldReconnect = synchronized(gattLock) {
                    reconnectJob = null
                    isReconnect
                }
                if (!shouldReconnect) return@launch

                withContext(Dispatchers.Main) {
                    BleBridge.updateState(BleConnectionState.CONNECTING)
                }
                ensureConnected()
            }
        }
    }

    /**
     * 연결 시도에 제한 시간을 건다.
     *
     * connectGatt() 는 결과를 콜백으로만 알린다. 그 콜백이 영영 오지 않으면 상태가
     * CONNECTING 에 머물러 다음 시도가 가드에 막힌다 - 스스로 못 빠져나온다.
     */
    @SuppressLint("MissingPermission")
    private fun scheduleConnectionTimeout(gatt: BluetoothGatt) {
        synchronized(gattLock) {
            if (currentGatt !== gatt ||
                gattConnectionState != GattConnectionState.CONNECTING
            ) {
                return
            }

            connectTimeoutJob?.cancel()
            connectTimeoutJob = bleScope.launch {
                delay(CONNECT_TIMEOUT_MS)

                val shouldReconnect = synchronized(gattLock) {
                    if (currentGatt === gatt &&
                        gattConnectionState == GattConnectionState.CONNECTING
                    ) {
                        currentGatt = null
                        gattConnectionState = GattConnectionState.DISCONNECTED
                        connectTimeoutJob = null
                        isReconnect
                    } else {
                        connectTimeoutJob = null
                        false
                    }
                }

                if (shouldReconnect) {
                    Log.w(TEST, "GATT connection timed out. Reconnecting.")
                    runCatching { gatt.close() }
                    scheduleReconnect()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        // 낡은 GATT 의 콜백은 여기서 끊는다. 닫히지 않은 채 남으면 그 객체가 계속
        // 콜백을 던져 현재 연결 상태를 덮어쓴다.
        if (!isCurrentGatt(gatt)) {
            // 민감정보 로그 - 주석 처리 (침투시험: 원시 측정값·임피던스·MAC 노출)
            // Log.w(TEST, "Ignore callback from stale GATT: ${gatt?.device?.address}")
            runCatching { gatt?.close() }
            return
        }

        // 민감정보 로그 - 주석 처리 (침투시험: 원시 측정값·임피던스·MAC 노출)
        // Log.d("GATT", "status=$status, newState=$newState, device=${gatt?.device?.address}")
        Log.e(TEST, "onConnectionStateChange 진입")
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.e("gatt", "gatt connected!")

                val connectionAccepted = synchronized(gattLock) {
                    if (!isReconnect || currentGatt !== gatt) {
                        false
                    } else {
                        gattConnectionState = GattConnectionState.CONNECTED
                        reconnectJob?.cancel()
                        reconnectJob = null
                        connectTimeoutJob?.cancel()
                        connectTimeoutJob = null
                        true
                    }
                }

                if (!connectionAccepted) {
                    Log.w(TEST, "Ignore connected callback after BLE connections were stopped.")
                    runCatching { gatt?.disconnect() }
                    runCatching { gatt?.close() }
                    return
                }

                // 워치독 기준점을 연결 시점으로 잡는다. 이 시각 이후 10분 동안 패킷이
                // 하나도 없으면 죽은 연결로 본다.
                lastPacketAt = SystemClock.elapsedRealtime()

                applyBondPolicy(gatt?.device)


                // BleBridge에 상태 연결 완료 전송
                BleBridge.updateState(BleConnectionState.CONNECTED)

                // mtu 517로 요청
                gatt?.requestMtu(517)

                // 신호소실 노티 제거
                NotificationManagerCompat.from(context).cancel(92)

            }

            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.e("gatt", "gatt disconnected!!!")

                var disconnectionAccepted = false
                val shouldReconnect = synchronized(gattLock) {
                    if (currentGatt !== gatt) {
                        false
                    } else {
                        disconnectionAccepted = true
                        currentGatt = null
                        gattConnectionState = GattConnectionState.DISCONNECTED
                        connectTimeoutJob?.cancel()
                        connectTimeoutJob = null
                        isReconnect
                    }
                }

                if (!disconnectionAccepted) {
                    Log.w(TEST, "Ignore disconnected callback from a GATT that is no longer active.")
                    runCatching { gatt?.close() }
                    return
                }

                clearGattOperationQueue("GATT 연결 해제")
                refreshDeviceCache(gatt)
                runCatching { gatt?.close() }

                if (!shouldReconnect) {
                    BleBridge.updateState(BleConnectionState.DISCONNECTED)
                    return
                }

                // 끊김 알림
                CoroutineScope(Dispatchers.IO).launch {
                    val isShowBleNoti = DataStoreManager.getNotiLostSignal().first() ?: true
                    Log.e("BLE", "BLE 연결 끊김 알림 DS로부터 불러온 설정값 : $isShowBleNoti")
                    if (isShowBleNoti) {
                        withContext(Dispatchers.Main) {
                            sendNotification(
                                context,
                                context.getString(R.string.notification_disconnect_ble),
                                "",
                                92
                            )
                            BleBridge.showBleConnectDialog(true)
                        }
                    }
                }

                // BleBridege에 Disconnected 상태 전송
                BleBridge.updateState(BleConnectionState.DISCONNECTED)

                Log.d(TEST, "disconnect 콜백 내부 : ${gatt?.device?.name}")

                // 재연결은 예약으로만 건다. 여기서 직접 connectGatt() 를 부르면
                // 끊김 콜백이 두 번 올 때 연결이 두 번 생긴다.
                scheduleReconnect()
            }


            BluetoothProfile.STATE_CONNECTING -> {

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
        // gatt 를 여기서 직접 본다. isCurrentGatt 도 null 이면 false 라 판정은
        // 같지만, 그 사실이 컴파일러에 전달되지 않아 아래에서 !! 가 필요해졌다.
        if (gatt == null || !isCurrentGatt(gatt)) {
            Log.w(TEST, "Ignore services from stale GATT")
            return
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TEST, "가트 연결이 되었고, onServicesDiscovered에 진입!")
            // ==== 서비스 조회 ====
            val services = gatt.services
            // gatt.services 는 자바에서 온 플랫폼 타입이라 Kotlin 이 null 검사를
            // 넣어 주지 않는다. 바로 순회하면 null 일 때 그 자리에서 터지는데,
            // 여기는 시스템이 부르는 콜백이라 감싸는 try/catch 가 없다.
            // 빈 목록도 함께 걸러 둔다. 그래야 아래 'UUID 미발견'과 사유가 갈린다.
            if (services.isNullOrEmpty()) {
                restartGattAfterOperationFailure(gatt, "서비스 목록이 비었음")
                return
            }

            services.forEach { discovered ->
                Log.d(TEST, "Service UUID: ${discovered.uuid}")
                discovered.characteristics.forEach { characteristic ->
                    Log.d(TEST, "Characteristic UUID: ${characteristic.uuid}")
                }
            }

//            val characteristicUUID = UUID.fromString(characteristicUuidReadF23)
            val characteristicUUID = UUID.fromString(protocol.readUUID)

            // 원하는 서비스를 찾는다. **찾기 전에는 쓰지 않는다.**
            //
            // 전에는 lateinit var service 를 두고 while 로 훑은 뒤, 못 찾았어도
            // service.getCharacteristic() 을 먼저 부르고 그 다음에 !found 를 봤다.
            // 순서가 거꾸로여서 UUID 를 못 찾거나 목록이 비면 그 자리에서
            // UninitializedPropertyAccessException 이 났다. 여기는 시스템이 부르는
            // 콜백이라 감싸는 try/catch 가 없어 그대로 프로세스가 죽는다.
            val service = services.firstOrNull {
                it.uuid.toString().equals(protocol.serviceUUID, ignoreCase = true)
            }
            if (service == null) {
                restartGattAfterOperationFailure(gatt, "필수 service 없음")
                return
            }

            val readCharacteristic = service.getCharacteristic(characteristicUUID)
            if (readCharacteristic == null) {
                restartGattAfterOperationFailure(gatt, "notify characteristic 없음")
                return
            }

            val notificationValue = when {
                readCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 ->
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                readCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ->
                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE

                else -> {
                    restartGattAfterOperationFailure(
                        gatt,
                        "read characteristic가 notify/indicate를 지원하지 않음"
                    )
                    return
                }
            }

            if (!gatt.setCharacteristicNotification(readCharacteristic, true)) {
                restartGattAfterOperationFailure(gatt, "로컬 notification 활성화 실패")
                return
            }

            // **CCCD 를 써야 센서가 실제로 보내기 시작한다.** 전에는 로컬 플래그만
            // 켜고 디스크립터를 쓰지 않아, 기기에 따라 데이터가 오지 않을 수 있었다.
            val cccd = readCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
            if (cccd == null) {
                restartGattAfterOperationFailure(gatt, "CCCD descriptor 없음")
                return
            }

            enqueueGattOperation(
                gatt,
                GattOperation.DescriptorWrite(
                    descriptor = cccd,
                    value = notificationValue,
                    label = "CCCD_ENABLE"
                )
            )

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_loading_data),
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            Log.e(TEST, "서비스 탐색 실패 status=$status")
            restartGattAfterOperationFailure(gatt, "서비스 탐색 실패 status=$status")
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)

        if (!isCurrentGatt(gatt)) {
            Log.w(TEST, "Ignore data from stale GATT")
            return
        }
        val callbackGatt = gatt ?: return

        // 워치독 기준점. 센서가 살아 있다는 유일한 증거다.
        lastPacketAt = SystemClock.elapsedRealtime()

        val data = characteristic?.value
        if (data == null || data.isEmpty()) {
            Log.w(TEST, "빈 패킷이 왔다. 버린다.")
            return
        }

        val response = protocol.runProtocol(AppDatabase.getInstance(applicationContext), data, userId, userDeviceId)
        if (response != null) {
            enqueueAck(callbackGatt, response)
        } else {
            Log.e(TEST, " response is null")
        }
//        val deviceName = gatt?.device?.name
//
//        Log.d(TEST, "commandId  : ${String.format("0x%02X", data[2])}")
//        Log.d(TEST, "status  : ${String.format("0x%02X", data[3])}")
//        Log.e(TEST, "데이터 size : ${data.size}")
//        Log.e(TEST, "data[4] length : ${java.lang.Byte.toUnsignedInt(data[4]) + 8}")
//
//        // Check CRC Code
//        val crc1 = java.lang.Byte.toUnsignedInt(data[6])
//        val crc2 = java.lang.Byte.toUnsignedInt(data[7])
//        Log.d(TEST, "crc1 : $crc1, crc2 : $crc2")
//
//        val crc = (crc1 * 256) + crc2
//        Log.d(TEST, "수신된 crc : $crc")
//
//        val checkCrc = checkCrc(data)
//        Log.d(TEST, "계산된 crc : $checkCrc")
//
//        // crc 안 맞으면 crc 에러 응답
//        if (crc != checkCrc) {
//            Log.e(TEST, "crc 에러 발생!")
//            writeAgms(gatt!!, makeByteArrayWithState(0x01.toByte())) // crc_error
//            return
//        } else {
//            Log.e(TEST, "=== crc 체크 통과! ===")
//        }
//
//        if (data[0] == 0xA0.toByte() && data[1] == 0x81.toByte()) {
//            Log.d(
//                TEST,
//                "data[0] : ${String.format("0x%02X", data[0])},  0xA0\n" +
//                        "data[1] : ${String.format("0x%02X", data[1])},  0x81"
//            )
//            val commandId = data[2]
//            Log.d(TEST, "data[2] commandId converted = ${0x42.toByte()}")
//            Log.d(TEST, "data[2] commandId = ${data[2]}")
//
//            when (commandId) {
//                // CMD_SEND (0x42)
//                0x42.toByte() -> {
//                    val length = java.lang.Byte.toUnsignedInt(data[4])
//                    val calLength = 8 + length
//                    if (data.size != calLength) {
//                        Log.e(
//                            TEST,
//                            "데이터 길이 맞지 않음\nlength = ${data.size}\ncal_length = ${calLength}"
//                        )
//                        writeAgms(
//                            gatt!!,
//                            makeByteArrayWithState(0x02.toByte())
//                        ) // date_length_error
//                    } else {
//                        // 데이터 받아오기
//                        Log.e(
//                            TEST,
//                            "=== 데이터 길이 일치! === \nlength = ${data.size}\ncal_length = ${calLength}"
//                        )
//                        try {
//                            val checker = receiveDataAndInsert(data, deviceName)
//                            if (checker == -1) {
//                                // 0x12 : time error
//                                writeAgms(gatt!!, makeByteArrayWithState(0x12.toByte()))
//                            } else {
//                                // 0x00 : no error (OK)
//                                writeAgms(gatt!!, makeByteArrayWithState(0x00.toByte()))
//                            }
//
//                        } catch (e: Exception) {
//                            Log.e(TEST, "데이터 받아오기 에러발생")
//                            CoroutineScope(Dispatchers.Main).launch {
//                                Log.e(TEST, "프로토콜2.0이 아님")
//                                Log.e(TEST, "error : ${e.message}")
//                                Toast.makeText(
//                                    context,
//                                    "해당 기기는 프로토콜2.0이 아닙니다.\n다른 기기를 선택해 주세요.",
//                                    Toast.LENGTH_LONG
//                                ).show()
////                                isChange = true
//
//                                gatt?.disconnect()
////                                eventViewModel.goBleList(0, true)
//                            }
//                        }
//                    }
//                }
//
//                // CMD_RTC (0x41)
//                0x41.toByte() -> {
//                    // sendRTC
//                    val state = data[3]
//                    Log.d(TEST, "state = ${String.format("0x%02X", data[3])}")
//                    if (state == 0x11.toByte() || state == 0x12.toByte()) {
//                        // write 기능
//                        writeAgms(gatt!!, sendRtc())
//                        Log.d(TEST, "SEND_RTC!!!")
//                        //    return
//                    }
//                }
//            }
//        } else {
//            Log.e(TEST, "start code Error!")
//        }


    }

    @SuppressLint("MissingPermission")
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        gatt?.discoverServices()
    }

    /**
     * 센서가 보낸 패킷에 대한 ACK 를 큐에 넣는다.
     *
     * 곧바로 writeCharacteristic() 을 부르지 않는다. 패킷은 몰아서 오는데 GATT 는
     * 한 번에 한 작업만 받으므로, 직접 부르면 둘째 것부터 조용히 거절당한다.
     */
    private fun enqueueAck(gatt: BluetoothGatt, value: ByteArray) {
        val writeCharacteristic = gatt
            .getService(UUID.fromString(protocol.serviceUUID))
            ?.getCharacteristic(UUID.fromString(protocol.writeUUID))

        if (writeCharacteristic == null) {
            restartGattAfterOperationFailure(gatt, "ACK write characteristic 없음")
            return
        }

        val command = value.getOrNull(2)?.toUByte()?.toString() ?: "unknown"
        val status = value.getOrNull(3)?.toUByte()?.toString() ?: "unknown"
        enqueueGattOperation(
            gatt,
            GattOperation.CharacteristicWrite(
                characteristic = writeCharacteristic,
                value = value.copyOf(),
                label = "ACK cmd=$command status=$status"
            )
        )
    }

    // ── GATT 작업 큐 ────────────────────────────────────────────────────
    //
    // 안드로이드 GATT 는 한 번에 한 작업만 받는다. 앞 작업이 끝나기 전에 또 걸면
    // 조용히 거절당하고, 거절당한 줄도 모른 채 응답만 기다리게 된다. 그래서 한 줄로
    // 세워 하나씩 보내고, 실패하면 세 번까지 다시 시도한다.

    @SuppressLint("MissingPermission")
    private fun enqueueGattOperation(gatt: BluetoothGatt, operation: GattOperation) {
        reconnectHandler.post {
            if (!isCurrentGatt(gatt)) return@post

            val queueSize = gattOperationQueue.enqueue(operation)
            Log.d("GATT_QUEUE", "ENQUEUE label=${operation.label} queueSize=$queueSize")
            executeNextGattOperation(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun executeNextGattOperation(gatt: BluetoothGatt) {
        if (!isCurrentGatt(gatt)) return

        val attempt = gattOperationQueue.nextAttempt() ?: return
        val operation = attempt.operation
        Log.d(
            "GATT_QUEUE",
            "START label=${operation.label} attempt=${attempt.number} queueSize=${gattOperationQueue.size()}"
        )

        val started = when (operation) {
            is GattOperation.DescriptorWrite -> startDescriptorWrite(gatt, operation)
            is GattOperation.CharacteristicWrite -> startCharacteristicWrite(gatt, operation)
        }

        if (!started) {
            handleGattOperationFailure(gatt, operation, "write 시작 거절")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDescriptorWrite(
        gatt: BluetoothGatt,
        operation: GattOperation.DescriptorWrite
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= TIRAMISU) {
            val result = gatt.writeDescriptor(operation.descriptor, operation.value)
            Log.d("GATT_QUEUE", "START_RESULT label=${operation.label} result=$result")
            result == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            operation.descriptor.value = operation.value
            @Suppress("DEPRECATION")
            val result = gatt.writeDescriptor(operation.descriptor)
            Log.d("GATT_QUEUE", "START_RESULT label=${operation.label} result=$result")
            result
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCharacteristicWrite(
        gatt: BluetoothGatt,
        operation: GattOperation.CharacteristicWrite
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= TIRAMISU) {
            val result = gatt.writeCharacteristic(
                operation.characteristic,
                operation.value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
            Log.d("GATT_QUEUE", "START_RESULT label=${operation.label} result=$result")
            result == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            operation.characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            @Suppress("DEPRECATION")
            operation.characteristic.value = operation.value
            @Suppress("DEPRECATION")
            val result = gatt.writeCharacteristic(operation.characteristic)
            Log.d("GATT_QUEUE", "START_RESULT label=${operation.label} result=$result")
            result
        }
    }

    private fun finishGattOperation(
        gatt: BluetoothGatt,
        operation: GattOperation,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            gattOperationQueue.completeSuccess()
            Log.i(
                "GATT_QUEUE",
                "COMPLETE label=${operation.label} status=$status remaining=${gattOperationQueue.size()}"
            )
            executeNextGattOperation(gatt)
        } else {
            handleGattOperationFailure(gatt, operation, "완료 콜백 실패 status=$status")
        }
    }

    private fun handleGattOperationFailure(
        gatt: BluetoothGatt,
        operation: GattOperation,
        reason: String
    ) {
        when (gattOperationQueue.completeFailure()) {
            GattOperationQueue.FailureAction.RETRY -> {
                Log.w("GATT_QUEUE", "RETRY label=${operation.label} reason=$reason")
                reconnectHandler.postDelayed(
                    { executeNextGattOperation(gatt) },
                    GATT_OPERATION_RETRY_DELAY_MS
                )
            }

            GattOperationQueue.FailureAction.EXHAUSTED -> {
                Log.e("GATT_QUEUE", "EXHAUSTED label=${operation.label} reason=$reason")
                restartGattAfterOperationFailure(gatt, "$reason (${operation.label})")
            }
        }
    }

    private fun clearGattOperationQueue(reason: String) {
        gattOperationQueue.clear()
        Log.d("GATT_QUEUE", "CLEAR reason=$reason")
    }

    /**
     * GATT 가 더 쓸 수 없는 상태라 판단되면 끊고 처음부터 다시 붙는다.
     *
     * 작업이 재시도를 다 쓴 경우, 필수 특성이 없는 경우, 데이터가 끊긴 경우가 모두
     * 이리로 온다. 복구 경로를 하나로 모아 두어야 어디서 왔든 같은 상태로 정리된다.
     */
    @SuppressLint("MissingPermission")
    private fun restartGattAfterOperationFailure(gatt: BluetoothGatt, reason: String) {
        Log.e("GATT_QUEUE", "RECONNECT reason=$reason")
        val shouldReconnect = synchronized(gattLock) {
            if (currentGatt !== gatt) {
                false
            } else {
                currentGatt = null
                gattConnectionState = GattConnectionState.DISCONNECTED
                connectTimeoutJob?.cancel()
                connectTimeoutJob = null
                isReconnect
            }
        }
        clearGattOperationQueue(reason)

        runCatching { gatt.disconnect() }
        runCatching { gatt.close() }
        BleBridge.updateState(BleConnectionState.DISCONNECTED)
        if (shouldReconnect) scheduleReconnect()
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        val callbackGatt = gatt ?: return
        if (!isCurrentGatt(callbackGatt)) return

        reconnectHandler.post {
            val operation = gattOperationQueue.inFlightOperation()
            if (operation !is GattOperation.DescriptorWrite ||
                operation.descriptor.uuid != descriptor?.uuid
            ) {
                Log.w(TEST, "예상하지 않은 descriptor write callback uuid=${descriptor?.uuid}")
                return@post
            }
            finishGattOperation(callbackGatt, operation, status)
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        val callbackGatt = gatt ?: return
        if (!isCurrentGatt(callbackGatt)) return

        reconnectHandler.post {
            val operation = gattOperationQueue.inFlightOperation()
            if (operation !is GattOperation.CharacteristicWrite ||
                operation.characteristic.uuid != characteristic?.uuid
            ) {
                Log.w(TEST, "예상하지 않은 characteristic write callback uuid=${characteristic?.uuid}")
                return@post
            }
            finishGattOperation(callbackGatt, operation, status)
        }
    }

    /**
     * 서비스가 내려갈 때 모두 정리한다.
     *
     * GATT 는 stopConnections() 가 닫는다. 여기서 또 닫으면 같은 객체를 두 번 닫게
     * 되고, 그 사이 다른 스레드가 만든 연결을 지울 위험도 있다.
     */
    fun shutdown() {
        stopConnections()

        reconnectHandler.removeCallbacksAndMessages(null)
        disconnectHandler.removeCallbacksAndMessages(null)
        timerTaskForConnected?.cancel()
        timerTaskForConnected = null
        timerWhenConnected?.cancel()
        timerWhenConnected = null

        BleBridge.updateState(BleConnectionState.DISCONNECTED)
        INSTANCE = null
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


//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun receiveDataAndInsert(data: ByteArray, deviceName: String?): Int {
//
//        // 리스트 생성
//        val saveData: MutableList<UserValue> = mutableListOf<UserValue>()
//
//        // 데이터 받아오기
//        val stx1 = data[0]
//        val stx2 = data[1]
//        Log.d(TEST, "stx1 : ${stx1}, stx2: ${stx2}")
//
//        val commandId = data[2]
//        val status = data[3]
//        Log.d(TEST, "commandId : ${commandId}")
//        // printf("%02X\n", 10);   // 출력 (앞의 빈자리를 0으로 채우기): 0A
//        Log.d(TEST, "status : ${status}")
//        // 왜 0x%02X라는 작업을 포맷하는가? 오는 파일이 16진수인가?
//
//        Log.d(TEST, "data의 size : ${data.size}")
//        val length = data[4]
//        Log.d("data", "length : $length")
//
//        val nDataLength =
//            (java.lang.Byte.toUnsignedInt(data[4]) - 10) / 6
//
//        val reserved = data[5]
//        Log.d(TEST, "reversed : $reserved")
//
//        val chc1 = data[6]
//        val chc2 = data[7]
//        Log.d(TEST, "chc1 : $chc1, chc2 : $chc2")
//
//        val year = data[8].toInt()
//        val month = data[9].toInt()
//        val day = data[10].toInt()
//        val hour = data[11].toInt()
//        val min = data[12].toInt()
//        val sec = data[13].toInt()
//
//        var lastWeo1 = 0.0
//
//        Log.d(TEST, "time : 20${year}년 ${month}월 ${day}일 ${hour}시 ${min}분 ${sec}초")
//
//        val cal = Calendar.getInstance().apply {
//            set(
//                Calendar.YEAR,
//                year + 2000
//            ) // Assuming time1 is in the format YY and represents a year after 2000
//            set(
//                Calendar.MONTH,
//                month - 1
//            ) // Calendar.MONTH is zero-based, so we need to subtract 1
//            set(Calendar.DAY_OF_MONTH, day)
//            set(Calendar.HOUR_OF_DAY, hour) // 24-hour format, range 0-23
//            set(Calendar.MINUTE, min)
//            set(Calendar.SECOND, sec)
//            set(Calendar.MILLISECOND, 0) // Optionally set milliseconds to zero
//        }
//
//        val calTime = cal.timeInMillis
//        Log.d(TEST, "calTime in milliseconds: $calTime")
//
//        val zoneId = ZoneId.of("Asia/Seoul")
//        val startTime: Long =
//            LocalDateTime.of((year + 2000), month, day, hour, min, sec).atZone(zoneId)
//                .toInstant().toEpochMilli()
//        Log.d("time", "startTime : $startTime")
//
//        val now = LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli()
//        Log.d("time", "now : $now")
//
//        val lastTime: Long =
//            startTime + ((1000 * 10).toLong() * (nDataLength - 1))
//        Log.d("time", "lastTime : $lastTime")
//
//        // 타입 오차 허용값 = 10분
//        val checkTime: Long = now + (60 * 1000 * 10)
//        Log.d("time", "checkTime : $checkTime")
//
//        // time error check
//        if (lastTime > checkTime) {
//            Log.e(TEST, "Time Check Error")
//            return -1
//        }
//
//        // 배터리
//        val batteryLevel =
//            java.lang.Byte.toUnsignedInt(data[14]) + (java.lang.Byte.toUnsignedInt(data[15]) / 100.0f * 100).roundToInt() / 100.0
//
//        val decimalFormat = DecimalFormat("#.00")
//        val battery = decimalFormat.format(batteryLevel)
//
////        val createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//        val createdAt = Instant.ofEpochMilli(lastTime)
//            .atZone(ZoneId.of("Asia/Seoul"))
//            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//
//        Log.d(TEST, "battery : $battery")
//
//        // 온도
//        val temperature =
//            java.lang.Byte.toUnsignedInt(data[16]) + (java.lang.Byte.toUnsignedInt(data[17]) / 100.0f * 100).roundToInt() / 100.0
//
//        Log.d(TEST, "temperature : $temperature")
//
//        // weo1, weo2
//        val findBufferWeoCount: Int = (data.size - 18) / 6
//        Log.d(TEST, "findBufferWeoCount : $findBufferWeoCount")
//        Log.d(TEST, "nDataLength : $nDataLength")
//        for (i in 0 until findBufferWeoCount) {
//            val nWHigh: Int = 18 + i * 6
//            val nWMiddle: Int = 19 + i * 6
//            val nWLow: Int = 20 + i * 6
////            Log.e(TEST, "WE Address : $nWHigh : $nWMiddle : $nWLow")
//
//            val nAHigh: Int = 21 + i * 6
//            val nAMiddle: Int = 22 + i * 6
//            val nALow: Int = 23 + i * 6
////            Log.e(TEST, "AE Address : " + nAHigh + " : " + nAMiddle + " : " + nALow)
//            val time: Long = startTime + (1000 * 10 * i)
//
//            val weCurrent: Double =
//                convertToCurrentDataDouble(data[nWHigh], data[nWMiddle], data[nWLow])
//            val aeCurrent: Double =
//                convertToCurrentDataDouble(data[nAHigh], data[nAMiddle], data[nALow])
//
//            Log.e(TEST, "WE_Current " + i + ": " + weCurrent)
//            Log.e(TEST, "AE_Current " + i + ": " + aeCurrent)
////            Log.e("PYTHON", "Glucose " + i + ": " + "${PythonManager.instance.calculationGlucose(time / 1000, weCurrent, aeCurrent)}")
//
//            val convertedTime = Instant.ofEpochMilli(time)
//                .atZone(ZoneId.of("Asia/Seoul"))
//                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//
//            saveData.add(
//                UserValue(
//                    userId = userId,
//                    weCurrent = weCurrent,
//                    aeCurrent = aeCurrent,
//                    createdAt = convertedTime,
//                    createdAtLong = time
//                )
//            )
//            if (i == findBufferWeoCount - 1) {
//                lastWeo1 = weCurrent
//            }
//        }
//
//        // ui에 weo1 값 실시간 갱신
////        BleBridge.updateWeo1(lastWeo1)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            AppDatabase.getInstance(context)?.dataDao()?.insertUserValue(saveData)
//        }
//
//
//        return 0
//    }

    /**
     * 로그인한 계정에 따라 본딩 여부를 정한다.
     *
     * 게스트 계정(GuestList)으로 로그인했을 때만 본딩하지 않는다. 그 밖에는 본딩한다.
     * 로그인 전이라 이메일이 없으면 게스트가 아닌 것으로 보고 본딩한다 - BleManager 는
     * 로그인 뒤에 도는 것이 정상이라 이 경우는 드물다.
     *
     * 이미 있는 본딩은 지우지 않는다. 지우려면 공개 API 가 없어 리플렉션으로
     * removeBond 를 불러야 하는데, 연결된 상태에서 부르면 링크가 끊기고 사용자에게는
     * 영문 모를 "신호 소실" 알림이 뜬다. 그래서 만들지 않는 것까지만 한다.
     * 일반 계정으로 쓰다 게스트로 바꾸면 이전 본딩이 남는데, 그건 설정에서 지운다.
     *
     * 이메일은 DataStore 에서 읽어야 해 코루틴으로 넘긴다. 본딩은 원래 비동기로
     * 진행되고 결과는 시스템 브로드캐스트로 오므로, 여기서 기다릴 것이 없다.
     */
    private fun applyBondPolicy(device: BluetoothDevice?) {
        if (device == null) {
            Log.e("BOND", "본딩 판정 불가 - device 가 null")
            return
        }

        bleScope.launch {
            val email = DataStoreManager.getEmail().first()
            val isGuest = email != null && GuestList.getGuestList().contains(email)

            if (isGuest) {
                // 계정(이메일)은 찍지 않는다 - 판정 결과만 남긴다 (CWE-200)
                Log.d("BOND", "게스트 계정 - 본딩하지 않는다")
                return@launch
            }

            when (device.bondState) {
                BluetoothDevice.BOND_BONDED ->
                    Log.d("BOND", "이미 본딩되어 있다 - 그대로 둔다")

                BluetoothDevice.BOND_BONDING ->
                    Log.d("BOND", "본딩이 진행 중이다")

                else -> {
                    val started = device.createBond()
                    // 계정(이메일)은 찍지 않는다 - createBond() 결과만 남긴다 (CWE-200)
                    Log.d("BOND", "본딩 시작 : $started")
                }
            }
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


    var sRefreshMethod: Method? = null

    @Throws(NoSuchMethodException::class)
    private fun refreshMethod(): Method {
        // 캐시가 있으면 그것을, 없으면 만들어 담고 그 값을 돌려준다.
        // null 검사 후 !! 로 다시 꺼내면, 그 사이에 비워졌을 가능성을 컴파일러가
        // 못 지우기 때문에 !! 가 필요해진다. 한 식으로 묶으면 그 틈이 없다.
        return sRefreshMethod ?: BluetoothGatt::class.java.getMethod("refresh").also {
            sRefreshMethod = it
        }
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
        AppNotificationManager.notify(
            context = context,
            channel = AlertChannel.BLE_DISCONNECTED,
            title = title,
            message = message,
            notificationId = notificationId
        )
    }
}
