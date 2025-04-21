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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import java.time.LocalDateTime
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
                            hint.value = "혈당을 입력해주세요." // 포커스를 잃고 입력값이 비어있다면 힌트를 다시 보여줍니다.
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
                interactionSource = interactionSource, // 터치 이벤트 감지
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
                text = "자가측정혈당수치를\n입력해주세요.",
                fontSize = 23.sp,
                color = Color(0xFF385DAB)
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "초기 설정을 위해\n공복 혈당이 필요합니다.",
                fontSize = 15.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(50.dp))

            // 완료 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_complete),
                    contentDescription = "완료 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            if(glucoseDataFromUser.value.contains(".") || glucoseDataFromUser.value.contains("-") || glucoseDataFromUser.value.contains(",")) {
                                Toast.makeText(context, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show()
                            } else if (glucoseDataFromUser.value != "") {
                                // TODO : 서버에 혈당데이터 전송, 화면이동
                                coroutineScope.launch(Dispatchers.IO) {
                                    val userId = DataStoreManager.getUserId().first()
                                    Log.e("TEST", "userId : $userId")
                                    try {
                                        val createdAt = LocalDateTime.now()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                        Log.e("TEST", "createdAt : $createdAt")
                                        val upload = tokenRetrofit.uploadEvent(
                                            RequestEventData(
                                                userId = userId!!,
                                                createdAt = createdAt,
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
                                                "네트워크를 확인해주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e("TEST", "업로드 실패")
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "혈당을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                )
            }
        }
    }
}