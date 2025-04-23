package kr.co.uxn.agms_p.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.co.uxn.agms_p.AlwaysApplication
import kr.co.uxn.agms_p.MainActivity
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestDataValue
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleManager.Companion.TEST
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.room.UserGlucose
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

class AlwaysService() : Service() {
    companion object {
        var isServiceRunning = false
    }

    lateinit var bleManager: BleManager
    lateinit var pendingIntent: PendingIntent

    private val localDbRepository by lazy {
        AppDatabase.getInstance(baseContext)
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var isDuplicatedJob: Job? = null

    private val localBinder = LocalBinder()

    private var timerForNoti: Timer? = null
    private var timerTaskForNoti: TimerTask? = null

    val NOTI_CHANNEL_ID: String = "NOTI_CHANNEL"
    val NOTI_CHANNEL_NAME: String = "FOREGROUND"
    val NOTI_ID: Int = 94

//    var manager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("SERVICE", "Service onCreate() call!")
//        bleManager = BleManager.getInstance(baseContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SERVICE", "Service onDestroy() call!")
        isServiceRunning = false

        // 노티 종료, 타이머 종료
        NotificationManagerCompat.from(baseContext).cancel(NOTI_ID)
        timerForNoti?.cancel()
        timerForNoti = null

        // 서비스안의 코루틴 제거
        serviceJob.cancel()
//        bleManager.reconnectHandler.removeCallbacksAndMessages(null)
        bleManager.isReconnect = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return localBinder
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == "ACTION_STOP_SERVICE") {
            Log.d("SERVICE", "Received ACTION_STOP_SERVICE, stopping service.")
            stopForeground(true) // 포그라운드만 종료
            stopSelf()
            return START_NOT_STICKY
        } else {
            Log.d("SERVICE", "Service onStartCommand() call!")
            Log.d("SERVICE", "Service onStartCommand() localDbRepository  : ${localDbRepository}!")

            var deviceMac = ""
            var userId = -1
            runBlocking {
                deviceMac = DataStoreManager.getDeviceMac().first().toString()
                userId = DataStoreManager.getUserId().first() ?: -1
            }

            Log.e("SERVICE", "onStartCommnad에서 DS로부터 불러온 userId : $userId")
            Log.e("SERVICE", "onStartCommnad에서 DS로부터 불러온 deviceMac : $deviceMac")

            val mac = deviceMac
            bleManager = BleManager.getInstance(baseContext, mac, userId, applicationContext)

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
//        if (timerForNoti == null) {
            timerForNoti = Timer()
            timerForNoti?.schedule(object : TimerTask() {
                override fun run() {
                    val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
                        .setOngoing(true)
                        .setContentTitle("Always가 작동 중입니다.")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setSilent(true)
                        .build()
                    NotificationManagerCompat.from(baseContext).notify(NOTI_ID, notification)
                }
            }, 0, 1000 * 10) // 10초에 한번씩 노티 생성
//        } else {
//            Log.e("SERVICE", "노티 1초마다 생서이 실행중이므로 스킵")
//        }

            // 노티 생성 및 설정
            val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle("AGMS 실행 중")
                .setContentText("Always가 작동 중입니다.")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build()

            // startForeground 실행
            startForeground(NOTI_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)

            isServiceRunning = true

            // 블루투스 연결
            val bluetoothManager =
                baseContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdpater = bluetoothManager.adapter
            val bluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.e(TEST, "======BLE Remote 연결 안드로이드 13 이상 시작========")
                bluetoothAdpater.getRemoteLeDevice(mac, BluetoothDevice.ADDRESS_TYPE_PUBLIC)
            } else {
                Log.e(TEST, "======BLE Remote 연결 안드로이드 10~12 시작========")
                bluetoothAdpater.getRemoteDevice(mac)
            }


//        if (bleManager.mGatt == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bluetoothDevice.connectGatt(
                    baseContext,
                    false,
                    bleManager,
                    BluetoothDevice.TRANSPORT_LE
                )
            } else {
                bluetoothDevice.connectGatt(baseContext, false, bleManager)
            }
