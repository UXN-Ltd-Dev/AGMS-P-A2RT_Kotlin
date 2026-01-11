package kr.co.uxn.agms_p.ui.components.login

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meticha.permissions_compose.AppPermission
import com.meticha.permissions_compose.rememberAppPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.PasswordChecker
import kr.co.uxn.agms_p.PasswordChecker.checkPwd
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
    val sendRegisterBtnEnabled = remember { mutableStateOf(false) }

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
    val buttonSize = remember { mutableStateOf(IntSize.Zero) }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    LaunchedEffect(timerKey.value) {
        if (isTimerRunning.value) {
            timerSeconds.value = 300 // 5분 설정
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
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
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
                            text = stringResource(R.string.login_email),
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
                                            text = stringResource(R.string.login_email_hint),
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
//                                  // 이메일 인증하기 타이머 초기화 코드
                                    timerKey.value++
                                    isTimerRunning.value = true

                                    // 인증번호 입력란 초기화
                                    verificationCode.value = ""

                                    // 버튼 비활성화
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendRegisterBtnEnabled.value = true
                                    }

                                    // 서버에 이메일 인증하기 요청
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            // 이메일 공백 처리
                                            val trimEmail = email.value.trim()

                                            val result =
//                                                emptyRetrofit.requestVerficationCode(trimEmail)
                                                emptyRetrofit.requestVerificationCodeResetPwd(trimEmail)
                                            val httpCode = result.code()
                                            Log.e("TEST", "http 코드 : $httpCode")

                                            if (result.isSuccessful && httpCode == 200) {
                                                Log.d("TAG", "인증하기 서버 응답: ${result.body()}")
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                        // 인증 번호 전송
                                                        withContext(Dispatchers.Main) {
                                                            // 이메일 인증 하기 타이머 초기화 코드
//                                                            timerKey.value++
//                                                            isTimerRunning.value = true

                                                            Toast.makeText(context, R.string.toast_send_email_code_check_please, Toast.LENGTH_SHORT).show()
                                                            // email_code_id 저장
                                                            emailCodeId.value = resultBody.emailCodeId
                                                        }
                                                        Log.d("TAG", "emailCodeId : ${emailCodeId.value}")


                                                    withContext(Dispatchers.Main) {
                                                        sendRegisterBtnEnabled.value = false
                                                    }
                                                } else {
                                                    Log.d("TAG", "서버 응답이 null 입니다.")
                                                }
                                            } else if (httpCode == 418) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.toast_account_not_registered,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    delay(1000)
                                                    navController.navigate("Login")
                                                }

                                            } else {
                                                Log.d(
                                                    "TAG",
                                                    "API 실패: ${result.errorBody()?.string()}"
                                                )
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.toast_invalid_email_format,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.d("TAG", "네트워크 오류 발생: ${e.message}")
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, R.string.toast_enter_vailid_email, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF385DAB) // 배경색 설정
                            ),
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(35.dp)
                                .onGloballyPositioned { layoutCoordinates ->
                                    buttonSize.value = layoutCoordinates.size
                                },
