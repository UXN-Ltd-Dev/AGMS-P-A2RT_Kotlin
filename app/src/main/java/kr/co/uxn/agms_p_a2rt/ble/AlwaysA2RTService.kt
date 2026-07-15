package kr.co.uxn.agms_p_a2rt.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.BleConnectionState
import kr.co.uxn.agms_p_a2rt.GuestList
import kr.co.uxn.agms_p_a2rt.MainActivity
import kr.co.uxn.agms_p_a2rt.PythonManager2
import kr.co.uxn.agms_p_a2rt.PythonManager3
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestA2RTData
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestDataCollectValue
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestDataValue
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.notification.AlertChannel
import kr.co.uxn.agms_p_a2rt.notification.AppNotificationManager
import kr.co.uxn.agms_p_a2rt.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p_a2rt.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p_a2rt.ble.BleManager.Companion.TEST
import kr.co.uxn.agms_p_a2rt.ble.BleUtils.STATUS_BLE_ENABLED
import kr.co.uxn.agms_p_a2rt.ble.BleUtils.TAG
import kr.co.uxn.agms_p_a2rt.ble.BleUtils.getBleStatus
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.GlucoseAlert
import kr.co.uxn.agms_p_a2rt.room.GlucoseAlertType
import kr.co.uxn.agms_p_a2rt.room.UserCalibration
import kr.co.uxn.agms_p_a2rt.room.UserGlucose
import kr.co.uxn.agms_p_a2rt.room.UserValue
import java.lang.reflect.Method
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.time.Duration
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

class AlwaysA2RTService() : Service() {
    companion object {
        private const val DEVICE_TYPE_F23UTC = 1105
        private const val DEVICE_TYPE_I10 = 1106
        var isServiceRunning = false
    }

    lateinit var bleManager: BleManager
    lateinit var pendingIntent: PendingIntent

