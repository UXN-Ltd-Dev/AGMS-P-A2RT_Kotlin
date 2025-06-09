package kr.co.uxn.agms_p.ui.components.login

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEmailCode
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEmailVerificationCode
import kr.co.uxn.agms_p.api.model.requestDTO.RequestUserInfo
import kr.co.uxn.agms_p.api.token.DataStoreManager

@Composable
fun PassWordResetScreen(navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val verificationCode = remember { mutableStateOf("") }
    val pwd1 = remember { mutableStateOf("") }
    val pwd2 = remember { mutableStateOf("") }
    val isEmailVerified = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // 인증번호 관련 변수
    val timerSeconds = remember { mutableStateOf(0) }
    val isTimerRunning = remember { mutableStateOf(false) }
    val timerKey = remember { mutableStateOf(0) } // 트리거 역할
    val emailCodeId = remember { mutableStateOf(0) }
    val isShowResetPwd = remember { mutableStateOf(false) }

    val fontSize = 18.sp

    LaunchedEffect(timerKey.value) {
        if (isTimerRunning.value) {
            timerSeconds.value = 300 // 5분 설정
//            timerSeconds.value = 60 // 1분 설정
            while (timerSeconds.value > 0) {
                delay(1000)
                timerSeconds.value -= 1
            }
            isTimerRunning.value = false
        }
    }

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
                .padding(horizontal = 30.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(40.dp))

            // 백 버튼
            Image(
                painter = painterResource(R.drawable.back_icon),
                contentDescription = "백 버튼",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Start)
                    .clickable {
                        navController.popBackStack()
                    }
            )
            Spacer(modifier = Modifier.size(50.dp))

            AnimatedVisibility(
                visible = isShowResetPwd.value == false,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isShowResetPwd.value) {
                        // 이메일
                        Text(
                            text = "이메일",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 5.dp)
                        )

                        // 이메일 입력란
                        BasicTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                            }),
                            textStyle = TextStyle(
                                fontSize = fontSize,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .drawBehind {
                                            val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                            val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                            drawLine(
                                                color = Color(0xFFEEEEEF),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                        .padding(start = 5.dp, end = 40.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (email.value.isEmpty()) {
                                        Text(
                                            text = "이메일 주소를 입력해 주세요.",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )


                        Spacer(Modifier.size(10.dp))

                        // 인증 번호 전송 버튼
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            onClick = {
                                if (email.value != "" && email.value.contains("@")) {
//                                    // 이메일 인증하기 타이머 초기화 코드
//                                    timerKey.value++
                                    isTimerRunning.value = false

                                    // 인증번호 입력란 초기화
                                    verificationCode.value = ""

                                    // 서버에 이메일 인증하기 요청
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            // 이메일 공백 처리
                                            val trimEmail = email.value.trim()

                                            val result = emptyRetrofit.requestVerficationCode(trimEmail)
                                            if (result.isSuccessful) {
                                                Log.d("TAG", "인증하기 서버 응답: ${result.body()}")
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                    if (resultBody.isDuplicated) {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(
                                                                context,
                                                                "이미 가입된 이메일입니다.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    } else { // isDuplicated = false
                                                        // 인증번호 전송
                                                        withContext(Dispatchers.Main) {

                                                            // 이메일 인증하기 타이머 초기화 코드
                                                            timerKey.value++
                                                            isTimerRunning.value = true

                                                            Toast.makeText(
                                                                context,
                                                                "인증번호가 전송되었습니다.\n메일을 확인해주세요.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            // email_code_id 저장
                                                            emailCodeId.value = resultBody.emailCodeId
                                                        }
                                                        Log.d("TAG", "emailCodeId : ${emailCodeId.value}")
                                                    }
                                                } else {
                                                    Log.d("TAG", "서버 응답이 null 입니다.")
                                                }
                                            } else {
                                                Log.d("TAG", "API 실패: ${result.errorBody()?.string()}")
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "메일 주소를 올바르게 입력해주세요.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.d("TAG", "네트워크 오류 발생: ${e.message}")
                                        }
                                    }

                                } else {
                                    Toast.makeText(context, "올바른 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF385DAB) // 배경색 설정
                            ),
                            modifier = Modifier
                                .align(Alignment.End)
//                                .size(120.dp, 35.dp),
                                .size(130.dp, 35.dp),
                            enabled = !isEmailVerified.value,
                        ) {
                            Text(
                                text = "인증번호 전송",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 이메일 인증번호
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "이메일 인증번호",
                                fontSize = fontSize,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 5.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            if (isEmailVerified.value) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF009900), // 색상을 Green으로 변경,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                // 공백 화면
                            }
                        }
                        BasicTextField(
                            value = verificationCode.value,
                            onValueChange = { verificationCode.value = it },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                            }),
                            textStyle = TextStyle(
                                fontSize = fontSize,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .drawBehind {
                                            val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                            val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                            drawLine(
                                                color = Color(0xFFEEEEEF),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                        .padding(start = 5.dp, end = 40.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (verificationCode.value.isEmpty()) {
                                        Text(
                                            text = "인증번호를 입력해 주세요.",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        Spacer(Modifier.size(10.dp))

                        // 인증번호 확인 버튼
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isTimerRunning.value) {
                                    val minutes = timerSeconds.value / 60
                                    val seconds = timerSeconds.value % 60
                                    // 타이머
                                    Text(
                                        text = String.format("%02d:%02d", minutes, seconds),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Button(
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = {
                                        // 실제 이메일 인증 로직 처리
//                            isTimerRunning.value = true
                                        isEmailVerified.value = false
                                        Log.e("TAG", "이메일 인증 시작, 타이머 시작됨.")

                                        // 서버 통신 시작
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val result = emptyRetrofit.checkVerificationCode(
                                                    RequestEmailCode(
                                                        emailCodeId = emailCodeId.value,
                                                        emailCode = verificationCode.value
                                                    )
                                                )

                                                if (result.isSuccessful) {
                                                    val resultBody = result.body()
                                                    Log.d("TEST", "인증번호 result body : ${resultBody}")
                                                    if (resultBody != null && resultBody.isSuccess) {
                                                        withContext(Dispatchers.Main) {

                                                            // ✅ 타이머 강제 종료
                                                            isTimerRunning.value = false
                                                            timerSeconds.value = 0

                                                            // 인증완료 테스트를 위한 코드
                                                            isEmailVerified.value = true
                                                            Log.e(
                                                                "TAG",
                                                                "isEmailVerified : ${isEmailVerified.value}"
                                                            )

                                                            // 패스워드 창 보이기
                                                            delay(500)
                                                            isShowResetPwd.value = true

                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT)
                                                                .show()
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("API", "네트워크 오류: ${e.message}")
                                            }
                                        }

                                    },
                                    enabled = isTimerRunning.value,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF385DAB)
                                    ),
//                                    modifier = Modifier.size(120.dp, 35.dp)
                                    modifier = Modifier.size(130.dp, 35.dp)
                                ) {
                                    Text(
                                        text = "확인",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                    }
                }
            }


            AnimatedVisibility(
                visible = isShowResetPwd.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)) + slideInHorizontally(initialOffsetX = { -it / 2 })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isShowResetPwd.value) {
                        // 비밀번호
                        Text(
                            text = "비밀번호",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 5.dp)
                        )
                        BasicTextField(
                            value = pwd1.value,
                            onValueChange = { pwd1.value = it },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                            }),
                            textStyle = TextStyle(fontSize = fontSize),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .drawBehind {
                                            val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                            val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                            drawLine(
                                                color = Color(0xFFEEEEEF),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                        .padding(start = 5.dp, end = 40.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (pwd1.value.isEmpty()) {
                                        Text(
                                            text = "비밀번호를 입력해 주세요.",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        Text(
                            text = "8~16자의 영문, 숫자, 특수문자를 조합해 사용해주세요.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.size(65.dp))

                        // 비밀번호 확인
                        Text(
                            text = "비밀번호 확인",
                            fontWeight = FontWeight.Medium,
                            fontSize = fontSize,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 5.dp)
                        )

                        BasicTextField(
                            value = pwd2.value,
                            onValueChange = { pwd2.value = it },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                            }),
                            textStyle = TextStyle(fontSize = fontSize),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .drawBehind {
                                            val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                            val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                            drawLine(
                                                color = Color(0xFFEEEEEF),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                        .padding(start = 5.dp, end = 40.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (pwd2.value.isEmpty()) {
                                        Text(
                                            text = "비밀번호를 확인 해주세요.",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        // 다음 버튼
                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.btn_next),
                                contentDescription = "다음 버튼",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable {
                                        if (pwd1.value.isEmpty()) {
                                            Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT)
                                                .show()
                                        } else if (pwd2.value.isEmpty()) {
                                            Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT)
                                                .show()
                                        } else if (pwd1.value != pwd2.value) {
                                            Toast.makeText(
                                                context,
                                                "비밀번호가 일치하지 않습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            try {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val resetPwd = emptyRetrofit.resetPwd(RequestUserInfo(email = email.value, pwd = pwd1.value))
                                                    Log.d("TEST", "email  = ${email.value}, pwd = ${pwd1.value})")
                                                    val resetPwdBody = resetPwd.body()
                                                    if (resetPwd.isSuccessful) {
                                                        Log.d("TEST", "resetPwdBody = ${resetPwdBody}")
                                                        withContext(Dispatchers.Main) {
                                                           Toast.makeText(context, "비밀번호가 재설정 되었습니다.", Toast.LENGTH_SHORT).show()
                                                            navController.popBackStack()
                                                        }
                                                    }

                                                    else {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "재설정 실패", Toast.LENGTH_SHORT).show()
//                                                            navController.popBackStack()
                                                        }
                                                        Log.e("TEST", "resetPwd 실패 : ${resetPwd.errorBody().toString()}")
                                                    }
                                                }
                                            } catch(e: Exception) {
                                                Log.e("TEST", "네트워크 에러 : ${e.message}")
                                            }
                                        }
                                    }
                            )
                        }


                        Spacer(modifier = Modifier.height(33.dp))

                    }
                }
            }
        }
    }
}


