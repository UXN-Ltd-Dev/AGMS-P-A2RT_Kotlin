package kr.co.uxn.agms_p_a2rt.ble

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase

class SendDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val localDbRepository = AppDatabase.getInstance(context)

    override suspend fun doWork(): Result {
        return try {
            Log.e("WORKER", "워커 실행")

            // 서비스 실행 코드
//            val serviceIntent = Intent(applicationContext, AlwaysService::class.java)
//            ContextCompat.startForegroundService(applicationContext, serviceIntent)
//            Log.e("WORKER", "워커에서 startForegroundService 호출 완료")

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