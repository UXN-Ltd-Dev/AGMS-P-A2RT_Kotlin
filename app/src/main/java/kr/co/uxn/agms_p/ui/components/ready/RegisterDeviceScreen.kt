package kr.co.uxn.agms_p.ui.components.ready

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestRefreshToken
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleUtils
import kr.co.uxn.agms_p.ble.BleUtils.STATUS_BLE_ENABLED
import kr.co.uxn.agms_p.ble.BleUtils.getBleStatus

@Composable
fun RegisterDeviceScreen(navController: NavController) {
    val context = LocalContext.current
    val deviceNumber = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // 터치 시, 힌트를 지우기 위한 용도
    val interactionSource = remember { MutableInteractionSource() }
    val hint = remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val fontSize = when {
        screenHeightDp == 777 -> 15.sp // s21
        else -> 16.sp
    }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    LaunchedEffect(Unit) {
        Log.d("TEST", "screenHeightDP : $screenHeightDp")
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(start = 30.dp, top = 30.dp),
            ) {
                // 백 버튼
                Image(
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "백 버튼",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )
            }

            Spacer(modifier = Modifier.size(50.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.register_device_title),
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.size(15.dp))

            Image(
                modifier = Modifier.size(300.dp, 250.dp),
                painter = painterResource(R.drawable.serial_number_pic),
                contentDescription = "시리얼넘버 예시 사진"
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = stringResource(R.string.register_device_sub_title),
                color = Color(0xFF385DAB),
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.size(20.dp))

            OutlinedTextField(
                value = deviceNumber.value,
                onValueChange = { deviceNumber.value = it },
                modifier = Modifier
                    .size(230.dp, 55.dp)
                    .onFocusChanged { focusState ->  // focusObserver 사용
                        if (focusState.isFocused) {
                            hint.value = "" // 포커스가 들어가면 힌트를 비웁니다
                        } else if (deviceNumber.value.isEmpty()) {
                            hint.value = context.getString(R.string.enter_serial_number_hint) // 포커스를 잃고 입력값이 비어있다면 힌트를 다시 보여줍니다.
                        }
                    },

                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                singleLine = true,
                placeholder = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = hint.value,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = fontSize
                    )
                },
                interactionSource = interactionSource, // 터치 이벤트 감지
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
            )

//            Spacer(modifier = Modifier.height(54.dp))
            Spacer(modifier = Modifier.weight(1f))

            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(bottom = 5.dp)
            ) {
                Image(
//                    painter = painterResource(id = if(isKorean) R.drawable.btn_next else R.drawable.btn_eng_continue),
                    painter = painterResource(id = R.drawable.btn_next),
                    contentDescription = "다음 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            if (deviceNumber.value == "999999") {
                                navController.navigate("ScanDeviceScreen/${deviceNumber.value}/${deviceNumber.value}")
                            } else if (deviceNumber.value != "") {

                                if (getBleStatus(context) != STATUS_BLE_ENABLED) {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        Toast.makeText(context, context.getString(R.string.toast_turn_on_bluetooth), Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                } else {
                                    try {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val result =
                                                tokenRetrofit.getDeviceMac(deviceNumber.value)
                                            if (result.isSuccessful) {
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                    if (resultBody.isExists) {
                                                        val mac = resultBody.deviceMac
                                                        withContext(Dispatchers.Main) {
                                                            navController.navigate("ScanDeviceScreen/$mac/${deviceNumber.value}")
//                                                        Toast.makeText(context, "조회된 mac은\n$mac 입니다.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("TAG", "API 에러 : ${result.errorBody()}")
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.toast_check_serial_number),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        }
                                    } catch (e: Exception) {
                                        Log.e("TAG", "네트워크 에러 : $e")
                                    }
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_empty_serial_number), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                )
            }
        }
    }
}
