package kr.co.uxn.agms_p

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kakao.sdk.common.KakaoSdk
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleManager.Companion.TEST
import kr.co.uxn.agms_p.ble.SendDataWorker
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.room.DummyValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class AlwaysApplication : Application() {

    private val localDbRepository by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.kakao_native_app_key)

        // 토큰매니저 초기화
        DataStoreManager.init(this)

        // 파이썬 초기화
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // db에 더미 저장

//        val inputStream = this.assets.open("dummy_current_file1.csv")
//
//        inputStream.bufferedReader().useLines { lines ->
//            lines.drop(1)
//                .forEach { line ->
//                    val tokens = line.split(",")
//                    if (tokens.size >= 4) {
//                        try {
//                            val item = DummyValue(
//                                weCurrent = tokens[2].trim().toDouble(),
//                                aeCurrent = tokens[3].trim().toDouble(),
//                                createdAt = tokens[1].trim().toString(),
//                                createdAtLong = 12
//                            )
//                        } catch (e : Exception) {
//                        }
//                    }
//                }
//        }
//        val reader = BufferedReader(InputStreamReader(inputStream))
//
//        reader.useLines { lines ->
//            lines.forEach { line ->
//                Log.d("TEST", "CSV Line: $line")
//            }
//        }

//        localDbRepository.dataDao().insertDummyList()

    }
    fun uploadWorkRequest() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 워커 작동 주기 설정하는 곳!
        val sendServerRequest = PeriodicWorkRequestBuilder<SendDataWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sendServer",
            ExistingPeriodicWorkPolicy.KEEP,
            sendServerRequest
        )

        Log.e(TEST, "===========WORK 등록===============")
    }

}

