package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.components.main.TimePickerDialog
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // notification swtich
    val checkedForSound = remember { mutableStateOf(false) }
    val checkedForVibration = remember { mutableStateOf(false) }
    val checkedDoNotDisturbMode = remember { mutableStateOf(false) }
    val checkedForHighGlucose = remember { mutableStateOf(false) }
    val checkedForLowGlucose = remember { mutableStateOf(false) }
    val checkedForLostSignal = remember { mutableStateOf(false) }
    val checkedForExpiredSensor = remember { mutableStateOf(false) }
    val checkedForStabilization = remember { mutableStateOf(false) }
    val checkedForCalibration = remember { mutableStateOf(false) }

    val showGlucoseDialog = remember { mutableStateOf(false) }
    val targetLowGlucose = remember { mutableStateOf("") }
    val targetHighGlucose = remember { mutableStateOf("") }

    val showSetDailyCalibrationDialog = remember { mutableStateOf(false) }

    var dailyCalibrationTime = remember { mutableStateOf("") }
    val state = rememberTimePickerState(
        is24Hour = true,
        initialHour = 0,
        initialMinute = 0
    )
    val formatter = remember { SimpleDateFormat("a hh:mm", Locale.KOREAN) }



    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // DS로부터 값 불러오기
            val verifiedDSHigh = DataStoreManager.getNotiHighGlucose().first() ?: false
            val verifiedDSLow = DataStoreManager.getNotiLowGlucose().first() ?: false
            val verifiedDSLostSignal = DataStoreManager.getNotiLostSignal().first() ?: false
            val verifiedDSExpiredSensor = DataStoreManager.getNotiExpiredSensor().first() ?: true
            val verifiedDSStabilization = DataStoreManager.getNotiStabilization().first() ?: true
            val verifiedDSCalibration = DataStoreManager.getNotiCalibration().first() ?: true
            val verifiedDSTargetLowGlucose = DataStoreManager.getTargetLowGlucose().first() ?: -1
            val verifiedDSTargetHighGlucose = DataStoreManager.getTargetHighGlucose().first() ?: -1
            val verifiedDSDailyCalibrationTime = DataStoreManager.getDailyCalibrationTime().first() ?: "오전 11:00"

            // 화면에 값 설정
            checkedForHighGlucose.value = verifiedDSHigh
            checkedForLowGlucose.value = verifiedDSLow
            checkedForLostSignal.value = verifiedDSLostSignal
            checkedForExpiredSensor.value = verifiedDSExpiredSensor
            checkedForStabilization.value = verifiedDSStabilization
            checkedForCalibration.value = verifiedDSCalibration
            targetLowGlucose.value = verifiedDSTargetLowGlucose.toString()
            targetHighGlucose.value = verifiedDSTargetHighGlucose.toString()
            dailyCalibrationTime.value = verifiedDSDailyCalibrationTime

            // 로그 띄우기
            Log.e("NOTI", "After High : ${verifiedDSHigh}, Low : ${verifiedDSLow} " +
                    "\n Lost : ${verifiedDSLostSignal} ExpiredSensor : ${verifiedDSExpiredSensor}" +
                    "\n Stabilization : ${verifiedDSStabilization} Calibration : ${verifiedDSCalibration}" +
                    "\n Target High Glucose : ${verifiedDSTargetHighGlucose} Target Low Glucose : ${verifiedDSTargetLowGlucose}")
        }
    }

    if (showSetDailyCalibrationDialog.value) {

        TimePickerDialog(
            title = "혈당값 입력 시간",
            onCancel = { showSetDailyCalibrationDialog.value = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, state.hour)
                cal.set(Calendar.MINUTE, state.minute)
                cal.isLenient = false
                dailyCalibrationTime.value = formatter.format(cal.time)
                showSetDailyCalibrationDialog.value = false

                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.setDailyCalibrationTime(dailyCalibrationTime.value)
                }
                Log.d("TIME", "finalTime : $dailyCalibrationTime")
            }
        ) {
            TimeInput(state = state)

        }

    }

    if (showGlucoseDialog.value) {
        Dialog(onDismissRequest = { showGlucoseDialog.value = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "저혈당",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    OutlinedTextField(
                        value = targetLowGlucose.value,
                        onValueChange = { targetLowGlucose.value = it},
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(15 .dp))

                    Text(
                        text = "고혈당",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    OutlinedTextField(
                        value = targetHighGlucose.value,
                        onValueChange = { targetHighGlucose.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (targetHighGlucose.value.contains(".") || targetHighGlucose.value.contains("-") || targetHighGlucose.value.contains(",")
                                    || targetLowGlucose.value.contains(".") || targetLowGlucose.value.contains("-") || targetLowGlucose.value.contains(",")
                                ) {
                                    Toast.makeText(context, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show()
                                } else if (targetHighGlucose.value != "" && targetLowGlucose.value != "") {
                                    showGlucoseDialog.value = false
                                    coroutineScope.launch(Dispatchers.Main) {
                                        DataStoreManager.setTargetHighGlucose(targetHighGlucose.value.toInt())
                                        DataStoreManager.setTargetLowGlucose(targetLowGlucose.value.toInt())
                                        Log.e("TEST", "저장된 고혈당 : ${DataStoreManager.getTargetHighGlucose().first()}")
                                        Log.e("TEST", "저장된 저혈당 : ${DataStoreManager.getTargetLowGlucose().first()}")
                                    }
                                } else {
                                    Toast.makeText(context, "혈당을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                }},
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3451B2), // 파란색
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "입력",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}")
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "알림 설정",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

//                Divider()
//
//                Box() {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.White)
//                            .height(50.dp)
//                            .padding(horizontal = 30.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "소리",
//                            fontSize = 16.sp
//                        )
//
//                        Switch(
//                            checked = checkedForSound.value,
//                            onCheckedChange = {
//                                checkedForSound.value = it
//                            }
//                        )
//                    }
//                }
//
//                Divider()
//
//                Box() {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.White)
//                            .height(50.dp)
//                            .padding(horizontal = 30.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "진동",
//                            fontSize = 16.sp
//                        )
//
//                        Switch(
//                            checked = checkedForVibration.value,
//                            onCheckedChange = {
//                                checkedForVibration.value = it
//                            }
//                        )
//                    }
//                }
//
//                Divider()
//
//                Box() {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.White)
//                            .height(50.dp)
//                            .padding(horizontal = 30.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "방해금지 중 알림 허용",
//                            fontSize = 16.sp
//                        )
//
//                        Switch(
//                            checked = checkedDoNotDisturbMode.value,
//                            onCheckedChange = {
//                                checkedDoNotDisturbMode.value = it
//                            }
//                        )
//                    }
//                }

//                Divider()
//
//                Divider(color = Color.Transparent, thickness = 20.dp)

                Divider()

                Box(
                    modifier = Modifier.clickable {
                    showGlucoseDialog.value = true
                }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(50.dp)
                            .padding(horizontal = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "알림 혈당 범위",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1.5f)
                        )

                        Text(
                            text = "${targetLowGlucose.value}" +  "~" + "${targetHighGlucose.value}" + "mg/dL",
                            fontSize = 15.sp,
                            color = Color(0xFF828282),
                            modifier = Modifier.weight(1f)
                        )


                        Surface(
                            modifier = Modifier
                                .padding(top = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_icon),
                                modifier = Modifier
                                    .size(16.dp),
                                contentDescription = "혈당 입력 시간 수정"
                            )
                        }
                    }
                }

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
                            text = "고혈당 알림",
                            fontSize = 16.sp
                        )

                        Switch(
                            checked = checkedForHighGlucose.value,
                            onCheckedChange = {
                                checkedForHighGlucose.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiHighGlucose(it)
                                }
                            }
                        )
                    }
                }

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
                            text = "저혈당 알림",
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForLowGlucose.value,
                            onCheckedChange = {
                                checkedForLowGlucose.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiLowGlucose(it)
                                }
                            }
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
                            text = "신호 소실",
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForLostSignal.value,
                            onCheckedChange = {
                                checkedForLostSignal.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiLostSignal(it)
                                }
                            }
                        )
                    }
                }

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
                            text = "센서 만료",
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForExpiredSensor.value,
                            onCheckedChange = {
                                checkedForExpiredSensor.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiExpiredSensor(it)
                                }
                            }
                        )
                    }
                }

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
                            text = "센서 안정화",
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForStabilization.value,
                            onCheckedChange = {
                                checkedForStabilization.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiStabilization(it)
                                }
                            }
                        )
                    }
                }

                Divider()

                Divider(color = Color.Transparent, thickness = 20.dp)

                Divider()

                Box(
                    modifier = Modifier.clickable {
                        showSetDailyCalibrationDialog.value = true
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(80.dp),
                        verticalArrangement = Arrangement.Center
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
                                text = "혈당값 입력 시간 알림",
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = checkedForCalibration.value,
                                onCheckedChange = {
                                    checkedForCalibration.value = it
                                    coroutineScope.launch(Dispatchers.IO) {
                                        DataStoreManager.setNotiCalibration(it)
                                    }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .height(30.dp)
                                .padding(horizontal = 30.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = dailyCalibrationTime.value,
                                color = Color(0xFF828282),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Surface(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit_icon),
                                    modifier = Modifier
                                        .size(16.dp),
                                    contentDescription = "혈당 입력 시간 수정"
                                )
                            }
                        }
                    }
                }

                Divider()

            }
        }
    }
}
