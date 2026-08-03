package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestLinkDevice
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.components.isKorea

@Composable
fun StabilizationCompleteScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    var isNotiStabilization = false
    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    LaunchedEffect(Unit) {
        DataStoreManager.deleteRoute()
        DataStoreManager.saveRoute("StabilizationCompleteScreen")
        val route = DataStoreManager.getRoute().first()
        Log.e("TEST", "안정화 완료 화면에서 Route : ${route}")
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = colorResource(R.color.background_white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(280.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.stabilization_complete_title),
                fontSize = 30.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.stabilization_complete_sub_title),
                fontSize = 30.sp,
                color = Color.Black
            )


            Spacer(modifier = Modifier.height(260.dp))

            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
//                    painter = painterResource(id = if (isKorean)R.drawable.btn_next else R.drawable.btn_eng_continue),
                    painter = painterResource(id = if (isKorea()) R.drawable.btn_next else R.drawable.btn_eng_next),
                    contentDescription = "다음 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            // 메인화면으로 고정 isMain = true
                            coroutine.launch(Dispatchers.IO) {
                                DataStoreManager.deleteStabilizationCompleted()
                                DataStoreManager.setStabilizationCompleted(true)
                                DataStoreManager.deleteRoute()
                                DataStoreManager.saveRoute("MainScreen/0")
                                DataStoreManager.saveIsMain(true)
                                val isMain = DataStoreManager.getIsMain().first()
                                val route = DataStoreManager.getRoute().first()
                                Log.e("TEST", "안정화 화면에서 버튼 눌럿을시isMain : ${isMain}")
                                Log.e("TEST", "안정화 화면에서 버튼 눌럿을시 Route : ${route}")

                                // 알림 혈당 설정
//                                val userId = DataStoreManager.getUserId().first() ?: -1
//                                try {
//                                    val detectorList = localDbRepository?.dataDao()?.getListAfterLastTime(userId, 0)
//                                    Log.d("StabilizationCompleteScreen, 알림혈당 기본값 설정", "detectorList is null or empty!")
//                                    if (detectorList.isNullOrEmpty()) {
//                                        DataStoreManager.setTargetLowGlucose(70)
//                                        DataStoreManager.setTargetHighGlucose(170)
//                                    } else {
//                                        Log.d("TEST", "detectorList is exist : ${detectorList}")
//                                    }
//
//                                } catch (e: Exception) {
//                                    Log.d("TEST","룸 DB에러 발생 : ${e.message}")
//                                }

                                withContext(Dispatchers.Main){
                                    NotificationManagerCompat.from(context).cancel(90)
                                }
                            }

                            coroutine.launch(Dispatchers.Main) {
                                navController.navigate("MainScreen/${0}") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                )
            }
        }
    }
}
