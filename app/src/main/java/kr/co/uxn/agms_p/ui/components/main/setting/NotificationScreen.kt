package kr.co.uxn.agms_p.ui.components.main.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.SnapPosition
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
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val targetGlucoseRange = remember { mutableStateOf("80~150mg/dL") }
    val dailyCalibrationTime = remember { mutableStateOf("오전 11시") }
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
                            text = "소리",
                            fontSize = 16.sp
                        )

                        Switch(
                            checked = checkedForSound.value,
                            onCheckedChange = {
                                checkedForSound.value = it
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
                            text = "진동",
                            fontSize = 16.sp
                        )

                        Switch(
                            checked = checkedForVibration.value,
                            onCheckedChange = {
                                checkedForVibration.value = it
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
                            text = "방해금지 중 알림 허용",
                            fontSize = 16.sp
                        )

                        Switch(
                            checked = checkedDoNotDisturbMode.value,
                            onCheckedChange = {
                                checkedDoNotDisturbMode.value = it
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
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "목표 혈당 범위",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1.5f)
                        )

                        Text(
                            text = targetGlucoseRange.value,
                            fontSize = 15.sp,
                            color = Color(0xFF828282),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.edit_icon),
                            modifier = Modifier.size(14.dp),
                            contentDescription = "목표 혈당 범위 수정 아이콘"
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
                            text = "고혈당 알림",
                            fontSize = 16.sp
                        )

                        Switch(
                            checked = checkedForHighGlucose.value,
                            onCheckedChange = {
                                checkedForHighGlucose.value = it
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
                            }
                        )
                    }
                }

                Divider()

                Divider(color = Color.Transparent, thickness = 20.dp)

                Divider()

                Box() {
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
                            Icon(
                                painter = painterResource(id = R.drawable.edit_icon),
                                modifier = Modifier.size(14.dp)
                                    .align(Alignment.CenterVertically),
                                contentDescription = "혈당 입력 시간 수정"
                            )
                        }
                    }
                }

                Divider()

            }
        }
    }
}

