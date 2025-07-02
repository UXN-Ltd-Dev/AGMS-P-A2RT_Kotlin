package kr.co.uxn.agms_p.ui.components.main.setting

import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.AlwaysDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, paddingValues: PaddingValues, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val showLogOutDialog = remember { mutableStateOf(false) }
    val showSensorOffDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val checkedForLandscapeMode = remember { mutableStateOf(false) }

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val verifiedDSLandscapeMode = DataStoreManager.getLandScapeMode().first() ?: false
            checkedForLandscapeMode.value = verifiedDSLandscapeMode
        }
    }


    if (showLogOutDialog.value) {
        AlwaysDialog(
            onConfirm = {
                showLogOutDialog.value = false
                coroutineScope.launch {
                    // 0. 토큰 정리
                    // 메인화면으로 고정 isMain = true
                    DataStoreManager.saveIsMain(false)
                    DataStoreManager.deleteRoute()
                    DataStoreManager.saveRoute("Splash")
                    Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                    Log.e("TEST", "DS에 저장된 Route는${DataStoreManager.getRoute().first()}")
                    DataStoreManager.deleteAccessToken()
                    DataStoreManager.deleteRefreshToken()
                    DataStoreManager.deleteUserId()
                    DataStoreManager.deleteDeviceMac()
//                    DataStoreManager.deleteStartTime()
//                    DataStoreManager.deleteEndTime()
                    DataStoreManager.deleteTargetLowGlucose()
                    DataStoreManager.deleteTargetHighGlucose()
                    DataStoreManager.deleteEmail()

                    // 1. 서비스 종료
                    bleViewModel.emit("STOP_SERVICE")
                    // 앱 강제 종료
                    Process.killProcess(Process.myPid())
                    exitProcess(0)
                }
                // 2. 데이터 전송
                // 3. 로그인 화면으로 이동
                navController.navigate("Login") {
                    popUpTo(0)
                }
            },
            onDismiss = {
                showLogOutDialog.value = false
            },
            title = "로그아웃",
            content = "정말 로그아웃하시겠습니까?"
        )
    }

    if (showSensorOffDialog.value) {
        AlwaysDialog(
            onDismiss = {
                showSensorOffDialog.value = false
            },
            onConfirm = {
                showSensorOffDialog.value = false
                coroutineScope.launch(Dispatchers.IO) {
                    // 0. 토큰 정리
                    val userId = DataStoreManager.getUserId().first() ?: -1
                    try {
                        val sensorOff = tokenRetrofit.doSensorOff(userId)
                        if (sensorOff.isSuccessful) {
                            val sensorOffBody = sensorOff.body()
                            if (sensorOffBody != null) {
                                Log.w("TEST", "sensorOff responseBody : ${sensorOffBody}")
                                if (sensorOffBody.isSuccess) {
                                    // userId의 db삭제
                                    localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                                    Log.w("TEST", "sensorOff 성공")
                                    DataStoreManager.saveIsMain(false)
                                    DataStoreManager.deleteRoute()
                                    DataStoreManager.saveRoute("Splash")
                                    Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                                    DataStoreManager.deleteAccessToken()
                                    DataStoreManager.deleteRefreshToken()
                                    DataStoreManager.deleteUserId()
                                    DataStoreManager.deleteDeviceMac()
                                    DataStoreManager.deleteStartTime()
                                    DataStoreManager.deleteEndTime()
                                    DataStoreManager.deleteDailyCalibrationTime()
                                    DataStoreManager.setLandScapeMode(false)
                                    DataStoreManager.deleteTargetLowGlucose()
                                    DataStoreManager.deleteTargetHighGlucose()
                                    DataStoreManager.deleteEmail()
                                    withContext(Dispatchers.Main) {
                                        // 1. 서비스 종료
                                        bleViewModel.emit("STOP_SERVICE")
                                        // 앱 강제 종료
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                        exitProcess(0)
                                    }
                                } else {
                                    Log.w("TEST", "sensorOff 실패")
                                }
                            }
                        } else {
                            Log.w("TEST", "sensorOff API통신 실패 : ${sensorOff.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.d("TEST", "sensorOff API통신 실패 : ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            title = "센서 종료",
            content = "센서 연결을 종료하시겠습니까?"
        )
    }



    Column(
        modifier = Modifier.fillMaxSize()
        .background(Color(0xF7F7FB))
        .verticalScroll(rememberScrollState())
        .padding(paddingValues)
    ) {
        Divider()
        Box(
            modifier = Modifier.clickable {
                navController.navigate("MyInfoScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "내 정보",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("SensorInfoScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "센서 정보",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("VersionInfoScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "버전 정보",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("NotificationScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "알림 설정",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()


        Box(
            modifier = Modifier.clickable {
                navController.navigate("TermsAndConditionsScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "이용 약관",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("PrivacyPolicyScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "개인정보 처리방침",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()

        Box(
            modifier = Modifier.clickable {
                showLogOutDialog.value = true
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "로그아웃",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                showSensorOffDialog.value = true
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "센서 종료",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("DeleteAccountScreen")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "계정 삭제",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()
        Box() {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "그래프 가로모드",
                    fontSize = 16.sp
                )
                Switch(
                    checked = checkedForLandscapeMode.value,
                    onCheckedChange = {
                        checkedForLandscapeMode.value = it
                        coroutineScope.launch(Dispatchers.IO) {
                            DataStoreManager.setLandScapeMode(it)
                        }
                    }
                )
            }
        }
        Divider()
    }
}

