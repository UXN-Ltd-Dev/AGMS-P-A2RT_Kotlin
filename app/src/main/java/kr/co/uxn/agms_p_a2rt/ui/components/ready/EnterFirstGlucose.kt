package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestEventData
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.UserCalibration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun EnterFirstGlucose(navController: NavController) {
    val context = LocalContext.current
    val glucoseDataFromUser = remember { mutableStateOf("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    // 터치 시, 힌트를 지우기 위한 용도
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            },
        color = colorResource(R.color.background_white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(120.dp))

            Image(
                modifier = Modifier.size(200.dp, 240.dp),
                painter = painterResource(R.drawable.enter_glucose_illustration),
                contentDescription = "혈당 입력 일러스트"
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = glucoseDataFromUser.value,
                onValueChange = { glucoseDataFromUser.value = it },
                modifier = Modifier
                    .size(250.dp, 60.dp)
                    .onFocusChanged { focusState ->  // focusObserver 사용
                        if (focusState.isFocused) {
                            hint.value = "" // 포커스가 들어가면 힌트를 비웁니다
                        } else if (glucoseDataFromUser.value.isEmpty()) {
                            hint.value = context.getString(R.string.enter_glucose_hint) // 포커스를 잃고 입력값이 비어있다면 힌트를 다시 보여줍니다.
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
                        fontSize = 18.sp
                    )
                },
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.first_glucose_title),
                fontSize = 23.sp,
                color = colorResource(R.color.main)
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.first_glucose_sub_title),
                fontSize = 15.sp,
                color = Color.Gray
            )

//            Spacer(modifier = Modifier.height(50.dp))
            Spacer(modifier = Modifier.weight(1f))


            // 완료 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
//                    painter = painterResource(id = if (isKorean) R.drawable.btn_complete else R.drawable.btn_eng_complete),
//                    painter = painterResource(id = R.drawable.btn_complete),
                    painter = painterResource(id = R.drawable.btn_complete_rt),
                    contentDescription = "완료 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            if (glucoseDataFromUser.value.contains(".") || glucoseDataFromUser.value.contains("-") || glucoseDataFromUser.value.contains(",")) {
                                coroutineScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, context.getString(R.string.toast_only_number), Toast.LENGTH_SHORT).show()
                                }
                            } else if (glucoseDataFromUser.value != "") {
                                if (glucoseDataFromUser.value.toInt() > 350 ) {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        Toast.makeText(context, context.getString(R.string.toast_invalid_glucose_too_high), Toast.LENGTH_SHORT).show()
                                    }
                                    return@clickable
                                }

                                if (glucoseDataFromUser.value.toInt() < 50) {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        Toast.makeText(context, context.getString(R.string.toast_invalid_glucose_too_low), Toast.LENGTH_SHORT).show()
                                    }
                                    return@clickable
                                }

                                coroutineScope.launch(Dispatchers.IO) {
                                    val userId = DataStoreManager.getUserId().first() ?: -1
                                    Log.e("TEST", "userId : $userId")
                                    try {
                                        val now = System.currentTimeMillis()
                                        val instant = Instant.ofEpochMilli(now)
                                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        val createdAt = instant
                                            .atZone(ZoneId.of("Asia/Seoul"))
                                            .format(formatter)
                                        val createdAtUtc = instant
                                            .atZone(ZoneOffset.UTC)
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                                        Log.e(
                                            "TEST",
                                            "createdAt(KST) : $createdAt, createdAt(UTC) : $createdAtUtc, createdAtLong : $now"
                                        )

                                        // db에 저장
                                        localDbRepository?.dataDao()?.insertCalibration(
                                            UserCalibration(
                                                userId = userId,
                                                glucoseValue = glucoseDataFromUser.value.toDouble(),
                                                createdAt = createdAt,
                                                createdAtLong = now
                                            )
                                        )

                                        val upload = tokenRetrofit.uploadEvent(
                                            RequestEventData(
                                                userId = userId!!,
                                                createdAt = createdAtUtc,
                                                eventTypeCode = 1403,
                                                content = glucoseDataFromUser.value
                                            )
                                        )

                                        Log.e("TEST", "uploadbody : ${upload.body().toString()}")
                                        if (upload.isSuccessful) {
                                            val uploadBody = upload.body()
                                            if (uploadBody != null) {
                                                if (uploadBody.isSuccess) {
                                                    withContext(Dispatchers.Main) {
                                                        Log.e("TEST", "${uploadBody.toString()}")
//                                                        navController.navigate("MainScreen/${0}") 기존
                                                        navController.navigate("MainScreen/${0}") {
                                                            popUpTo("StabilizationCompleteScreen") {
                                                                inclusive = true
                                                            }
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.e("TEST", "API 에러 : ${upload.errorBody()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("TEST", "네트워크 에러 : $e")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.toast_network_error),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e("TEST", "업로드 실패")
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_req_glucose), Toast.LENGTH_SHORT).show()
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch(Dispatchers.Main) {
                            navController.navigate("MainScreen/${0}") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                textAlign = TextAlign.Center,
                text = stringResource(R.string.first_glucose_skip),
                fontSize = 17.sp,
                color = Color.Gray,
                textDecoration = TextDecoration.Underline

            )
        }
    }
}
