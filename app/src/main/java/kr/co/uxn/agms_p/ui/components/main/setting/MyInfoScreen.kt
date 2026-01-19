package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestUpdateUser
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 계정 정보
    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val sex = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val diabetesType = remember { mutableStateOf("") }

    // 목표 혈당범위
    val targetMinRange = remember { mutableStateOf("0") }
    val targetMaxRange = remember { mutableStateOf("0") }

    var expandedForSex by remember { mutableStateOf(false) }
    var expandedForDiabetesType by remember { mutableStateOf(false) }

    // 다이얼로그 변수 모음
    var showCaliDialog = bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog = bleViewModel.showBleConnectDialog.collectAsState()
    var showBluetoothOnDialog = bleViewModel.showBluetoothOnDialog.collectAsState()
    var showLowGlucoseDialog = bleViewModel.showLowGlucoseDialog.collectAsState()
    var showHighGlucoseDialog = bleViewModel.showHighGlucoseDialog.collectAsState()
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"


    LaunchedEffect(Unit) {
        try {
            val userId = DataStoreManager.getUserId().first() ?: -1
            Log.e("TEST", "내정보 에서 불러온 userId : ${userId}")
            val getUserData = tokenRetrofit.getUser(userId)
            if (getUserData.isSuccessful) {
                val userData = getUserData.body()
                Log.d("TEST", "userDataBody : ${userData}")
                if (userData != null) {
                    if (userData.isSuccess) {
                        email.value = userData.email
                        name.value = userData.name
                        age.value = userData.age.toString()
                        height.value = userData.height.toString()
                        weight.value = userData.weight.toString()
                        targetMaxRange.value = userData.targetGlucoseMax.toString()
                        targetMinRange.value = userData.targetGlucoseMin.toString()
                        if (isKorean) {
                            sex.value = userData.sex
                            diabetesType.value = userData.diabetesType
                        } else {
                            if(userData.sex == "남성") {
                                sex.value = "Male"
                            } else if(userData.sex == "여성") {
                                sex.value = "Female"
                            } else {
                                sex.value = "Prefer not to say"
                            }

                            if(userData.diabetesType == "정상") {
                                diabetesType.value = "Normal"
                            } else if(userData.diabetesType == "당뇨 전단계") {
                                diabetesType.value = "Prediabetes"
                            } else if(userData.diabetesType == "제1형 당뇨병") {
                                diabetesType.value = "Type 1 Diabetes"
                            } else if(userData.diabetesType == "제2형 당뇨병") {
                                diabetesType.value = "Type 2 Diabetes"
                            } else if(userData.diabetesType == "임신성 당뇨병") {
                                diabetesType.value = "Gestational Diabetes"
                            } else if(userData.diabetesType == "LADA") {
                                diabetesType.value = "LADA"
                            } else { // "모름"
                                diabetesType.value = "Unknown"
                            }

                        }
                    }
                }
            } else {
                Log.e("TEST", "API 에러 : ${getUserData.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TEST", "네트워크 에러 : ${e.message}")
        }
    }

    // 다이얼로그
    // 1. Dialog : 일일 혈당 입력
    if (showCaliDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showCaliDialog(false) },
            onConfirm = {
                BleBridge.showCaliDialog(false)
                navController.navigate("GlucoseRegisterScreen")
            },
            title = stringResource(R.string.dialog_daily_enter_glucose_title),
            content = stringResource(R.string.dialog_daily_enter_glucose_content)
        )
    }

    // 2. Dialog : BLE 끊김
    if (showBleConnectDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
            },
            title = stringResource(R.string.dialog_ble_disconnected_title),
            content = stringResource(R.string.dialog_ble_disconnected_content)
        )
    }

    // 4. Dialog : 블루투스 ON
    if (showBluetoothOnDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBluetoothOnDialog(false) },
            onConfirm = {
                BleBridge.showBluetoothOnDialog(false)
            },
            title = stringResource(R.string.dialog_bluetooth_off_title),
            content = stringResource(R.string.dialog_bluetooth_off_content),
        )
    }

    // 5. Dialog : 측정 종료
    if (showEndMeasurementDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
                coroutineScope.launch(Dispatchers.IO) {
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
                                    Log.d("TEST", "${DataStoreManager.getIsMain().first()}")
                                    DataStoreManager.deleteAccessToken()
                                    DataStoreManager.deleteRefreshToken()
                                    DataStoreManager.deleteUserId()
                                    DataStoreManager.deleteDeviceMac()
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
                            Toast.makeText(context, context.getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            title = stringResource(R.string.dialog_end_measurement_title),
            content = stringResource(R.string.dialog_end_measurement_content),
        )
    }

    // 6.1 Dialog : 저혈당
    if (showLowGlucoseDialog.value) {
        NotiDialog(
            onDismiss = { showLowGlucoseDialog(false) },
            onConfirm = {
                showLowGlucoseDialog(false)
                NotificationManagerCompat.from(context).cancel(95)
            },
            title = stringResource(R.string.dialog_low_glucose_title),
            content = stringResource(R.string.dialog_low_glucose_content),
        )
    }

    // 6.2 Dialog : 고혈당
    if (showHighGlucoseDialog.value) {
        NotiDialog(
            onDismiss = { showHighGlucoseDialog(false) },
            onConfirm = {
                showHighGlucoseDialog(false)
                NotificationManagerCompat.from(context).cancel(96)
            },
            title = stringResource(R.string.dialog_high_glucose_title),
            content = stringResource(R.string.dialog_high_glucose_content)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}") {
                                    popUpTo("MainScreen/{startIndex}") { inclusive = true }
                                }
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings_profile),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9)),
            ) {
                Divider()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 내 정보를 입력하세요.
                    Text(
                        text = stringResource(R.string.settings_login_email),
                        fontSize = 18.sp,
                        color = Color(0xFF828282),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = email.value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

//                    Spacer(modifier = Modifier.height(70.dp))
                    Spacer(modifier = Modifier.height(40.dp))

                    // 1. 이름
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_name),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.size(60.dp, 27.dp))

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray, // 원하는 색상
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = name.value,
                                onValueChange = { name.value = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(27.dp)
                                            .drawBehind {
                                                val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 성별
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_gender),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.size(60.dp, 27.dp))
                        var expanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    expanded = !expanded
                                }
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray, // 원하는 색상
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sex.value,
                                fontSize = 20.sp,
                                modifier = Modifier.clickable {
                                    expanded = !expanded
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = DpOffset(100.dp, -200.dp)
                        ) {
                            // First section
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_gender_male),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    sex.value = context.getString(R.string.item_gender_male)
                                    expanded = !expanded
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_gender_female),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    sex.value = context.getString(R.string.item_gender_female)
                                    expanded = !expanded
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_gender_none),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    sex.value = context.getString(R.string.item_gender_none)
                                    expanded = !expanded
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 연령
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_age),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.size(60.dp, 27.dp))

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray, // 원하는 색상
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = age.value,
                                onValueChange = { age.value = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(27.dp)
                                            .drawBehind {
                                                val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 신장
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_height),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.size(60.dp, 27.dp))
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = height.value,
                                onValueChange = { height.value = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(27.dp)
                                            .drawBehind {
                                                val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. 체중
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_weight),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.size(60.dp, 27.dp))

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = weight.value,
                                onValueChange = { weight.value = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(27.dp)
                                            .drawBehind {
                                                val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. 당뇨 정보
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_diabetes_type),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        // 제1형 당뇨병
                        // 제2형 당뇨병
                        // 임신성 당뇨병
                        // 당뇨 전단계
                        // LADA(Latent Autoimmune Diabetes in Adults)
                        // 정상
                        Spacer(modifier = Modifier.width(22.dp))

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray, // 원하는 색상
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diabetesType.value,
                                fontSize = 20.sp,
                                modifier = Modifier.clickable {
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = expandedForDiabetesType,
                            onDismissRequest = { expandedForDiabetesType = false },
                            offset = DpOffset(100.dp, -10.dp)
                        ) {
                            // First section
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_normal),
                                        fontSize = 20.sp
                                    )
                               },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_normal)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_prediabetes),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_prediabetes)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_1),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_1)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_2),
                                        fontSize = 20.sp
                                    )
                               },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_2)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_gestational),
                                        fontSize = 20.sp
                                    )
                               },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_gestational)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_lada),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_lada)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.item_diabetes_type_unknown),
                                        fontSize = 20.sp
                                    )
                                },
                                onClick = {
                                    diabetesType.value = context.getString(R.string.item_diabetes_type_unknown)
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                        }
                    }

