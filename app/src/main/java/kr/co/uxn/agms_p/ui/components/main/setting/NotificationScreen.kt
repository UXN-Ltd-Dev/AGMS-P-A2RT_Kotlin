package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEventData
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.components.main.event.ItemData
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val glucoseDataFromUser = remember { mutableStateOf("") }
    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // notification swtich
    val checkedForSound = remember { mutableStateOf(false) }
    val checkedForVibration = remember { mutableStateOf(false) }
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

                Box(
                    modifier = Modifier.clickable {

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

                Box(
                    modifier = Modifier.clickable {

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

                Divider(color = Color.Transparent, thickness = 20.dp)

                Divider()

                Box(
                    modifier = Modifier.clickable {

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

                Box(
                    modifier = Modifier.clickable {

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

                Box(
                    modifier = Modifier.clickable {

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

                Box(
                    modifier = Modifier.clickable {

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

                Box(
                    modifier = Modifier.clickable {

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

                Box(
                    modifier = Modifier.clickable {

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
                }

                Divider()

            }
        }
    }
}