//        } else {
//            Log.e("SERIVCE", "이미 ble 연결이 되어있어서 스킵")
//        }


            // 워커 실행
//            (application as AlwaysApplication).uploadWorkRequest()

            // 포그라운드에서 반복 실행
            isDuplicatedJob = serviceScope.launch {
                while (isActive) {
                    Log.d("SERVICE", "Coroutine 루프에서 반복 실행 중: ${System.currentTimeMillis()}")
                    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                    val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "agms:loopWake")

                    try {
                        Log.e("SERVICE", "서비스 내 코루틴 실행")

                        wl.acquire(1000 * 75) // 75초

                        val userId = DataStoreManager.getUserId().first() ?: -1

                        val lastTime = tokenRetrofit.getLastTime(userId)
                        if (lastTime.isSuccessful) {
                            val lastTimeBody = lastTime.body()
                            if (lastTimeBody != null) {
                                Log.e("SERVICE", "서비스 코루틴에서 호출한 lastTime (isSuccessful) : ${lastTimeBody.toString()}")
                                if(lastTimeBody.isSuccess == false) {
                                    val userId2 = DataStoreManager.getUserId().first() ?: -1
                                    val localDBDataList = localDbRepository?.dataDao()?.getListAfterLastTime(userId = userId2, lastTime = 0)

                                    Log.e("TEST", "DB로부터 가져온 리스트 : ${localDBDataList}")

                                    val sendDataList = localDBDataList?.map {
                                        RequestDataValue(
                                            userId = it.userId,
                                            createdAt = it.createdAt,
                                            weCurrent = it.weCurrent,
                                            aeCurrent = it.aeCurrent
                                        )
                                    }

                                    val chunkedList = sendDataList?.chunked(5)
                                    chunkedList?.forEachIndexed { index, chunk ->
                                        val sendData = tokenRetrofit.sendData(chunk)
                                        if (sendData.isSuccessful) {
                                            val sendDataBody = sendData.body()
                                            if (sendDataBody != null) {
                                                Log.e("TEST", "SendDataBody : ${sendDataBody.toString()}")
                                            }
                                        } else {
                                            Log.e(
                                                "TEST",
                                                "SendData API통신 실패 : ${sendData.errorBody()?.string()}"
                                            )
                                        }
                                    }

                                } else {

                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                    val convertToLocalDateTime = LocalDateTime.parse(lastTimeBody.recentTime, formatter)
                                    val zoneId = ZoneId.of("Asia/Seoul") // 타임존 설정 (필수!)
                                    val parsedLongTime = convertToLocalDateTime.atZone(zoneId).toInstant().toEpochMilli()

                                    val userId2 = DataStoreManager.getUserId().first() ?: -1

                                    val localDBDataList = localDbRepository?.dataDao()?.getListAfterLastTime(userId = userId2, lastTime = parsedLongTime)

                                    Log.e("TEST", "DB로부터 가져온 리스트 : ${localDBDataList}")

                                    val sendDataList = localDBDataList?.map {
                                        RequestDataValue(
                                            userId = it.userId,
                                            createdAt = it.createdAt,
                                            weCurrent = it.weCurrent,
                                            aeCurrent = it.aeCurrent
                                        )
                                    }

                                    val chunkedList = sendDataList?.chunked(5)
                                    chunkedList?.forEachIndexed { index, chunk ->
                                        val sendData = tokenRetrofit.sendData(chunk)
                                        if (sendData.isSuccessful) {
                                            val sendDataBody = sendData.body()
                                            if (sendDataBody != null) {
                                                Log.e("TEST", "SendDataBody : ${sendDataBody.toString()}")
                                            }
                                        } else {
                                            Log.e(
                                                "TEST",
                                                "SendData API통신 실패 : ${sendData.errorBody()?.string()}"
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e(
                                "TEST",
                                "recent time API통신 실패 : ${lastTime.errorBody()?.string()}"
                            )
                        }


                        val glucoseList = tokenRetrofit.getGlucoseList(userId)
                        if (glucoseList.isSuccessful) {
                            val glucoseListBody = glucoseList.body()
                            if (glucoseListBody != null) {

                                val userId = DataStoreManager.getUserId().first() ?: -1
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val zoneId = ZoneId.of("Asia/Seoul") // 타임존 설정 (필수!)

                                Log.e("TEST", "glucoseListBody : ${glucoseListBody}")
                                val insertDataList = glucoseListBody.map {
                                    val convertToLocalDateTime = LocalDateTime.parse(it.createdAt, formatter)
                                    val parsedLongTime = convertToLocalDateTime.atZone(zoneId).toInstant().toEpochMilli()
                                    UserGlucose(userId = userId , glucose = it.glucose.toDouble(), weo1 = it.weo1, weo2 = it.weo2, createdAt = it.createdAt, createdAtLong = parsedLongTime)
                                }

                                Log.e("TEST", "insertDataList : ${insertDataList}")
                                // db에 저장
                                localDbRepository?.dataDao()?.insertGlucose(insertDataList)

                                // ui에 마지막 글루코즈 값 갱신
                                Log.e("TEST", "glucoseList first : ${glucoseListBody.first().createdAt}, last : ${glucoseListBody.last().createdAt}")
                                BleBridge.updateGlucose(glucoseListBody.last().glucose)


                                val lastGlucose = glucoseListBody.last().glucose


                                // 알람을 위한 target glucose 값 불러오기

                                val targetHigh = DataStoreManager.getTargetHighGlucose().first() ?: -1
                                val targetLow = DataStoreManager.getTargetLowGlucose().first() ?: -1

                                val highChecker = DataStoreManager.getNotiHighGlucose().first() ?: false
                                val lowChecker = DataStoreManager.getNotiLowGlucose().first() ?: false

                                if (highChecker) {
                                    if (lastGlucose > targetHigh) {
                                        sendNotification(baseContext, "고혈당 주의", "고혈당이 감지되었습니다. \n현재 혈당 : ${lastGlucose} mg/dL", 96)
                                    }
                                }

                                if (lowChecker) {
                                    if (lastGlucose < targetLow) {
                                        sendNotification(baseContext, "저혈당 주의", "저혈당이 감지되었습니다 \n현재 혈당 : ${lastGlucose} mg/dL", 95)
                                    }
                                }

                                // 그래프를 위한 트리거
                                BleBridge.activateTrigger()
                            }
                        } else {
                            Log.e("TEST", "glucoseList API통신 실패 : ${glucoseList.errorBody()?.string()}")
                        }

                        // 측정 종료 로직
                        val zoneId = ZoneId.of("Asia/Seoul")
                        val now = LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli()
                        val endTime = DataStoreManager.getEndTime().first()

                        val convertedNow= Instant.ofEpochMilli(now)
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                        val convertedEndTime = endTime?.let {
                            Instant.ofEpochMilli(it)
                                .atZone(ZoneId.of("Asia/Seoul"))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        }

                        Log.d("SERVICE", "now : $convertedNow \nendTime : $convertedEndTime")

                        if (endTime != null && now > endTime) {
                            DataStoreManager.saveIsMain(false)
                            DataStoreManager.deleteRoute()
                            DataStoreManager.saveRoute("Splash")
                            Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                            Log.e("TEST", "DS에 저장된 Route는${DataStoreManager.getRoute().first()}")
                            DataStoreManager.deleteAccessToken()
                            DataStoreManager.deleteRefreshToken()
                            DataStoreManager.deleteUserId()
                            DataStoreManager.deleteDeviceMac()
                            DataStoreManager.deleteStartTime()
                            DataStoreManager.deleteEndTime()
                            // 1. 서비스 종료
                            stopSelf()
                            // 앱 강제종료
                            android.os.Process.killProcess(android.os.Process.myPid())
                            exitProcess(0)
                        }


                        delay(1000 * 60 * 1)
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
        val channelId = "glucose_alert_channel"

        // Oreo 이상은 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Glucose Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for high or low glucose levels"
            }

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        NotificationManagerCompat.from(baseContext).notify(notificationId, notification)
    }

    inner class LocalBinder : Binder() {
        fun getService(): AlwaysService = this@AlwaysService
    }
}

