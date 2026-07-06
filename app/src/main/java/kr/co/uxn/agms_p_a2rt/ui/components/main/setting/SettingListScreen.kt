package kr.co.uxn.agms_p_a2rt.ui.components.main.setting

import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.UserInfoCache
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.components.main.AlwaysDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.system.exitProcess
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestDeleteOauthUserInfo
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestDeleteUserInfo
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel

// 테마 컬러
val BackgroundGray = Color(0xFFF2F2F2)
val TextGray = Color(0xFF888888)
val PrimaryOrange = Color(0xFFFCA937)
val AlertRed = Color(0xFFFF5252)

// 1. 네비게이션 호스트
@Composable
fun SettingListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    bleViewModel: BleViewModel,
    onSensorEnded: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "settings_main",
        modifier = modifier.fillMaxSize().background(BackgroundGray)
    ) {
        composable("settings_main") { SettingsMainScreen(navController, bleViewModel) }
        composable("user_info") { UserInfoScreen(navController) }
        composable("sensor_info") {
            SensorInfoScreen(navController, bleViewModel, onSensorEnded)
        }
        composable("delete_account") {
            DeleteAccountScreen(navController, bleViewModel)
        }
        composable("alarm_settings") { AlarmSettingsScreen(navController) }
        composable("app_info") { AppInfoScreen(navController) }
    }
}

// ==========================================
// [첫 번째 화면] 설정 메인 화면
// ==========================================
@Composable
fun SettingsMainScreen(navController: NavHostController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val showLogOutDialog = remember { mutableStateOf(false) }

    if (showLogOutDialog.value) {
        AlwaysDialog(
            onConfirm = {
                showLogOutDialog.value = false
                coroutineScope.launch {
                    // 로그아웃 api 전송
                    try {
                        withContext(Dispatchers.IO) {
                            val result2 = tokenRetrofit.logout()
                        }
                    } catch (e: Exception) {
                        Log.d("TEST", "로그아웃 API통신 실패 : ${e.message}")
                    }

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
                    // 2. 데이터 전송
                    // 3. 로그인 화면으로 이동
                    navController.navigate("Login") {
                        popUpTo(0)
                    }
                    // 앱 강제 종료
                    android.os.Process.killProcess(Process.myPid())
                    exitProcess(0)
                }
            },
            onDismiss = {
                showLogOutDialog.value = false
            },
            title = stringResource(R.string.dialog_log_out_title),
            content = stringResource(R.string.dialog_log_out_content)
        )
    }

    LaunchedEffect(Unit) {
        try {
            val userInfo = withContext(Dispatchers.IO) {
                UserInfoCache.getUserInfo()
            }
            if (userInfo != null) {
                name.value = userInfo.name
                email.value = userInfo.email
            }
        } catch (e: Exception) {
            Log.e("TEST", "SettingsMain 사용자 정보 로드 실패 : ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 사용자 프로필 영역 (흰색 배경)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(name.value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("계정: ${email.value}", fontSize = 14.sp, color = TextGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 메뉴 리스트 영역 (회색 배경)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 사용자 정보 & 센서 정보 (하나의 카드로 묶음)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    SettingsMenuRow("사용자 정보") { navController.navigate("user_info") }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = BackgroundGray
                    )
                    SettingsMenuRow("센서 정보") { navController.navigate("sensor_info") }
                }
            }

            // 2. 단일 메뉴 카드들
            SettingsSingleCard("알림 설정") { navController.navigate("alarm_settings") }
            SettingsSingleCard("앱 정보") { navController.navigate("app_info") }

            // 3. 로그아웃
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showLogOutDialog.value = true
                    }
            ) {
                Text(
                    text = "로그아웃",
                    color = AlertRed,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// 메뉴 공통 컴포넌트
@Composable
fun SettingsSingleCard(title: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsMenuRow(title = title, onClick = onClick)
    }
}

@Composable
fun SettingsMenuRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
    }
}

// ==========================================
// 공통 서브 화면 헤더 (뒤로가기 + 타이틀)
// ==========================================
@Composable
fun SubScreenHeader(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(8.dp)
        )
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(40.dp)) // 타이틀 중앙 정렬을 위한 여백
    }
}