    private val localDbRepository by lazy {
        AppDatabase.getInstance(baseContext)
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var bleInitializationJob: Job? = null
    private var isDuplicatedJob: Job? = null
    private var resourcesReleased = false

    private val localBinder = LocalBinder()
    var count = 0
    private val guestMinuteTargets = listOf(60, 110, 200, 110)
    private var guestMinuteTargetIndex = 1
    private var guestPreviousGlucose = 60

    private var timerForNoti: Timer? = null
    private var timerTaskForNoti: TimerTask? = null

    //    val NOTI_CHANNEL_ID: String = "NOTI_CHANNEL"
    val NOTI_CHANNEL_ID: String = "NOTI_CHANNEL_ID"
    val NOTI_CHANNEL_NAME: String = "FOREGROUND"
    val NOTI_ID: Int = 94

//    var manager: NotificationManager? = null

    private var mBluetoothStateBroadcastReceiver: BroadcastReceiver? = null

    private fun formatUtcDateTime(epochMilli: Long): String {
        return Instant.ofEpochMilli(epochMilli)
            .atZone(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("SERVICE", "Service onCreate() call!")
//        bleManager = BleManager.getInstance(baseContext)
    }

    override fun onDestroy() {
        Log.d("SERVICE", "Service onDestroy() call!")
        releaseResources()
        super.onDestroy()
    }

    private fun releaseResources() {
        if (resourcesReleased) return
        resourcesReleased = true
        isServiceRunning = false

        // 노티 종료, 타이머 종료
        NotificationManagerCompat.from(baseContext).cancel(NOTI_ID)
        timerTaskForNoti?.cancel()
        timerTaskForNoti = null
        timerForNoti?.cancel()
        timerForNoti = null

        // 서비스안의 코루틴 제거
        bleInitializationJob?.cancel()
        bleInitializationJob = null
        isDuplicatedJob?.cancel()
        isDuplicatedJob = null
        serviceJob.cancel()

        if (::bleManager.isInitialized) {
            bleManager.shutdown()
        }

        mBluetoothStateBroadcastReceiver?.let { receiver ->
            runCatching { unregisterReceiver(receiver) }
                .onFailure { Log.w("SERVICE", "BLE receiver 해제 실패: ${it.message}") }
        }
        mBluetoothStateBroadcastReceiver = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return localBinder
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == "ACTION_STOP_SERVICE") {
            Log.d("SERVICE", "Received ACTION_STOP_SERVICE, stopping service.")
            startForegroundForStopRequest()
            releaseResources()
            stopForeground(true) // 포그라운드만 종료
            stopSelf()
            return START_NOT_STICKY
        } else {
            Log.d("SERVICE", "Service onStartCommand() call!")
            Log.d("SERVICE", "Service onStartCommand() localDbRepository  : ${localDbRepository}!")

            // Bluetooth OFF 노티
            Log.e(TAG, "======BLE OFF======")
            if (getBleStatus(baseContext) != STATUS_BLE_ENABLED) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        if (ActivityCompat.checkSelfPermission(
                                baseContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d("BLE", "BLE 권한 허용 안됨")
                            return@withContext
                        }
                        sendBleConnectNotification(baseContext, baseContext.getString(R.string.notification_bluetooth_off), "", 91)
                        BleBridge.showBluetoothOnDialog(true)
                    }
                }
            }
            // 홈화면 연결 상태 UI 변경
            BleBridge.updateState(BleConnectionState.DISCONNECTED)

            // 1. 노티 채널 생성
            createNotificationChannel()

            // 노티 눌러서 이동할 화면 설정
            val notificationIntent = Intent(baseContext, MainActivity::class.java)
            notificationIntent.flags = FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 안드로이드 11(API 레벨 30) 이상인 경우
                pendingIntent = PendingIntent.getActivity(
                    baseContext,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                // 안드로이드 11 이하인 경우
                pendingIntent = PendingIntent.getActivity(
                    baseContext,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // 2. 10초마다 노티 생성
            timerForNoti = Timer()
            timerForNoti?.schedule(object : TimerTask() {
                override fun run() {
                    val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
                        .setOngoing(true)
                        .setContentTitle(baseContext.getString(R.string.notification_agms_running))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setSilent(true)
                        .build()
                    NotificationManagerCompat.from(baseContext).notify(NOTI_ID, notification)
                }
            }, 0, 1000 * 10) // 10초에 한번씩 노티 생성

            // 노티 생성 및 설정
            val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle(baseContext.getString(R.string.notification_agms_running))
                .setContentText(baseContext.getString(R.string.notification_always_running))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build()

            // startForeground 실행
            startForeground(NOTI_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)

            isServiceRunning = true

            val bleInitializationReady = CompletableDeferred<Unit>()
            initializeBleFromDataStore(bleInitializationReady)

            // 포그라운드에서 반복 실행
            isDuplicatedJob?.cancel()
            isDuplicatedJob = serviceScope.launch {
                bleInitializationReady.await()
                while (isActive) {
                    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                    val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "agms:loopWake")

                    try {
                        Log.e("SERVICE", "서비스 내 코루틴 실행")

                        // 액세스 토큰 변경 테스트

                        wl.acquire(1000 * 75) // 75초

                        val userId = DataStoreManager.getUserId().first() ?: -1
                        val userEmail = DataStoreManager.getEmail().first() ?: ""
                        val isStabilizationCompleted = DataStoreManager.getStabilizationCompleted().first()

                        // Guest 유무 파악
                        // 1. 게스트인 경우
                        if (GuestList.getGuestList().contains(userEmail)) {
                            Log.d("Guest", "He is Guest")
                            Log.d("Guest", "userEmail : ${userEmail}")

                            // dummy api
//                            if (!isStabilizationCompleted) {
//                                Log.d("Guest", "안정화 완료 전이므로 게스트 더미 혈당 저장을 건너뜁니다.")
//                            } else {
                                try {
                                    val glucoseDummyList = tokenRetrofit.getDummyGlucose(count)
                                    if (glucoseDummyList.isSuccessful) {
                                        val glucoseListBody = glucoseDummyList.body()
                                        val latestResponse = glucoseListBody?.lastOrNull()
                                        val receivedGlucose = latestResponse?.glucose?.lastOrNull()
                                        count++
                                        if (latestResponse != null && receivedGlucose != null) {
                                            val userId = DataStoreManager.getUserId().first() ?: -1
                                            val formatter =
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                                            val now = System.currentTimeMillis()
                                            val targetGlucose =
                                                guestMinuteTargets[guestMinuteTargetIndex] // 60, 110, 200, 110
                                            val insertDataList = (0 until 6).map { offset ->
                                                val progress = offset / 5.0
                                                val easedProgress = (1.0 - cos(PI * progress)) / 2.0
                                                val interpolatedGlucose = guestPreviousGlucose +
                                                        (targetGlucose - guestPreviousGlucose) * easedProgress
                                                val glucose =
                                                    (interpolatedGlucose / 5.0).roundToInt() * 5
                                                val time = now - (5 - offset) * 10_000L
                                                val formattedTime = LocalDateTime.ofInstant(
                                                    Instant.ofEpochMilli(time),
                                                    ZoneId.of("Asia/Seoul")
                                                ).format(formatter)

                                                UserGlucose(
                                                    userId = userId,
                                                    glucose = glucose.toDouble(),
                                                    weo1 = 0.0,
                                                    weo2 = 0.0,
                                                    createdAt = formattedTime,
                                                    createdAtLong = time
                                                )
                                            }
                                            guestPreviousGlucose = targetGlucose
                                            guestMinuteTargetIndex =
                                                (guestMinuteTargetIndex + 1) % guestMinuteTargets.size
                                            val lastGlucose = insertDataList.last().glucose.toInt()

                                            Log.e("TEST", "glucoseListBody : ${glucoseListBody}")
                                            Log.e(
                                                "TEST",
                                                "received: $receivedGlucose, displayed: $lastGlucose"
                                            )

                                            Log.e("TEST", "insertDataList : $insertDataList")

                                            if (count > 1) {
                                                Log.d("TEST", "현재 count는 1이상 , count : $count")
                                                // db에 저장
                                                localDbRepository?.dataDao()
                                                    ?.insertGlucose(insertDataList)


                                                // ui에 마지막 글루코즈 값 갱신
                                                BleBridge.updateGlucose(lastGlucose)

                                                // 알람을 위한 target glucose 값 불러오기
                                                val targetHigh =
                                                    DataStoreManager.getTargetHighGlucose().first()
                                                        ?: 170
                                                val targetLow =
                                                    DataStoreManager.getTargetLowGlucose().first()
                                                        ?: 70

                                                val highChecker =
                                                    DataStoreManager.getNotiHighGlucose().first()
                                                        ?: false
                                                val lowChecker =
                                                    DataStoreManager.getNotiLowGlucose().first()
                                                        ?: false

                                                // 고혈당, 저혈당 알람
                                                if (highChecker) {
                                                    if (lastGlucose >= targetHigh) {
                                                        sendNotification(
                                                            baseContext,
                                                            baseContext.getString(R.string.notification_alert_high_glucose),
                                                            baseContext.getString(R.string.notification_alert_high_glucose_description) + "${lastGlucose} mg/dL",
                                                            96
                                                        )
                                                        localDbRepository?.dataDao()
                                                            ?.insertGlucoseAlert(
                                                                GlucoseAlert(
                                                                    userId = userId,
                                                                    alertType = GlucoseAlertType.HIGH,
                                                                    glucose = lastGlucose,
                                                                    createdAtLong = System.currentTimeMillis()
                                                                )
                                                            )
                                                        showHighGlucoseDialog(true)
                                                        Log.e(
                                                            "NOTI",
                                                            "고혈당, lastGlucose: ${lastGlucose}, targetHigh: ${targetHigh}"
                                                        )
                                                    }
                                                }

                                                if (lowChecker) {
                                                    if (lastGlucose < targetLow) {
                                                        sendNotification(
                                                            baseContext,
                                                            baseContext.getString(R.string.notification_alert_low_glucose),
                                                            baseContext.getString(R.string.notification_alert_low_glucose_description) + "${lastGlucose} mg/dL",
                                                            95
                                                        )
                                                        localDbRepository?.dataDao()
                                                            ?.insertGlucoseAlert(
                                                                GlucoseAlert(
                                                                    userId = userId,
                                                                    alertType = GlucoseAlertType.LOW,
                                                                    glucose = lastGlucose,
                                                                    createdAtLong = System.currentTimeMillis()
                                                                )
                                                            )
                                                        showLowGlucoseDialog(true)
                                                        Log.e(
                                                            "NOTI",
                                                            "저혈당, lastGlucose: ${lastGlucose}, targetLow: ${targetLow}"
                                                        )
                                                    }
                                                }


                                                // 그래프를 위한 트리거
                                                BleBridge.activateTrigger()
                                            } else {
                                                Log.d("TEST", "현재 count는 1이하라 db저장을 건너뜁니다 , count : $count")
                                            }
                                        }
                                    } else {
                                        Log.e(
                                            "TEST",
                                            "glucoseList API통신 실패 : ${
                                                glucoseDummyList.errorBody()?.string()
                                            }"
                                        )
                                    }

                                } catch (e: Exception) {
                                    Log.d("DUMMY", "Dummy API 에러 : ${e.message}")
                                } // 요기까지
//                            }

                            try {
                                val lastTime = tokenRetrofit.getLastTime(userId)
                                if (lastTime.isSuccessful) {
                                    val lastTimeBody = lastTime.body()
                                    if (lastTimeBody != null) {
                                        Log.e(
                                            "SERVICE",
                                            "서비스 코루틴에서 호출한 lastTime (isSuccessful) : ${lastTimeBody.toString()}"
                                        )
                                        if (lastTimeBody.isSuccess == false) {
                                            val userId2 = DataStoreManager.getUserId().first() ?: -1
                                            val localDBDataList = localDbRepository?.dataDao()
                                                ?.getListAfterLastTime(
                                                    userId = userId2,
                                                    lastTime = 0
                                                )

                                            Log.e("TEST", "DB 로부터 가져온 리스트 : ${localDBDataList}")

                                            val sendDataList = localDBDataList?.map {
                                                RequestDataCollectValue(
                                                    userId = it.userId,
                                                    createdAt = formatUtcDateTime(it.createdAtLong),
                                                    weCurrent = it.weCurrent,
                                                    aeCurrent = it.aeCurrent,
                                                    temperature = it.temperature
                                                )
                                            }

                                            // 서버에 timeout 없이 보내기위해 5개로 쪼개서 전송
                                            val chunkedList = sendDataList?.chunked(5)
                                            chunkedList?.forEachIndexed { index, chunk ->
                                                val sendData = tokenRetrofit.sendData(chunk)
                                                if (sendData.isSuccessful) {
                                                    val sendDataBody = sendData.body()
                                                    if (sendDataBody != null) {
                                                        Log.d(
                                                            "TEST",
                                                            "SendDataBody : ${sendDataBody.toString()}"
                                                        )
                                                    }
                                                } else {
                                                    Log.d(
                                                        "TEST",
                                                        "SendData API통신 실패 : ${
                                                            sendData.errorBody()?.string()
                                                        }"
                                                    )
                                                }
                                            }
                                        } else {
                                            Log.d("choco5732", "recentTime = ${lastTimeBody.recentTime}")
                                            Log.d("TEST", "2")

                                            val formatter =
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                            val convertToLocalDateTime = LocalDateTime.parse(
                                                lastTimeBody.recentTime,
                                                formatter
                                            )
//                                                        val zoneId = ZoneId.of("Asia/Seoul") // 타임존 설정 (필수!)
                                            val zoneId = ZoneId.of("UTC") // 타임존 설정 (필수!)
                                            val parsedLongTime =
                                                convertToLocalDateTime.atZone(zoneId).toInstant()
                                                    .toEpochMilli()

                                            val userId2 = DataStoreManager.getUserId().first() ?: -1

                                            val localDBDataList = localDbRepository?.dataDao()
                                                ?.getListAfterLastTime(
                                                    userId = userId2,
                                                    lastTime = parsedLongTime
                                                )

                                            Log.d("TEST", "DB로부터 가져온 리스트 : ${localDBDataList}")

                                            val sendDataList = localDBDataList?.map {
                                                RequestDataCollectValue(
                                                    userId = it.userId,
                                                    createdAt = formatUtcDateTime(it.createdAtLong),
                                                    weCurrent = it.weCurrent,
                                                    aeCurrent = it.aeCurrent,
                                                    temperature = it.temperature
                                                )
                                            }

                                            val chunkedList = sendDataList?.chunked(5)
                                            chunkedList?.forEachIndexed { index, chunk ->
                                                val sendData = tokenRetrofit.sendData(chunk)
                                                if (sendData.isSuccessful) {
                                                    val sendDataBody = sendData.body()
                                                    if (sendDataBody != null) {
                                                        Log.d(
                                                            "TEST",
                                                            "SendDataBody : ${sendDataBody.toString()}"
                                                        )
                                                    }
                                                } else {
                                                    Log.d(
                                                        "TEST",
                                                        "SendData API통신 실패 : ${
                                                            sendData.errorBody()?.string()
                                                        }"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.d(
                                        "TEST",
                                        "recent time API통신 실패 : ${lastTime.errorBody()?.string()}"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.d("SERVICE", "서비스 내 API통신 에러 발생 : ${e.message}")
                            }


                        } else {
                            // 게스트 아닌 경우
                            Log.d("Guest", "He is not Guest")
                            Log.d("Guest", "userEmail : ${userEmail}")

                            try {
                                val lastTime = tokenRetrofit.getLastTime(userId)
                                if (lastTime.isSuccessful) {
                                    val lastTimeBody = lastTime.body()
                                    if (lastTimeBody != null) {
                                        Log.e(
                                            "SERVICE",
                                            "서비스 코루틴에서 호출한 lastTime (isSuccessful) : ${lastTimeBody.toString()}"
                                        )
                                        if (lastTimeBody.isSuccess == false) {
                                            Log.d("TEST", "4")
                                            val userId2 = DataStoreManager.getUserId().first() ?: -1
                                            val localDBDataList = localDbRepository?.dataDao()
                                                ?.getListAfterLastTime(
                                                    userId = userId2,
                                                    lastTime = 0
                                                )

                                            Log.e("TEST", "DB 로부터 가져온 리스트 : ${localDBDataList}")

                                            val sendDataList = localDBDataList?.map {
                                                RequestDataCollectValue(
                                                    userId = it.userId,
                                                    createdAt = formatUtcDateTime(it.createdAtLong),
                                                    weCurrent = it.weCurrent,
                                                    aeCurrent = it.aeCurrent,
                                                    temperature = it.temperature
                                                )
                                            }
                                            Log.d("TEST", "5")

                                            val chunkedList = sendDataList?.chunked(5)
                                            chunkedList?.forEachIndexed { index, chunk ->
                                                val sendData = tokenRetrofit.sendData(chunk)
                                                if (sendData.isSuccessful) {
                                                    val sendDataBody = sendData.body()
                                                    if (sendDataBody != null) {
                                                        Log.d(
                                                            "TEST",
                                                            "SendDataBody : ${sendDataBody.toString()}"
                                                        )
                                                    }
                                                } else {
                                                    Log.d(
                                                        "TEST",
                                                        "SendData API통신 실패 : ${
                                                            sendData.errorBody()?.string()
                                                        }"
                                                    )
                                                }
                                            }
                                        } else {
                                            Log.d("TEST", "6")
                                            val formatter =
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                            val convertToLocalDateTime = LocalDateTime.parse(
                                                lastTimeBody.recentTime,
                                                formatter
                                            )
                                            val zoneId = ZoneId.of("UTC") // 타임존 설정 (필수!)
                                            val parsedLongTime =
                                                convertToLocalDateTime.atZone(zoneId).toInstant()
                                                    .toEpochMilli()

                                            Log.d("TEST", "7")

                                            val userId2 = DataStoreManager.getUserId().first() ?: -1

                                            val localDBDataList = localDbRepository?.dataDao()
                                                ?.getListAfterLastTime(
                                                    userId = userId2,
                                                    lastTime = parsedLongTime
                                                )

                                            Log.d("TEST", "DB로부터 가져온 리스트 : ${localDBDataList}")

                                            val sendDataList = localDBDataList?.map {
                                                RequestDataCollectValue(
                                                    userId = it.userId,
                                                    createdAt = formatUtcDateTime(it.createdAtLong),
                                                    weCurrent = it.weCurrent,
                                                    aeCurrent = it.aeCurrent,
                                                    temperature = it.temperature
                                                )
                                            }

                                            val chunkedList = sendDataList?.chunked(5)
                                            chunkedList?.forEachIndexed { index, chunk ->
                                                val sendData = tokenRetrofit.sendData(chunk)
                                                if (sendData.isSuccessful) {
                                                    val sendDataBody = sendData.body()
                                                    if (sendDataBody != null) {
                                                        Log.d(
                                                            "TEST",
                                                            "SendDataBody : ${sendDataBody.toString()}"
                                                        )
                                                    }
                                                } else {
                                                    Log.d(
                                                        "TEST",
                                                        "SendData API통신 실패 : ${
                                                            sendData.errorBody()?.string()
                                                        }"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.d(
                                        "TEST",
                                        "recent time API통신 실패 : ${lastTime.errorBody()?.string()}"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.d("SERVICE", "서비스 내 API통신 에러 발생 : ${e.message}")
                            }

                            // A2RT 데이터 전송 코드
                            try {
                                val userDeviceId = DataStoreManager.getUserDeviceId().first()
                                if (userDeviceId != null) {
                                    val lastImpedanceTime = tokenRetrofit.getA2RTLastTime(userDeviceId)
                                    if (lastImpedanceTime.isSuccessful) {
                                        val lastImpedanceTimeBody = lastImpedanceTime.body()
                                        if (lastImpedanceTimeBody != null) {
                                            val targetTime = if (lastImpedanceTimeBody.success) {
                                                lastImpedanceTimeBody.targetTime?.let { timeString ->
                                                    LocalDateTime.parse(
                                                        timeString,
                                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                    )
                                                        .atZone(ZoneId.of("UTC"))
                                                        .toInstant()
                                                        .toEpochMilli()
                                                } ?: 0L
                                            } else {
                                                0L
                                            }

                                            val a2rtDataList = localDbRepository?.dataDao()
                                                ?.getA2RTDataList(
                                                    userDeviceId = userDeviceId,
                                                    targetTime = targetTime
                                                )
                                                .orEmpty()

                                            Log.d(
                                                "SERVICE",
                                                "A2RT 전송 대상 개수 : ${a2rtDataList.size}, targetTime : $targetTime"
                                            )

                                            val requestA2RTDataList = a2rtDataList.map { a2rtData ->
                                                RequestA2RTData(
                                                    userDeviceId = userDeviceId,
                                                    frequencyType = a2rtData.frequencyType,
                                                    createdAt = formatUtcDateTime(a2rtData.createdAt),
                                                    temperature = a2rtData.temperature,
                                                    real = a2rtData.real,
                                                    imaginary = a2rtData.imaginary,
                                                    magnitude = a2rtData.magnitude,
                                                    phase = a2rtData.phase
                                                )
                                            }

                                            if (requestA2RTDataList.isNotEmpty()) {
                                                val sendA2RTData = tokenRetrofit.sendA2RTData(
                                                    requestA2RTDataList
                                                )
                                                if (sendA2RTData.isSuccessful) {
                                                    Log.d(
                                                        "SERVICE",
                                                        "A2RT 데이터 ${requestA2RTDataList.size}건 전송 성공 : ${sendA2RTData.code()}"
                                                    )
                                                } else {
                                                    Log.d(
                                                        "SERVICE",
                                                        "A2RT 데이터 전송 실패 : ${sendA2RTData.errorBody()?.string()}"
                                                    )
                                                }
                                            } else {
                                                Log.d("SERVICE", "전송할 A2RT 데이터가 없습니다.")
                                            }
                                        } else {
                                            Log.d("SERVICE", "A2RT 최신 데이터 조회 응답 body가 null입니다.")
                                        }
                                    } else {
                                        Log.d(
                                            "SERVICE",
                                            "A2RT 최신 데이터 조회 실패 : ${lastImpedanceTime.errorBody()?.string()}"
                                        )
                                    }
                                } else {
                                    Log.d("SERVICE", "user_device_id가 없어 A2RT 최신 데이터 조회를 건너뜁니다.")
                                }

                            } catch (e: Exception) {
                                Log.d("SERVICE", "A2RT 데이터 전송 중 에러 발생 : ${e.message}")
                            }


                        }

                        /** 일일 혈당 알림
                        val isDailyCalibration = DataStoreManager.getNotiCalibration().first() ?: true
                        val dailyCalibrationLastTime = DataStoreManager.getDailyCalibrationLastTime().first() ?: ""
                        val today = LocalDate.now(ZoneId.of("Asia/Seoul")).toString()
                        Log.d("CALI", "today is : ${today}\nlastTime : ${dailyCalibrationLastTime}")

                        if (isDailyCalibration && dailyCalibrationLastTime != today) {
                            val calibrationTime = DataStoreManager.getDailyCalibrationTime().first() ?: "오전 11:00"
                            Log.d("CALI", "calibrationTime is : ${calibrationTime}")
                            if (calibrationTime != "") {
                                // a hh:mm 형태의 스트링 값을 현재 시간과 비교후 오차 간격 3분 이내면 알림 울림
                                val formatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                                val targetTime = LocalTime.parse(calibrationTime, formatter)
                                Log.d("CALI", "targetTime is : ${targetTime}")

                                val nowTime = LocalTime.now(ZoneId.of("Asia/Seoul"))
                                Log.d("CALI", "nowTime is : ${nowTime}")

                                val diff = Duration.between(targetTime, nowTime).toMinutes().let { abs(it) }
                                Log.d("CALI", "diff is : ${diff}")

                                if (diff <= 3) {
                                    Log.d("CALI", "3분 이내! 알림 실행 diff : ${diff}")
                                    sendNotification(
                                        baseContext,
                                        baseContext.getString(R.string.notification_time_to_enter_daily_glucose),
                                        baseContext.getString(R.string.notification_time_to_enter_daily_glucose_description),
                                        93
                                    )
                                    BleBridge.showCaliDialog(true)
                                } else {
                                    Log.d("CALI", "캘리 알림 범위 아님 : ${diff}")
                                }
                            }
                        }
                        **/


                        // 혈당 불러 오기 (new)
                        if (!GuestList.getGuestList().contains(userEmail)) {
                            val userValueList =
                                localDbRepository?.dataDao()?.getListAfterLastTime(userId, 0)

                            // 빈 리스트 생성
                            var seperatedUserValueList: List<UserValue>? = emptyList()

                            // 첫 번째 순으로 짤라서 담기
                            if (!userValueList.isNullOrEmpty()) {
                                seperatedUserValueList = userValueList
                                    .chunked(6)
                                    .map { it.first() }
                                    .sortedBy { it.createdAtLong }

                                Log.d("TEST", "userValueList : ${userValueList}")
                                Log.d("TEST", "seperatedUserValueList : ${seperatedUserValueList}")
                            }


                            val calibrationList: List<UserCalibration> =
                                localDbRepository?.dataDao()?.getCalibrationList(userId).orEmpty()


                            val pythonData = PythonManager3.instance.calculateGlucose(
                                seperatedUserValueList!!,
                                calibrationList
                            )

                            Log.d("PYTHON", "받아온 pythonData : ${pythonData?.glucoseList}")

                            if (pythonData != null) {
                                val insertDataList = pythonData.glucoseList.mapIndexed { index, glucose ->

                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                                    val time = pythonData.timeStampList[index]
                                    val timeString = Instant.ofEpochMilli(time) // db에 개발자가 보기편하게 하기위한 한국시간용, 계산용도는 아님
                                        .atZone(ZoneId.of("Asia/Seoul"))
                                        .toLocalDateTime()
                                        .format(formatter)
                                    UserGlucose(
                                        userId = userId,
                                        glucose = glucose,
                                        weo1 = seperatedUserValueList[index].weCurrent,
                                        weo2 = seperatedUserValueList[index].aeCurrent,
                                        createdAt = timeString,
                                        createdAtLong = time
                                    )
                                }

                                Log.e("TEST", "insertDataList : ${insertDataList}")
                                if (insertDataList.isNotEmpty()) {
                                    // Python 계산 결과를 최종 혈당 데이터로 사용하므로 기존 혈당을 지우고 다시 저장한다.
                                    localDbRepository?.dataDao()?.replaceUserGlucoseTable(userId, insertDataList)
                                }

                                // ui에 마지막 글루코즈 값 갱신
                                val lastGlucose = pythonData.glucoseList.last().toInt()
                                BleBridge.updateGlucose(lastGlucose)
                                Log.d("TEST", "lastGlucose : $lastGlucose")

                                // 알람을 위한 target glucose 값 불러 오기
                                val targetHigh = DataStoreManager.getTargetHighGlucose().first() ?: 170
                                val targetLow = DataStoreManager.getTargetLowGlucose().first() ?: 70

                                val highChecker = DataStoreManager.getNotiHighGlucose().first() ?: false
                                val lowChecker = DataStoreManager.getNotiLowGlucose().first() ?: false

                                // 고혈당, 저혈당 알람
                                if (highChecker) {
                                    if (lastGlucose >= targetHigh) {
                                        sendNotification(
                                            baseContext,
                                            baseContext.getString(R.string.notification_alert_high_glucose),
                                            baseContext.getString(R.string.notification_alert_high_glucose_description) + "${lastGlucose} mg/dL",
                                            96
                                        )
                                        localDbRepository?.dataDao()?.insertGlucoseAlert(
                                            GlucoseAlert(
                                                userId = userId,
                                                alertType = GlucoseAlertType.HIGH,
                                                glucose = lastGlucose,
                                                createdAtLong = System.currentTimeMillis()
                                            )
                                        )
                                        showHighGlucoseDialog(true)
                                        Log.e(
                                            "NOTI",
                                            "고혈당, lastGlucose: $lastGlucose, targetHigh: $targetHigh"
                                        )
                                    }
                                }

                                if (lowChecker) {
                                    if (lastGlucose < targetLow) {
                                        sendNotification(
                                            baseContext,
                                            baseContext.getString(R.string.notification_alert_low_glucose),
                                            baseContext.getString(R.string.notification_alert_low_glucose_description) + "${lastGlucose} mg/dL",

                                            95
                                        )
                                        localDbRepository?.dataDao()?.insertGlucoseAlert(
                                            GlucoseAlert(
                                                userId = userId,
                                                alertType = GlucoseAlertType.LOW,
                                                glucose = lastGlucose,
                                                createdAtLong = System.currentTimeMillis()
                                            )
                                        )
                                        showLowGlucoseDialog(true)
                                    }
                                }
                                // 그래프를 위한 트리거
                                BleBridge.activateTrigger()

                            } else {
                                Log.d("PYTHON", "glucoseList is empty! ${pythonData?.glucoseList?.size}")
                            }
                        }

                        // 측정 종료 알림
                        val now = System.currentTimeMillis()
                        val endTime = DataStoreManager.getEndTime().first()

                        val convertedNow = Instant.ofEpochMilli(now)
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                        val convertedEndTime = endTime?.let {
                            Instant.ofEpochMilli(it)
                                .atZone(ZoneId.of("Asia/Seoul"))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        }

                        Log.d("SERVICE", "now : $convertedNow \nendTime : $convertedEndTime")

                        if (endTime != null && now >= endTime) {
                            Log.d("SERVICE", "측정종료 프로세스 작동!")
                            sendNotification(baseContext, baseContext.getString(R.string.notification_end_measurement_check_app), baseContext.getString(R.string.notification_end_measurement_check_app_description), 94)
                            BleBridge.showEndMeasurementDialog(true)
                        }

                        delay(1000 * 60 * 1) // 1분
                    } catch (e: Exception) {
                        Log.e("SERVICE", "서비스 코루틴 에러 발생 : ${e.message}")
                    } finally {
                        if (wl.isHeld) wl.release()
                    }
                }
            }
            return START_STICKY
        }
    }

    private fun initializeBleFromDataStore(initializationReady: CompletableDeferred<Unit>) {
        bleInitializationJob?.cancel()
        bleInitializationJob = serviceScope.launch {
            try {
                val isSensorEnded = DataStoreManager.getIsSensorEnded().first()
                if (isSensorEnded) {
                    withContext(Dispatchers.Main) {
                        Log.d("SERVICE", "센서 사용이 종료되어 서비스를 중지합니다.")
                        releaseResources()
                        stopForeground(true)
                        stopSelf()
                    }
                    return@launch
                }

                val deviceMac = DataStoreManager.getDeviceMac().first().orEmpty()
                val userId = DataStoreManager.getUserId().first() ?: -1
                val userDeviceId = DataStoreManager.getUserDeviceId().first() ?: -1
                val deviceType = DataStoreManager.getDeviceType().first()

//                if (deviceMac.isBlank()) {
//                    withContext(Dispatchers.Main) {
//                        Log.e("SERVICE", "저장된 deviceMac이 없어 서비스를 중지합니다.")
//                        releaseResources()
//                        stopForeground(true)
//                        stopSelf()
//                    }
//                    return@launch
//                }

                val protocol: Protocol = when (deviceType) {
                    DEVICE_TYPE_I10 -> I10Device()
                    DEVICE_TYPE_F23UTC -> F23UTCDevice()
                    null -> {
                        Log.w(TAG, "저장된 deviceType이 없어 I10 프로토콜을 사용합니다.")
                        I10Device()
                    }
                    else -> {
                        Log.w(TAG, "알 수 없는 deviceType($deviceType), I10 프로토콜을 사용합니다.")
                        I10Device()
                    }
                }

                Log.d(
                    TAG,
                    "선택된 기기 프로토콜: ${protocol.javaClass.simpleName}, deviceType: $deviceType"
                )

                withContext(Dispatchers.Main) {
                    if (!isServiceRunning) return@withContext

                    bleManager = BleManager.getInstance(
                        baseContext,
                        deviceMac,
                        userId,
                        userDeviceId,
                        applicationContext,
                        protocol
                    )
                    registerBluetoothStateBroadcastReceiver(deviceMac)

                    val bluetoothManager =
                        baseContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val bluetoothAdapter = bluetoothManager.adapter
                    val bluetoothDevice =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Log.e(TEST, "======BLE Remote 연결 안드로이드 13 이상 시작========")
                            bluetoothAdapter.getRemoteLeDevice(
                                deviceMac,
                                BluetoothDevice.ADDRESS_TYPE_PUBLIC
                            )
                        } else {
                            Log.e(TEST, "======BLE Remote 연결 안드로이드 10~12 시작========")
                            bluetoothAdapter.getRemoteDevice(deviceMac)
                        }

                    BleManager.mGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bluetoothDevice.connectGatt(
                            baseContext,
                            false,
                            bleManager,
                            BluetoothDevice.TRANSPORT_LE
                        )
                    } else {
                        bluetoothDevice.connectGatt(baseContext, false, bleManager)
                    }

                    initializationReady.complete(Unit)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("SERVICE", "BLE 초기화 중 오류가 발생했습니다.", e)
                withContext(Dispatchers.Main) {
                    if (isServiceRunning) {
                        releaseResources()
                        stopForeground(true)
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 코드 O : API 26, 안드로이드 8
            val serviceChannel = NotificationChannel(
                NOTI_CHANNEL_ID,
                NOTI_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)

            Log.e("SERVICE", "이미 노티 매니저가 생성되었으므로 스킵")
        }
    }

    private fun startForegroundForStopRequest() {
        createNotificationChannel()

        val notificationIntent = Intent(baseContext, MainActivity::class.java).apply {
            flags = FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP
        }
        val stopPendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val stopPendingIntent = PendingIntent.getActivity(
            baseContext,
            0,
            notificationIntent,
            stopPendingIntentFlags
        )
        val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
            .setOngoing(false)
            .setContentTitle(baseContext.getString(R.string.notification_agms_running))
            .setContentText(baseContext.getString(R.string.notification_always_running))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(stopPendingIntent)
            .setSilent(true)
            .build()

        startForeground(NOTI_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
        AppNotificationManager.notify(
            context = context,
            channel = AlertChannel.GLUCOSE,
            title = title,
            message = message,
            notificationId = notificationId
        )
    }

    private fun registerBluetoothStateBroadcastReceiver(mac: String) {
        if (mBluetoothStateBroadcastReceiver == null) {
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            mBluetoothStateBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF
                    )
                    handleBLEStateChanged(state, mac)
                }
            }
            registerReceiver(mBluetoothStateBroadcastReceiver, filter)
        }
    }

    private fun handleBLEStateChanged(state: Int, mac: String) {
        when (state) {
            // 1
            BluetoothAdapter.STATE_TURNING_OFF -> {
                Log.d(TAG, "======BLE 비활성화 중...======")
            }

            // 2
            BluetoothAdapter.STATE_OFF -> {
                Log.e(TAG, "======BLE OFF======")
//                resetDevice()
                // 끊김 알림
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        if (ActivityCompat.checkSelfPermission(
                                baseContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.d("BLE", "BLE 권한 허용 안됨")
                            return@withContext
                        }
                        sendBleConnectNotification(baseContext, baseContext.getString(R.string.notification_bluetooth_off), "", 91)
                        BleBridge.showBluetoothOnDialog(true)
                    }
                }
                // 홈화면 연결 상태 UI 변경
                BleBridge.updateState(BleConnectionState.DISCONNECTED)
            }

            // 3
            BluetoothAdapter.STATE_TURNING_ON -> {
                Log.d(TAG, "======BLE 활성화 중...======")
            }

            // 4
            BluetoothAdapter.STATE_ON -> {
                Log.e(TAG, "======BLE ON!!======")
//                mGatt?.disconnect()
//                mGatt?.close()
//                refreshDeviceCache(mGatt)
//                mGatt = null
                // 블루투스 연결
                val bluetoothManager =
                    baseContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                val bluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Log.e(TEST, "======BLE Remote 연결 안드로이드 13 이상 시작========")
                    bluetoothAdapter.getRemoteLeDevice(mac, BluetoothDevice.ADDRESS_TYPE_PUBLIC)
                } else {
                    Log.e(TEST, "======BLE Remote 연결 안드로이드 10~12 시작========")
                    bluetoothAdapter.getRemoteDevice(mac)
                }


//        if (bleManager.mGatt == null) {
                CoroutineScope(Dispatchers.Main).launch {
                    BleManager.mGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bluetoothDevice.connectGatt(
                            baseContext,
                            false,
                            bleManager,
                            BluetoothDevice.TRANSPORT_LE
                        )
                    } else {
                        bluetoothDevice.connectGatt(baseContext, false, bleManager)
                    }
                }

                // 기존 블루투스 On 노티 제거
                NotificationManagerCompat.from(baseContext).cancel(91)
                BleBridge.showBluetoothOnDialog(false)
            }

            BluetoothAdapter.STATE_DISCONNECTED -> {
                Log.e(TAG, "BLE 연결 종료됨")
            }

            BluetoothAdapter.STATE_CONNECTED -> {
                Log.e(TAG, "BLE 연결됨")
            }

            else -> {
                Log.e(TAG, "알 수 없는 BLE 상태: $state")
            }
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

    var sRefreshMethod: Method? = null

    @Throws(NoSuchMethodException::class)
    private fun refreshMethod(): Method {
        if (sRefreshMethod == null) {
            sRefreshMethod = BluetoothGatt::class.java.getMethod("refresh")
        }
        return sRefreshMethod!!
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun sendBleConnectNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        AppNotificationManager.notify(
            context = context,
            channel = AlertChannel.BLE_STATUS,
            title = title,
            message = message,
            notificationId = notificationId
        )
    }


    inner class LocalBinder : Binder() {
        fun getService(): AlwaysA2RTService = this@AlwaysA2RTService
    }
}