//                    Spacer(modifier = Modifier.height(120.dp))
                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 65.dp)
                            .padding(start = 50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_target_glucose_range),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(100.dp))
                    }




                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = targetMinRange.value,
                                onValueChange = { targetMinRange.value = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(27.dp)
                                            .drawBehind {
                                                val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }

                        Text(
                            text = " ~ "
                        )

                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()  // 선 굵기
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = targetMaxRange.value,
                                onValueChange = { targetMaxRange.value = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(27.dp)
                                            .drawBehind {
                                                val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }


                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 15.dp)
                            .padding(horizontal = 30.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.target_glucose_description),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Image(
                            painter = painterResource(id = if(isKorean) R.drawable.btn_save else R.drawable.btn_eng_save),
                            contentDescription = "저장 버튼",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val userId = DataStoreManager.getUserId().first() ?: -1
                                            Log.e("TEST", "저장버튼 에서 불러온 userId : ${userId}")

                                            val sex = when (sex.value) {
                                                context.getString(R.string.item_gender_male) -> 1601
                                                context.getString(R.string.item_gender_female) -> 1602
                                                else -> 1603
                                            }

                                            Log.e("TEST", "코드로 변환된 sex : ${sex}")

                                            val diabetesType = when (diabetesType.value) {
                                                context.getString(R.string.item_diabetes_type_1) -> 1701
                                                context.getString(R.string.item_diabetes_type_2) -> 1702
                                                context.getString(R.string.item_diabetes_type_gestational) -> 1703
                                                context.getString(R.string.item_diabetes_type_prediabetes) -> 1704
                                                context.getString(R.string.item_diabetes_type_lada) -> 1705
                                                context.getString(R.string.item_diabetes_type_normal) -> 1706
                                                else -> 1707
                                            }

                                            Log.e("TEST", "코드로 변환된 sex : ${sex}")

                                            val requestUpdateUser = RequestUpdateUser(
                                                userId = userId,
                                                name = name.value,
                                                sex = sex,
                                                age = age.value.toInt(),
                                                height = height.value.toInt(),
                                                weight = weight.value.toInt(),
                                                diabetesType = diabetesType,
                                                targetGlucoseMin = targetMinRange.value.toInt(),
                                                targetGlucoseMax = targetMaxRange.value.toInt()
                                            )

                                            val getUserData =
                                                tokenRetrofit.updateUser(requestUpdateUser)
                                            if (getUserData.isSuccessful) {
                                                val userData = getUserData.body()
                                                Log.e("TEST", "userDataBody : ${userData}")
                                                if (userData != null) {
                                                    if (userData.isSuccess) {
                                                        Log.e("TEST", "DB 저장 성공")
                                                        withContext(Dispatchers.Main) {
                                                            navController.navigate("MainScreen/${2}")
                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            navController.navigate("MainScreen/${2}")
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("TEST", "API 에러 발생 : ${getUserData.errorBody()?.string()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("TEST", "네트워크 에러 발생 : ${e.message}")

                                            if(!e.message.isNullOrEmpty()) {
                                                if(e.message!!.contains("For input string")) {
                                                 Log.d("TEST", "입력 문구 확인")
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        Toast.makeText(context, context.getString(R.string.toast_empty_value_alert), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