// ==========================================
// [두 번째 화면] 사용자 정보
// ==========================================
@Composable
fun UserInfoScreen(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val sex = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val diabetesType = remember { mutableStateOf("") }
    val targetGlucoseRange = remember { mutableStateOf("") }
    val showDeleteAccountDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val localDbRepository by lazy { AppDatabase.getInstance(context) }

    LaunchedEffect(Unit) {
        try {
            val userInfo = withContext(Dispatchers.IO) {
                UserInfoCache.getUserInfo()
            }
            if (userInfo != null) {
                name.value = userInfo.name
                email.value = userInfo.email
                sex.value = userInfo.sex
                age.value = userInfo.age.toString()
                height.value = userInfo.height.toString()
                weight.value = userInfo.weight.toString()
                diabetesType.value = userInfo.diabetesType
                targetGlucoseRange.value =
                    "${userInfo.targetGlucoseMin} ~ ${userInfo.targetGlucoseMax} mg/dL"
            }
        } catch (e: Exception) {
            Log.e("TEST", "UserInfo 사용자 정보 로드 실패 : ${e.message}")
        }
    }

    if (showDeleteAccountDialog.value) {
        AlwaysDialog(
            onConfirm = {
                showDeleteAccountDialog.value = false

                coroutineScope.launch(Dispatchers.IO) {
                    val type = DataStoreManager.getType().first() ?: -1
                    val userId = DataStoreManager.getUserId().first() ?: -1

                    if (type != -1) {
                        Log.e("TEST", "type : $type")
                        if (type == 1803) {
                            try {
                                val deleteUser =
                                    tokenRetrofit.deleteUser(RequestDeleteUserInfo(userId))
                                val deleteUserBody = deleteUser.body()
                                Log.e("TEST", "deleteUserBody : $deleteUserBody")
                                if (deleteUser.isSuccessful) {
                                    if (deleteUserBody != null) {
                                        if (deleteUserBody.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.toast_delete_account),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            // 토큰정리 및 앱 종료

                                            localDbRepository?.dataDao()
                                                ?.deleteUserValueTable(userId)
                                            localDbRepository?.dataDao()
                                                ?.deleteUserGlucoseTable(userId)
                                            localDbRepository?.dataDao()
                                                ?.deleteUserCalibrationTable(userId)

                                            Log.w("TEST", "sensorOff 성공")
                                            DataStoreManager.saveIsMain(false)
                                            DataStoreManager.deleteRoute()
                                            DataStoreManager.saveRoute("Splash")
                                            Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                                            Log.e(
                                                "TEST",
                                                "DS에 저장된 Route : ${
                                                    DataStoreManager.getRoute().first()
                                                }"
                                            )
                                            DataStoreManager.deleteAccessToken()
                                            DataStoreManager.deleteRefreshToken()
                                            DataStoreManager.deleteUserId()
                                            DataStoreManager.deleteDeviceMac()
                                            DataStoreManager.deleteStartTime()
                                            DataStoreManager.deleteEndTime()
                                            withContext(Dispatchers.Main) {
                                                // 1. 서비스 종료
//                                                bleViewModel.emit("STOP_SERVICE")
                                                // 앱 강제종료
                                                android.os.Process.killProcess(android.os.Process.myPid())
                                                exitProcess(0)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("TEST", "API 에러 : ${deleteUser.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Log.e("TEST", "네트워크 에러 : $e")

                            }
                        } else {
                            try {
                                val deleteOauthUser = tokenRetrofit.deleteOauthUser(
                                    RequestDeleteOauthUserInfo(
                                        userId,
                                        type
                                    )
                                )
                                val deleteOauthUserBody = deleteOauthUser.body()
                                Log.e("TEST", "deleteOauthUserBody : $deleteOauthUserBody")
                                if (deleteOauthUser.isSuccessful) {
                                    if (deleteOauthUserBody != null) {
                                        if (deleteOauthUserBody.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.toast_delete_account),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            // 토큰 정리 및 앱 종료

                                            localDbRepository?.dataDao()
                                                ?.deleteUserValueTable(userId)
                                            localDbRepository?.dataDao()
                                                ?.deleteUserGlucoseTable(userId)
                                            localDbRepository?.dataDao()
                                                ?.deleteUserCalibrationTable(userId)

                                            Log.w("TEST", "sensorOff 성공")
                                            DataStoreManager.saveIsMain(false)
                                            DataStoreManager.deleteRoute()
                                            DataStoreManager.saveRoute("Splash")
                                            Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                                            Log.e(
                                                "TEST",
                                                "DS에 저장된 Route : ${
                                                    DataStoreManager.getRoute().first()
                                                }"
                                            )
                                            DataStoreManager.deleteAccessToken()
                                            DataStoreManager.deleteRefreshToken()
                                            DataStoreManager.deleteUserId()
                                            DataStoreManager.deleteDeviceMac()
                                            DataStoreManager.deleteStartTime()
                                            DataStoreManager.deleteEndTime()
                                            withContext(Dispatchers.Main) {
                                                // 1. 서비스 종료
//                                                bleViewModel.emit("STOP_SERVICE")
                                                // 앱 강제종료
                                                android.os.Process.killProcess(android.os.Process.myPid())
                                                exitProcess(0)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e(
                                        "TEST",
                                        "API 에러 : ${deleteOauthUser.errorBody()?.string()}"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("TEST", "네트워크 에러 : $e")
                            }
                        }
                    }
                }
            },
            onDismiss = {
                showDeleteAccountDialog.value = false
            },
            title = context.getString(R.string.dialog_delete_account),
            content = context.getString(R.string.dialog_delete_ask_again)
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SubScreenHeader("사용자 정보", onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("기본정보", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("이름", name.value)
            InfoRow("이메일", email.value)
//             비밀번호 변경하기 비활성화
//            Text("비밀번호 변경하기", fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp).clickable { })

            Spacer(modifier = Modifier.height(24.dp))

            Text("부가정보", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("성별", sex.value)
            InfoRow("연령", age.value)
            InfoRow("신장", height.value)
            InfoRow("체중", weight.value)
            InfoRow("당뇨 유형", diabetesType.value)
            InfoRow("목표 혈당 범위", targetGlucoseRange.value)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("일반적인 목표 혈당범위는 80~130 mg/dL이며, 식후 최대 혈당은 180 mg/dL미만입니다.", fontSize = 12.sp, color = TextGray)

            Spacer(modifier = Modifier.height(32.dp))
            Text("회원탈퇴", color = AlertRed, fontSize = 14.sp, modifier = Modifier.clickable {
                navController.navigate("delete_account")
            })
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// [세 번째 화면] 센서 정보
// ==========================================
@Composable
fun SensorInfoScreen(
    navController: NavHostController,
    bleViewModel: BleViewModel,
    onSensorEnded: () -> Unit
) {

    val startTime = remember { mutableStateOf("") }
    val remainingTime = remember { mutableStateOf(-1) }
    var serialNumber = remember { mutableStateOf("")}
    val showSensorOffDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }



    LaunchedEffect(Unit) {
        val startTimeMilli = DataStoreManager.getStartTime().first() ?: -1
        val endTimeMilli = DataStoreManager.getEndTime().first() ?: -1
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
        val userId = DataStoreManager.getUserId().first() ?: -1

        val formattedDate = if (startTimeMilli != -1L) {
            Instant.ofEpochMilli(startTimeMilli)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        } else {
            ""
        }
        // 센서 시작 시간 설정
        startTime.value = formattedDate

        val remainingDays = if (startTimeMilli != -1L && endTimeMilli != -1L) {
            val diffMillis = endTimeMilli - System.currentTimeMillis()
            (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        } else {
            -1 // 오류 처리
        }

        // 남은 사용 기간 설정
        remainingTime.value = remainingDays

        // 시리얼번호 가져오기
        withContext(Dispatchers.IO) {
            serialNumber.value = DataStoreManager.getSerialNumber().first() ?: ""
        }
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

                    // 로그아웃 api 전송
//                    try {
//                        withContext(Dispatchers.IO) {
//                            val result2 = tokenRetrofit.logout()
//                        }
//                    } catch (e: Exception) {
//                        Log.d("TEST", "로그아웃 API통신 실패 : ${e.message}")
//                    }

                    try {
                        val sensorOff = tokenRetrofit.doSensorOff(userId)
                        if (sensorOff.isSuccessful) {
                            val sensorOffBody = sensorOff.body()
                            if (sensorOffBody != null) {
                                Log.w("TEST", "sensorOff responseBody : ${sensorOffBody}")
                                if (sensorOffBody.isSuccess) {
                                    /*
                                     * 기존 센서 종료 로직. 필요 시 아래 블록을 복원할 수 있도록 보존한다.
                                    tokenRetrofit.logout()
                                    delay(1000)
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
//                                    DataStoreManager.deleteDeviceMac()
                                    DataStoreManager.setNotiHighGlucose(false)
                                    DataStoreManager.setNotiLowGlucose(false)
                                    DataStoreManager.deleteStartTime()
                                    DataStoreManager.deleteEndTime()
                                    DataStoreManager.deleteDailyCalibrationTime()
                                    DataStoreManager.deleteDailyCalibrationLastTime()
                                    DataStoreManager.setLandScapeMode(false)
                                    DataStoreManager.deleteTargetLowGlucose()
                                    DataStoreManager.deleteTargetHighGlucose()
                                    DataStoreManager.deleteEmail()
                                    withContext(Dispatchers.Main) {
                                        // 1. 서비스 종료
//                                        bleViewModel.emit("STOP_SERVICE")
                                        // 앱 강제 종료
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                        exitProcess(0)
                                    }
                                     */

                                    // 새 센서 종료 로직: 로그인 정보와 토큰은 유지한다.
                                    localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                                    DataStoreManager.saveIsSensorEnded(true)
                                    DataStoreManager.deleteDeviceMac()
                                    DataStoreManager.deleteUserDeviceId()
                                    DataStoreManager.deleteSerialNumber()
                                    DataStoreManager.deleteStartTime()
                                    DataStoreManager.deleteMeasurementTime()
                                    DataStoreManager.deleteEndTime()
                                    DataStoreManager.deleteDailyCalibrationTime()
                                    DataStoreManager.deleteDailyCalibrationLastTime()
                                    DataStoreManager.setNotiHighGlucose(false)
                                    DataStoreManager.setNotiLowGlucose(false)
                                    DataStoreManager.setLandScapeMode(false)

                                    withContext(Dispatchers.Main) {
                                        bleViewModel.emit("STOP_SERVICE")
                                        onSensorEnded()
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
                            Toast.makeText(context, context.getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            title = stringResource(R.string.dialog_sensor_off_title),
            content = stringResource(R.string.dialog_sensor_off_content)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("센서 정보", onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            InfoRow("센서 시작일", startTime.value)
            InfoRow("남은 사용 기간", "${remainingTime.value}일")
            InfoRow("시리얼 번호", serialNumber.value)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("센서종료", color = AlertRed, fontSize = 16.sp, modifier = Modifier.clickable {
                showSensorOffDialog.value = true
            })
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Text(value, fontSize = 16.sp, color = TextGray)
    }
}

// ==========================================
// [네 번째 화면] 알림 설정
// ==========================================
@Composable
fun AlarmSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val checkedForHighGlucose = remember { mutableStateOf(false) }
    val checkedForLowGlucose = remember { mutableStateOf(false) }
    val checkedForLostSignal = remember { mutableStateOf(false) }
    val checkedForExpiredSensor = remember { mutableStateOf(false) }
    val checkedForStabilization = remember { mutableStateOf(false) }
    val checkedForCalibration = remember { mutableStateOf(false) }
    val checkedForSilentMode = remember { mutableStateOf(false) }
    val targetLowGlucose = remember { mutableStateOf("") }
    val targetHighGlucose = remember { mutableStateOf("") }
    val showSetLowGlucoseDialog = remember { mutableStateOf(false) }
    val showSetHighGlucoseDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val notificationSettings = withContext(Dispatchers.IO) {
            // DS로부터 값 불러오기
            val verifiedDSHigh = DataStoreManager.getNotiHighGlucose().first() ?: false
            val verifiedDSLow = DataStoreManager.getNotiLowGlucose().first() ?: false
            val verifiedDSLostSignal = DataStoreManager.getNotiLostSignal().first() ?: true
            val verifiedDSExpiredSensor = DataStoreManager.getNotiExpiredSensor().first() ?: true
            val verifiedDSStabilization = DataStoreManager.getNotiStabilization().first() ?: true
            val verifiedDSCalibration = DataStoreManager.getNotiCalibration().first() ?: true
            val verifiedDSSilentMode = DataStoreManager.getNotiSilentMode().first()
            val verifiedDSTargetLowGlucose = DataStoreManager.getTargetLowGlucose().first() ?: 70
            val verifiedDSTargetHighGlucose = DataStoreManager.getTargetHighGlucose().first() ?: 170
            val verifiedDSDailyCalibrationTime = DataStoreManager.getDailyCalibrationTime().first() ?: "오전 11:00"

            // 로그 띄우기
            Log.e("NOTI", "After High : ${verifiedDSHigh}, Low : ${verifiedDSLow} " +
                    "\n Lost : ${verifiedDSLostSignal} ExpiredSensor : ${verifiedDSExpiredSensor}" +
                    "\n Stabilization : ${verifiedDSStabilization} Calibration : ${verifiedDSCalibration}" +
                    "\n Target High Glucose : ${verifiedDSTargetHighGlucose} Target Low Glucose : ${verifiedDSTargetLowGlucose}")

            NotificationSettings(
                highGlucose = verifiedDSHigh,
                lowGlucose = verifiedDSLow,
                lostSignal = verifiedDSLostSignal,
                expiredSensor = verifiedDSExpiredSensor,
                stabilization = verifiedDSStabilization,
                calibration = verifiedDSCalibration,
                silentMode = verifiedDSSilentMode,
                targetLowGlucose = verifiedDSTargetLowGlucose.toString(),
                targetHighGlucose = verifiedDSTargetHighGlucose.toString()
            )
        }

        checkedForHighGlucose.value = notificationSettings.highGlucose
        checkedForLowGlucose.value = notificationSettings.lowGlucose
        checkedForLostSignal.value = notificationSettings.lostSignal
        checkedForExpiredSensor.value = notificationSettings.expiredSensor
        checkedForStabilization.value = notificationSettings.stabilization
        checkedForCalibration.value = notificationSettings.calibration
        checkedForSilentMode.value = notificationSettings.silentMode
        targetLowGlucose.value = notificationSettings.targetLowGlucose
        targetHighGlucose.value = notificationSettings.targetHighGlucose
    }

    if (showSetLowGlucoseDialog.value) {
        TargetGlucoseDialog(
            title = "저혈당 알림",
            value = targetLowGlucose.value,
            onValueChange = { targetLowGlucose.value = it },
            onDismiss = { showSetLowGlucoseDialog.value = false },
            onConfirm = {
                val target = targetLowGlucose.value
                if (target.isBlank()) {
                    Toast.makeText(context, "혈당값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                val targetValue = target.toIntOrNull()
                if (targetValue == null) {
                    Toast.makeText(context, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                showSetLowGlucoseDialog.value = false
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setTargetLowGlucose(targetValue)
                }
            }
        )
    }

    if (showSetHighGlucoseDialog.value) {
        TargetGlucoseDialog(
            title = "고혈당 알림",
            value = targetHighGlucose.value,
            onValueChange = { targetHighGlucose.value = it },
            onDismiss = { showSetHighGlucoseDialog.value = false },
            onConfirm = {
                val target = targetHighGlucose.value
                if (target.isBlank()) {
                    Toast.makeText(context, "혈당값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                val targetValue = target.toIntOrNull()
                if (targetValue == null) {
                    Toast.makeText(context, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                showSetHighGlucoseDialog.value = false
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setTargetHighGlucose(targetValue)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SubScreenHeader("알림 설정", onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            AlarmSwitchRow("무음 모드", checkedForSilentMode.value) {
                checkedForSilentMode.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiSilentMode(it)
                }
            }
            AlarmSwitchRow(
                title = "저혈당 알림",
                checked = checkedForLowGlucose.value,
                subValue = "${targetLowGlucose.value} mg/dL",
                onRowClick = { showSetLowGlucoseDialog.value = true },
                onCheckedChange = {
                    checkedForLowGlucose.value = it
                    coroutineScope.launch(Dispatchers.IO) {
                        DataStoreManager.setNotiLowGlucose(it)
                    }
                }
            )
            AlarmSwitchRow(
                title = "고혈당 알림",
                checked = checkedForHighGlucose.value,
                subValue = "${targetHighGlucose.value} mg/dL",
                onRowClick = { showSetHighGlucoseDialog.value = true },
                onCheckedChange = {
                    checkedForHighGlucose.value = it
                    coroutineScope.launch(Dispatchers.IO) {
                        DataStoreManager.setNotiHighGlucose(it)
                    }
                }
            )
            AlarmSwitchRow("신호 소실", checkedForLostSignal.value) {
                checkedForLostSignal.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiLostSignal(it)
                }
            }
            AlarmSwitchRow("센서 만료", checkedForExpiredSensor.value) {
                checkedForExpiredSensor.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiExpiredSensor(it)
                }
            }
            AlarmSwitchRow("센서 안정화", checkedForStabilization.value) {
                checkedForStabilization.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiStabilization(it)
                }
            }
        }
    }
}

@Composable
fun AlarmSwitchRow(
    title: String,
    checked: Boolean,
    subValue: String? = null,
    onRowClick: (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onRowClick != null) {
                        Modifier.clickable { onRowClick() }
                    } else {
                        Modifier
                    }
                )
        ) {
            Text(title, fontSize = 16.sp)
            if (subValue != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(subValue, fontSize = 14.sp, color = TextGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextGray, modifier = Modifier.size(14.dp))
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colorResource(R.color.main),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

private data class NotificationSettings(
    val highGlucose: Boolean,
    val lowGlucose: Boolean,
    val lostSignal: Boolean,
    val expiredSensor: Boolean,
    val stabilization: Boolean,
    val calibration: Boolean,
    val silentMode: Boolean,
    val targetLowGlucose: String,
    val targetHighGlucose: String
)

@Composable
private fun TargetGlucoseDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val primaryOrange = Color(0xFFE67A15)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1E27)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = {
                        Text(
                            text = "mg/dL",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryOrange,
                        cursorColor = primaryOrange,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "취소",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "확인",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// [다섯 번째 화면] 앱 정보
// ==========================================
@Composable
fun AppInfoScreen(navController: NavHostController) {

    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("앱 정보", onBackClick = { navController.popBackStack() })

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // 로고 대체 텍스트 (실제 앱에서는 Image 컴포저블 사용)
            Image(
                modifier = Modifier.fillMaxWidth(0.4f),
                painter = painterResource(id = R.drawable.always_icon_rt),
                contentDescription = "App Logo",
            )
//            Text("Always RT", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("버전 $versionName", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("현재 최신버전입니다.", fontSize = 12.sp, color = TextGray)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("의료기기 허가정보", fontSize = 14.sp, modifier = Modifier.clickable {
                    Toast.makeText(context, "의료기기 허가정보는 추후 기입 예정입니다.", Toast.LENGTH_SHORT).show()
                })
                Text("서비스 이용약관", fontSize = 14.sp, modifier = Modifier.clickable {
                    Toast.makeText(context, "서비스 이용약관은 추후 제공될 예정입니다.", Toast.LENGTH_SHORT).show()
                })
                Text("개인정보 처리방침", fontSize = 14.sp, modifier = Modifier.clickable {
                    Toast.makeText(context, "개인정보 처리방침은 추후 제공될 예정입니다.", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}
