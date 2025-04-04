package kr.co.uxn.agms_p.ble

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kr.co.uxn.agms_p.MainActivity
import kr.co.uxn.agms_p.R
import java.util.Timer
import java.util.TimerTask

class AlwaysService() : Service() {
    companion object{
        var isServiceRunning = false
    }
//    lateinit var bleManager: BleManager
    lateinit var pendingIntent: PendingIntent

    private var timerForNoti: Timer? = null
    private var timerTaskForNoti: TimerTask? = null

    val NOTI_CHANNEL_ID: String = "NOTI_CHANNEL"
    val NOTI_CHANNEL_NAME: String = "FOREGROUND"
    val NOTI_ID: Int = 94

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

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
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SERVICE", "Service onStartCommand() call!")

        val device = intent?.getParcelableExtra<Device>("device")
        // 노티 채널 생성
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


        timerForNoti = Timer()
        timerForNoti?.schedule(object: TimerTask() {
            override fun run() {
                val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
                    .setOngoing(true)
                    .setContentTitle("Always가 작동 중입니다.")
//            .setContentTitle("연결된 장치 : ${rssi?.deviceName}   ${rssi?.rssi}\n연결 상태 : ${bleState}")
//                    .setContentText("포그라운드 서비스가 작동 중 입니다.")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .setSilent(true)
                    .build()
                NotificationManagerCompat.from(baseContext).notify(NOTI_ID, notification)
            }

        }, 0 , 1000)

        // 노티 생성 및 설정
        val notification = NotificationCompat.Builder(baseContext, NOTI_CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle("AGMS 실행 중")
//            .setContentTitle("연결된 장치 : ${rssi?.deviceName}   ${rssi?.rssi}\n연결 상태 : ${bleState}")
            .setContentText("포그라운드 서비스가 작동 중 입니다.")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()

        startForeground(NOTI_ID, notification)
//        startForeground(NOTI_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)

        // 블루투스 연결
//        val device = bleManager.getDevice()
        isServiceRunning = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mGatt = device.connectGatt(baseContext, false, bleManager, BluetoothDevice.TRANSPORT_LE)
//            device?.device?.connectGatt(baseContext, false, bleManager, BluetoothDevice.TRANSPORT_LE)
        } else {
//            mGatt = device.device?.connectGatt(baseContext, false, bleManager)
//            device?.device?.connectGatt(baseContext, false, bleManager)
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 코드 O : API 26, 안드로이드 8
            val serviceChannel = NotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}