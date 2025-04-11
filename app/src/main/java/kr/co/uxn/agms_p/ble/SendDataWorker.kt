package kr.co.uxn.agms_p.ble

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.room.AppDatabase

class SendDataWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val localDbRepository = AppDatabase.getInstance(context)
    override fun doWork(): Result {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                Log.e("Worker", "워커 실행")
                val userId = DataStoreManager.getUserId().first() ?: -1
                val lastTime = tokenRetrofit.getLastTime(userId)
                if (lastTime.isSuccessful) {
                    val lastTimeBody = lastTime.body()
                    if (lastTimeBody != null) {
                        Log.e("TEST", "워커에서 호출한 lastTime : ${lastTimeBody.toString()}")
//                        val sendData = tokenRetrofit.SendData(lastTimeBody.lastTime)
//                        if (sendData.isSuccessful) {
//                            val sendDataBody = sendData.body()
//                            if (sendDataBody != null) {
//                                localDbRepository.sendDataAfterTime(lastTimeBody.lastTime)
//                            }
//                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("Worker", "워커 에러 발생 : ${e.message}")
            return Result.failure()
        }
    }
}