//                              .size(130.dp, 35.dp),
                            enabled = !sendRegisterBtnEnabled.value,
                        ) {
                            Text(
                                text = stringResource(R.string.send_authentication_code),
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
                                text = stringResource(R.string.email_authentication_code),
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
                                            text = stringResource(R.string.enter_authentication_code),
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
                                        isEmailVerified.value = false
                                        sendRegisterBtnEnabled.value = true
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
                                                    Log.d(
                                                        "TEST",
                                                        "인증번호 result body : ${resultBody}"
                                                    )
                                                    if (resultBody != null && resultBody.isSuccess) {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(
                                                                context,
                                                                R.string.toast_correct_authentcation_code,
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            // ✅ 타이머 강제 종료
                                                            isTimerRunning.value = false
                                                            sendRegisterBtnEnabled.value = true
                                                            timerSeconds.value = 0

                                                            // 인증 완료 테스트를 위한 코드
                                                            isEmailVerified.value = true

                                                            // 패스워드 창 보이기
                                                            delay(500)
                                                            isShowResetPwd.value = true

                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(
                                                                context,
                                                                R.string.toast_incorrect_autehntication_code_check_please,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
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
                                    modifier = Modifier
                                        .size(
                                            width = with(LocalDensity.current) { buttonSize.value.width.toDp() },
                                            height = 35.dp
                                        )
                                ) {
                                    Text(
                                        text = stringResource(R.string.confirm),
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
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)) + slideInHorizontally(
                    initialOffsetX = { -it / 2 })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isShowResetPwd.value) {
                        // 비밀번호
                        Text(
                            text = stringResource(R.string.login_pwd),
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
                                            text = stringResource(R.string.login_pwd_hint),
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        Text(
                            text = stringResource(R.string.pwd_reset_condition_description),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.size(65.dp))

                        // 비밀번호 확인
                        Text(
                            text = stringResource(R.string.pwd_confirm),
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
                                            text = stringResource(R.string.request_pwd_confirm),
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
                                painter = painterResource(id = if(isKorean) R.drawable.btn_next else R.drawable.btn_eng_continue),
                                contentDescription = "다음 버튼",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable {
                                        if (pwd1.value.isEmpty()) {
                                            Toast.makeText(context, R.string.toast_pwd_empty, Toast.LENGTH_SHORT).show()
                                        } else if (checkPwd(pwd1.value) != PasswordChecker.PwdError.NO_ERROR) {
                                            when (checkPwd(pwd1.value)) {
                                                PasswordChecker.PwdError.TOO_SHORT_OR_LONG -> {
                                                    Toast.makeText(context, R.string.toast_pwd_length_error, Toast.LENGTH_SHORT).show()
                                                }

                                                PasswordChecker.PwdError.INVALID_CHAR -> {
                                                    Toast.makeText(context, R.string.toast_pwd_invalid, Toast.LENGTH_SHORT).show()
                                                }

                                                PasswordChecker.PwdError.NO_UPPERCASE -> {
                                                    Toast.makeText(context, R.string.toast_pwd_req_uppercase, Toast.LENGTH_SHORT).show()
                                                }

                                                PasswordChecker.PwdError.NO_LOWERCASE -> {
                                                    Toast.makeText(context, R.string.toast_pwd_req_lowercase, Toast.LENGTH_SHORT).show()
                                                }

                                                PasswordChecker.PwdError.NO_NUMBER -> {
                                                    Toast.makeText(context, R.string.toast_pwd_req_number, Toast.LENGTH_SHORT).show()
                                                }

                                                PasswordChecker.PwdError.NO_SPECIAL_CHAR -> {
                                                    Toast.makeText(context, R.string.toast_pwd_req_special, Toast.LENGTH_SHORT).show()
                                                }

                                                PasswordChecker.PwdError.NO_ERROR -> {}
                                            }
                                        } else if (pwd2.value.isEmpty()) {
                                            Toast.makeText(context, R.string.toast_pwd_empty, Toast.LENGTH_SHORT).show()
                                        } else if (pwd1.value != pwd2.value) {
                                            Toast.makeText(context, R.string.toast_pwd_mismatch, Toast.LENGTH_SHORT).show()
                                        } else {
                                            try {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val resetPwd = emptyRetrofit.resetPwd(
                                                        RequestUserInfo(
                                                            email = email.value,
                                                            pwd = pwd1.value
                                                        )
                                                    )
                                                    Log.d("TEST", "email  = ${email.value}, pwd = ${pwd1.value})"
                                                    )
                                                    val resetPwdBody = resetPwd.body()
                                                    if (resetPwd.isSuccessful) {
                                                        Log.d("TEST", "resetPwdBody = ${resetPwdBody}"
                                                        )
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, R.string.toast_pwd_reset_success, Toast.LENGTH_SHORT).show()
                                                            navController.popBackStack()
                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, R.string.toast_pwd_reset_fail, Toast.LENGTH_SHORT).show()
//                                                            navController.popBackStack()
                                                        }
                                                        Log.e(
                                                            "TEST",
                                                            "resetPwd 실패 : ${resetPwd.errorBody().toString()}"
                                                        )
                                                    }
                                                }
                                            } catch (e: Exception) {
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


