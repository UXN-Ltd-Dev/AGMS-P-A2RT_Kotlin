package kr.co.uxn.agms_p_a2rt.ble

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager

/**
 * 포그라운드 서비스가 죽었으면 다시 띄우는 워치독.
 *
 * 안드로이드는 메모리 압박·배터리 최적화로 포그라운드 서비스도 죽인다. 그때 스스로
 * 살아날 길이 BLE 끊김 콜백에는 없다 - 서비스가 없으면 콜백을 받을 주체도 없다.
 * 그래서 밖에서 15 분마다 찔러 준다.
 *
 * 이미 돌고 있으면 서비스의 onStartCommand 가 중복 가드에 걸려 바로 빠져나오고,
 * 나가는 길에 연결을 한 번 챙긴다. 그래서 이 워커의 주기가 곧 재연결 점검 주기다.
 *
 * **네트워크 제약을 걸지 않는다.** 서비스를 되살리는 데 망은 필요 없고, 걸어 두면
 * 오히려 망이 없을 때 - 되살릴 필요가 더 큰 때 - 돌지 않는다.
 */
class SendDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 등록된 기기가 없으면 띄우지 않는다. 붙을 데가 없는 서비스를 올리면
            // 알림만 띄운 채 아무 일도 하지 않는다.
            val deviceMac = DataStoreManager.getDeviceMac().first()
            if (deviceMac.isNullOrBlank()) {
                Log.d("WORKER", "등록된 기기가 없다. 서비스를 띄우지 않는다.")
                return Result.success()
            }

            val serviceIntent = Intent(applicationContext, AlwaysA2RTService::class.java)
            ContextCompat.startForegroundService(applicationContext, serviceIntent)
            Log.e("WORKER", "워커에서 startForegroundService 호출 완료")
            Result.success()

        } catch (e: Exception) {
            Log.e("WORKER", "워커 에러 발생 : ${e.message}")
            Result.failure()
        }
    }
}


//package kr.co.uxn.agms_p_a2rt.ble
//
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.content.ContextCompat
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
//import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
//import kr.co.uxn.agms_p_a2rt.room.AppDatabase
//
//class SendDataWorker(val context: Context, workerParams: WorkerParameters) :
//    Worker(context, workerParams) {
//    private val localDbRepository = AppDatabase.getInstance(context)
//
//    override fun doWork(): Result {
//        try {
//            CoroutineScope(Dispatchers.IO).launch {
//                Log.e("Worker", "워커 실행")
////                val userId = DataStoreManager.getUserId().first() ?: -1
////                val lastTime = tokenRetrofit.getLastTime(userId)
////                if (lastTime.isSuccessful) {
////                    val lastTimeBody = lastTime.body()
////                    if (lastTimeBody != null) {
////                        Log.e("TEST", "워커에서 호출한 lastTime : ${lastTimeBody.toString()}")
////                        val sendData = tokenRetrofit.SendData(lastTimeBody.lastTime)
////                        if (sendData.isSuccessful) {
////                            val sendDataBody = sendData.body()
////                            if (sendDataBody != null) {
////                                localDbRepository.sendDataAfterTime(lastTimeBody.lastTime)
////                            }
////                        }
////                    }
////                }
//
//                // 서비스 실행
//                val device = DataStoreManager.getDeviceMac().first()
//                val userId = DataStoreManager.getUserId().first()
//                Log.e("TEST", "불러온 userId : $userId")
//                Log.e("TEST", "불러온 device : $device")
//                val serviceIntent = Intent(context, AlwaysService::class.java).apply {
//                    putExtra("device", device)
//                    putExtra("userId", userId)
//                }
//
//                ContextCompat.startForegroundService(context, serviceIntent)
//                Log.e("SERVICE", "메인액티비티 startForegroundService call!")
//
//            }
//            return Result.success()
//        } catch (e: Exception) {
//            Log.e("Worker", "워커 에러 발생 : ${e.message}")
//            return Result.failure()
//        }
//    }
//}