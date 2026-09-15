package kr.co.uxn.agms_p_a2rt.ui.components.main.setting

import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import kr.co.uxn.agms_p_a2rt.api.token.CachedUserInfo
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestUpdateUser
import kr.co.uxn.agms_p_a2rt.ui.components.isKorea
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
            Text( if(isKorea()) { "계정: ${email.value}"} else {"Account: ${email.value}"} , fontSize = 14.sp, color = TextGray)
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
                    SettingsMenuRow(stringResource(R.string.setting_menu_title_user_info)) { navController.navigate("user_info") }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = BackgroundGray
                    )
                    SettingsMenuRow(stringResource(R.string.settings_sensor_info)) { navController.navigate("sensor_info") }
                }
            }

            // 2. 단일 메뉴 카드들
            SettingsSingleCard(stringResource(R.string.setting_menu_title_alarm_settings)) { navController.navigate("alarm_settings") }
            SettingsSingleCard(stringResource(R.string.setting_menu_title_app_info)) { navController.navigate("app_info") }

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
                    text = stringResource(R.string.settings_log_out),
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
    val age = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    // 성별과 당뇨 유형은 서버 코드로 들고 있는다. 화면에 내걸 때만 문자열로 바꾸면
    // 언어 설정에 따라 저절로 갈리고, 보낼 때 되돌릴 일도 없다.
    val sexCode = remember { mutableStateOf(SEX_NONE) }
    val diabetesCode = remember { mutableStateOf(DIABETES_UNKNOWN) }
    val targetLow = remember { mutableStateOf("") }
    val targetHigh = remember { mutableStateOf("") }
    val userId = remember { mutableStateOf(-1) }

    val showDeleteAccountDialog = remember { mutableStateOf(false) }
    val editing = remember { mutableStateOf<UserInfoField?>(null) }
    val isSubmitting = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val localDbRepository by lazy { AppDatabase.getInstance(context) }

    LaunchedEffect(Unit) {
        try {
            val userInfo = withContext(Dispatchers.IO) {
                UserInfoCache.getUserInfo()
            }
            if (userInfo != null) {
                userId.value = userInfo.userId
                name.value = userInfo.name
                email.value = userInfo.email
                age.value = userInfo.age.toString()
                height.value = userInfo.height.toString()
                weight.value = userInfo.weight.toString()
                sexCode.value = sexCodeOf(userInfo.sex)
                diabetesCode.value = diabetesCodeOf(userInfo.diabetesType)
                targetLow.value = userInfo.targetGlucoseMin.toString()
                targetHigh.value = userInfo.targetGlucoseMax.toString()
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
        SubScreenHeader(stringResource(R.string.setting_menu_title_user_info), onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(stringResource(R.string.user_info_title), color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            EditableInfoRow(stringResource(R.string.user_info_name), name.value) { editing.value = UserInfoField.NAME }
            EditableInfoRow(stringResource(R.string.user_info_email), email.value) { editing.value = UserInfoField.EMAIL }
//             비밀번호 변경하기 비활성화
//            Text("비밀번호 변경하기", fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp).clickable { })

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.user_info_sub_title), color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            EditableInfoRow(stringResource(R.string.user_info_gender), stringResource(sexLabelRes(sexCode.value))) { editing.value = UserInfoField.SEX }
            EditableInfoRow(stringResource(R.string.user_info_age), age.value) { editing.value = UserInfoField.AGE }
            EditableInfoRow(stringResource(R.string.user_info_height), height.value) { editing.value = UserInfoField.HEIGHT }
            EditableInfoRow(stringResource(R.string.user_info_weight), weight.value) { editing.value = UserInfoField.WEIGHT }
            EditableInfoRow(stringResource(R.string.user_info_diabetes_type), stringResource(diabetesLabelRes(diabetesCode.value))) { editing.value = UserInfoField.DIABETES }
            EditableInfoRow(
                stringResource(R.string.user_info_target_glucose_range),
                "${targetLow.value} ~ ${targetHigh.value} mg/dL"
            ) { editing.value = UserInfoField.TARGET_RANGE }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.user_info_description), fontSize = 12.sp, color = TextGray)

            Spacer(modifier = Modifier.height(32.dp))
            Text(stringResource(R.string.user_info_delete_account), color = AlertRed, fontSize = 14.sp, modifier = Modifier.clickable {
                navController.navigate("delete_account")
            })

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val trimmedName = name.value.trim()
                    val trimmedEmail = email.value.trim()
                    val ageInt = age.value.trim().toIntOrNull()
                    val heightInt = height.value.trim().toIntOrNull()
                    val weightInt = weight.value.trim().toIntOrNull()
                    val lowInt = targetLow.value.trim().toIntOrNull()
                    val highInt = targetHigh.value.trim().toIntOrNull()

                    // 빈 칸이나 뒤집힌 범위를 그대로 보내면 서버에 잘못된 값이 남는다.
                    val valid = trimmedName.isNotEmpty() && trimmedEmail.isNotEmpty() &&
                        ageInt != null && heightInt != null && weightInt != null &&
                        lowInt != null && highInt != null && lowInt < highInt

                    if (!valid) {
                        Toast.makeText(context, R.string.toast_user_info_invalid, Toast.LENGTH_SHORT).show()
                    } else {
                        isSubmitting.value = true
                        coroutineScope.launch {
                            try {
                                val id = if (userId.value != -1) {
                                    userId.value
                                } else {
                                    DataStoreManager.getUserId().first() ?: -1
                                }

                                val response = withContext(Dispatchers.IO) {
                                    tokenRetrofit.updateUser(
                                        RequestUpdateUser(
                                            userId = id,
                                            email = trimmedEmail,
                                            name = trimmedName,
                                            sex = sexCode.value,
                                            age = ageInt!!,
                                            height = heightInt!!,
                                            weight = weightInt!!,
                                            diabetesType = diabetesCode.value,
                                            targetGlucoseMin = lowInt!!,
                                            targetGlucoseMax = highInt!!
                                        )
                                    )
                                }

                                val body = response.body()
                                if (response.isSuccessful && body?.isSuccess == true) {
                                    // 캐시를 함께 고치지 않으면 UserInfoCache 가 예전 값을 계속 내준다.
                                    withContext(Dispatchers.IO) {
                                        DataStoreManager.saveCachedUserInfo(
                                            CachedUserInfo(
                                                userId = id,
                                                email = trimmedEmail,
                                                name = trimmedName,
                                                sex = sexServerValue(sexCode.value),
                                                age = ageInt,
                                                height = heightInt,
                                                weight = weightInt,
                                                diabetesType = diabetesServerValue(diabetesCode.value),
                                                targetGlucoseMin = lowInt,
                                                targetGlucoseMax = highInt
                                            )
                                        )
                                    }
                                    Toast.makeText(context, R.string.toast_user_info_updated, Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Log.e("TEST", "사용자 정보 수정 실패 : ${response.code()} ${response.errorBody()?.string()}")
                                    Toast.makeText(context, R.string.toast_user_info_update_failed, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("TEST", "사용자 정보 수정 네트워크 에러 : ${e.message}")
                                Toast.makeText(context, R.string.toast_user_info_update_failed, Toast.LENGTH_SHORT).show()
                            } finally {
                                isSubmitting.value = false
                            }
                        }
                    }
                },
                enabled = !isSubmitting.value,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    stringResource(R.string.user_info_edit),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    UserInfoEditDialogs(
        editing = editing,
        name = name,
        email = email,
        age = age,
        height = height,
        weight = weight,
        sexCode = sexCode,
        diabetesCode = diabetesCode,
        targetLow = targetLow,
        targetHigh = targetHigh
    )
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

    val isKorea = isKorea()
    val remainingTimeText = remember(remainingTime.value, isKorea) {
        val days = remainingTime.value
        if (isKorea) {
            "${days}일"
        } else {
            "$days ${if (days == 1) "day" else "days"}"
        }
    }



    LaunchedEffect(Unit) {
        val startTimeMilli = DataStoreManager.getStartTime().first() ?: -1
        val endTimeMilli = DataStoreManager.getEndTime().first() ?: -1
        val formatter = if (isKorea) DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN) else DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

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
                                    localDbRepository?.dataDao()?.deleteUserGlucoseAlertTable(userId)
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
        SubScreenHeader(stringResource(R.string.setting_menu_title_sensor_info), onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            InfoRow(stringResource(R.string.sensor_info_start_time), startTime.value)
            InfoRow(stringResource(R.string.sensor_info_left_time), remainingTimeText)
            InfoRow(stringResource(R.string.sensor_info_serial_number), serialNumber.value)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(stringResource(R.string.sensor_info_sensor_end), color = AlertRed, fontSize = 16.sp, modifier = Modifier.clickable {
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
            title = stringResource(R.string.alarm_setting_low_glucose),
            value = targetLowGlucose.value,
            onValueChange = { targetLowGlucose.value = it },
            onDismiss = { showSetLowGlucoseDialog.value = false },
            onConfirm = {
                val target = targetLowGlucose.value
                if (target.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.toast_enter_glucose), Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                val targetValue = target.toIntOrNull()
                if (targetValue == null) {
                    Toast.makeText(context, context.getString(R.string.toast_enter_only_number), Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                // 범위 설정(총괄 때 활성화)
//                if (targetValue >= 60 && targetValue <= 150) {
//                    Toast.makeText(context, context.getString(R.string.toast_enter_proper_low_glucose), Toast.LENGTH_SHORT).show()
//                    return@TargetGlucoseDialog
//                }

                showSetLowGlucoseDialog.value = false
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setTargetLowGlucose(targetValue)
                }
            }
        )
    }

    if (showSetHighGlucoseDialog.value) {
        TargetGlucoseDialog(
            title = stringResource(R.string.alarm_setting_high_glucose),
            value = targetHighGlucose.value,
            onValueChange = { targetHighGlucose.value = it },
            onDismiss = { showSetHighGlucoseDialog.value = false },
            onConfirm = {
                val target = targetHighGlucose.value
                if (target.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.toast_enter_glucose), Toast.LENGTH_SHORT).show()
                    return@TargetGlucoseDialog
                }

                val targetValue = target.toIntOrNull()
                if (targetValue == null) {
                    Toast.makeText(context, context.getString(R.string.toast_enter_only_number), Toast.LENGTH_SHORT).show()
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
        SubScreenHeader(stringResource(R.string.alarm_setting_title), onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            AlarmSwitchRow(stringResource(R.string.alarm_setting_silent_mode), checkedForSilentMode.value) {
                checkedForSilentMode.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiSilentMode(it)
                }
            }
            AlarmSwitchRow(
                title = stringResource(R.string.alarm_setting_low_glucose),
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
                title = stringResource(R.string.alarm_setting_high_glucose),
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
            AlarmSwitchRow(stringResource(R.string.lost_signal), checkedForLostSignal.value) {
                checkedForLostSignal.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiLostSignal(it)
                }
            }
            AlarmSwitchRow(stringResource(R.string.sensor_expired), checkedForExpiredSensor.value) {
                checkedForExpiredSensor.value = it
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setNotiExpiredSensor(it)
                }
            }
            AlarmSwitchRow(stringResource(R.string.alarm_setting_sensor_stabilization), checkedForStabilization.value) {
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
                            text = stringResource(R.string.cancel),
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
                            text = stringResource(R.string.confirm),
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
        SubScreenHeader(stringResource(R.string.setting_menu_title_app_info), onBackClick = { navController.popBackStack() })

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
            Text(if(isKorea()){"버전 $versionName"} else {"Version $versionName"}, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.app_info_latest_version), fontSize = 12.sp, color = TextGray)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(stringResource(R.string.app_info_medical_device_regulatory_info), fontSize = 14.sp, modifier = Modifier.clickable {
                    Toast.makeText(context, context.getString(R.string.toast_medical_device_regulatory_info_excuse), Toast.LENGTH_SHORT).show()
                })
                Text(stringResource(R.string.app_info_terms_of_service), fontSize = 14.sp, modifier = Modifier.clickable {
                    Toast.makeText(context, context.getString(R.string.toast_terms_of_service_excuse), Toast.LENGTH_SHORT).show()
                })
                Text(stringResource(R.string.app_info_privacy_policy), fontSize = 14.sp, modifier = Modifier.clickable {
                    Toast.makeText(context, context.getString(R.string.toast_privacy_policy_excuse), Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}


// ==========================================
// 사용자 정보 수정
// ==========================================

// 성별과 당뇨 유형은 서버가 코드로 받고 문자열로 돌려준다. 회원가입에서 쓰는 값과
// 같아야 하므로 SignUpInfoScreen3 의 숫자를 그대로 가져왔다. 한쪽만 고치면 안 된다.
private const val SEX_MALE = 1601
private const val SEX_FEMALE = 1602
private const val SEX_NONE = 1603

private const val DIABETES_TYPE_1 = 1701
private const val DIABETES_TYPE_2 = 1702
private const val DIABETES_GESTATIONAL = 1703
private const val DIABETES_PRE = 1704
private const val DIABETES_LADA = 1705
private const val DIABETES_NORMAL = 1706
private const val DIABETES_UNKNOWN = 1707

private val SEX_CHOICES = listOf(
    SEX_MALE to R.string.item_gender_male,
    SEX_FEMALE to R.string.item_gender_female,
    SEX_NONE to R.string.item_gender_none
)

private val DIABETES_CHOICES = listOf(
    DIABETES_NORMAL to R.string.item_diabetes_type_normal,
    DIABETES_PRE to R.string.item_diabetes_type_prediabetes,
    DIABETES_TYPE_1 to R.string.item_diabetes_type_1,
    DIABETES_TYPE_2 to R.string.item_diabetes_type_2,
    DIABETES_GESTATIONAL to R.string.item_diabetes_type_gestational,
    DIABETES_LADA to R.string.item_diabetes_type_lada,
    DIABETES_UNKNOWN to R.string.item_diabetes_type_unknown
)

private fun sexLabelRes(code: Int): Int =
    SEX_CHOICES.firstOrNull { it.first == code }?.second ?: R.string.item_gender_none

private fun diabetesLabelRes(code: Int): Int =
    DIABETES_CHOICES.firstOrNull { it.first == code }?.second ?: R.string.item_diabetes_type_unknown

// 아래 네 함수가 다루는 한글은 화면에 쓰는 말이 아니라 서버가 주고받는 값이다.
// 그래서 strings.xml 로 빼지 않고 여기에 그대로 적는다. 번역하면 매칭이 깨진다.
private fun sexCodeOf(raw: String): Int = when (raw) {
    "남성" -> SEX_MALE
    "여성" -> SEX_FEMALE
    else -> SEX_NONE
}

private fun sexServerValue(code: Int): String = when (code) {
    SEX_MALE -> "남성"
    SEX_FEMALE -> "여성"
    else -> "선택 안함"
}

private fun diabetesCodeOf(raw: String): Int = when (raw) {
    "제1형 당뇨병" -> DIABETES_TYPE_1
    "제2형 당뇨병" -> DIABETES_TYPE_2
    "임신성 당뇨병" -> DIABETES_GESTATIONAL
    "당뇨 전단계" -> DIABETES_PRE
    "LADA" -> DIABETES_LADA
    "정상" -> DIABETES_NORMAL
    else -> DIABETES_UNKNOWN
}

private fun diabetesServerValue(code: Int): String = when (code) {
    DIABETES_TYPE_1 -> "제1형 당뇨병"
    DIABETES_TYPE_2 -> "제2형 당뇨병"
    DIABETES_GESTATIONAL -> "임신성 당뇨병"
    DIABETES_PRE -> "당뇨 전단계"
    DIABETES_LADA -> "LADA"
    DIABETES_NORMAL -> "정상"
    else -> "모름"
}

enum class UserInfoField { NAME, EMAIL, SEX, AGE, HEIGHT, WEIGHT, DIABETES, TARGET_RANGE }

/** 값 오른쪽에 연필을 두어 눌러서 고칠 수 있는 줄임을 알린다. */
@Composable
fun EditableInfoRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 16.sp, color = TextGray)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 어느 줄을 눌렀느냐에 따라 맞는 대화상자를 연다.
 *
 * 고른 값은 화면 상태에만 담아 둔다. 서버로는 "수정하기" 를 눌렀을 때 한 번에 보낸다.
 * 줄마다 따로 보내면 중간에 끊겼을 때 절반만 바뀐 상태가 남는다.
 */
@Composable
fun UserInfoEditDialogs(
    editing: MutableState<UserInfoField?>,
    name: MutableState<String>,
    email: MutableState<String>,
    age: MutableState<String>,
    height: MutableState<String>,
    weight: MutableState<String>,
    sexCode: MutableState<Int>,
    diabetesCode: MutableState<Int>,
    targetLow: MutableState<String>,
    targetHigh: MutableState<String>
) {
    val field = editing.value ?: return
    val close = { editing.value = null }

    when (field) {
        UserInfoField.NAME -> SingleValueEditDialog(
            title = stringResource(R.string.user_info_name),
            initial = name.value,
            numeric = false,
            onDismiss = close
        ) { name.value = it; close() }

        UserInfoField.EMAIL -> SingleValueEditDialog(
            title = stringResource(R.string.user_info_email),
            initial = email.value,
            numeric = false,
            onDismiss = close
        ) { email.value = it; close() }

        UserInfoField.AGE -> SingleValueEditDialog(
            title = stringResource(R.string.user_info_age),
            initial = age.value,
            numeric = true,
            onDismiss = close
        ) { age.value = it; close() }

        UserInfoField.HEIGHT -> SingleValueEditDialog(
            title = stringResource(R.string.user_info_height),
            initial = height.value,
            numeric = true,
            onDismiss = close
        ) { height.value = it; close() }

        UserInfoField.WEIGHT -> SingleValueEditDialog(
            title = stringResource(R.string.user_info_weight),
            initial = weight.value,
            numeric = true,
            onDismiss = close
        ) { weight.value = it; close() }

        UserInfoField.SEX -> ChoiceEditDialog(
            title = stringResource(R.string.user_info_gender),
            choices = SEX_CHOICES,
            selected = sexCode.value,
            onDismiss = close
        ) { sexCode.value = it; close() }

        UserInfoField.DIABETES -> ChoiceEditDialog(
            title = stringResource(R.string.user_info_diabetes_type),
            choices = DIABETES_CHOICES,
            selected = diabetesCode.value,
            onDismiss = close
        ) { diabetesCode.value = it; close() }

        UserInfoField.TARGET_RANGE -> TargetRangeEditDialog(
            initialLow = targetLow.value,
            initialHigh = targetHigh.value,
            onDismiss = close
        ) { low, high ->
            targetLow.value = low
            targetHigh.value = high
            close()
        }
    }
}

@Composable
private fun EditDialogFrame(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                content()
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFFD8D8D8))
                    ) {
                        Text(stringResource(R.string.cancel), color = TextGray, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.confirm), color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryOrange,
    unfocusedBorderColor = Color.LightGray,
    cursorColor = PrimaryOrange
)

@Composable
private fun SingleValueEditDialog(
    title: String,
    initial: String,
    numeric: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val text = remember(initial) { mutableStateOf(initial) }
    EditDialogFrame(title = title, onDismiss = onDismiss, onConfirm = { onConfirm(text.value) }) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text
            ),
            colors = editFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChoiceEditDialog(
    title: String,
    choices: List<Pair<Int, Int>>,
    selected: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val picked = remember(selected) { mutableStateOf(selected) }
    EditDialogFrame(title = title, onDismiss = onDismiss, onConfirm = { onConfirm(picked.value) }) {
        Column {
            choices.forEach { (code, labelRes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { picked.value = code }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = picked.value == code,
                        onClick = { picked.value = code },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryOrange)
                    )
                    Text(stringResource(labelRes), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun TargetRangeEditDialog(
    initialLow: String,
    initialHigh: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val low = remember(initialLow) { mutableStateOf(initialLow) }
    val high = remember(initialHigh) { mutableStateOf(initialHigh) }
    EditDialogFrame(
        title = stringResource(R.string.user_info_target_glucose_range),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(low.value, high.value) }
    ) {
        Column {
            Text(stringResource(R.string.user_info_edit_target_low), fontSize = 14.sp, color = TextGray)
            OutlinedTextField(
                value = low.value,
                onValueChange = { low.value = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = editFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.user_info_edit_target_high), fontSize = 14.sp, color = TextGray)
            OutlinedTextField(
                value = high.value,
                onValueChange = { high.value = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = editFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
