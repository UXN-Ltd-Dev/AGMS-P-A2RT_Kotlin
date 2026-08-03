package kr.co.uxn.agms_p_a2rt.ui.components.login

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.PasswordChecker
import kr.co.uxn.agms_p_a2rt.PasswordChecker.checkPwd
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestEmailCode
import kr.co.uxn.agms_p_a2rt.ui.components.isKorea

@Composable
fun SignUpCheckScreen2(navController: NavController, type: Int, oAuthEmail: String?) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val verificationCode = remember { mutableStateOf("") }
    val pwd1 = remember { mutableStateOf("") }
    val pwd2 = remember { mutableStateOf("") }
    val isEmailVerified = remember { mutableStateOf(false) }
    val sendRegisterBtnEnabled = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 인증번호 관련 변수
    val timerSeconds = remember { mutableStateOf(0) }
    val isTimerRunning = remember { mutableStateOf(false) }
    val timerKey = remember { mutableStateOf(0) } // 트리거 역할
    val emailCodeId = remember { mutableStateOf(0) }
    val isShowSetPwd = remember { mutableStateOf(false) }

    val fontSize = 18.sp
    val buttonSize = remember { mutableStateOf(IntSize.Zero) }

    val currentLanguage = Locale.current.language
//    val isKorean = currentLanguage == "ko"

    val customSelectionColors = TextSelectionColors(
        handleColor = Color.Black,
        backgroundColor = Color.Black.copy(alpha = 0.3f)
    )

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
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            },
        color = colorResource(R.color.background_white)
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.background_white))
                    .height(70.dp)
                    .align(Alignment.Start)
                    .padding(start = 0.dp, top = 30.dp)
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


            AnimatedVisibility(
                visible = isShowSetPwd.value == false,
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    if (!isShowSetPwd.value) {
                        // 이메일
                        Text(
                            text = stringResource(R.string.login_email),
                            fontSize = fontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Start)
                                .padding(start = 5.dp)
                        )

                        // uxn 회원 가입
                        if (type == 1803) {
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
                                cursorBrush = SolidColor(Color.Black),
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
                            // 구글 회원 가입
                        } else if (type == 1801) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        val strokeWidth = 3.dp.toPx()  // 선 굵기
                                        val y = size.height - strokeWidth / 2
                                        drawLine(
                                            color = Color(0xFFEEEEEF), // 원하는 색상
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    .padding(start = 5.dp, end = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!oAuthEmail.isNullOrEmpty()) {
                                    email.value = oAuthEmail
                                }
                                Text(
                                    text = email.value,
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    fontSize = fontSize,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        } else {
                            // 카카오 회원 가입
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
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color.Black),
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
                                        if (!oAuthEmail.isNullOrEmpty()) {

                                        }

                                        if (email.value == "") {
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
                        }

                        Spacer(Modifier.size(10.dp))

                        // 인증 번호 전송 버튼
                        Button(
                            shape = RoundedCornerShape(10.dp),
                            onClick = {
                                if (email.value != "" && email.value.contains("@")) {
                                    // 이메일 인증하기 타이머 초기화 코드
                                    timerKey.value++
                                    isTimerRunning.value = true

//                                    isTimerRunning.value = false

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

                                            val result = emptyRetrofit.requestVerificationCodeEmail(trimEmail)
                                            val httpCode = result.code()
                                            Log.e("TEST", "http 코드 : $httpCode")

                                            if (result.isSuccessful && httpCode == 200) {
                                                Log.d("TAG", "인증하기 서버 응답: ${result.body()}")
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                        // 인증 번호 전송
                                                        withContext(Dispatchers.Main) {

//                                                            timerKey.value++
//                                                            isTimerRunning.value = true

                                                            Toast.makeText(
                                                                context,
                                                                R.string.toast_send_email_code_check_please,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
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
                                            } else if (httpCode == 415) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.toast_already_registed_account,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
//                                                    delay(1000)
//                                                    navController.navigate("Login")

                                                    sendRegisterBtnEnabled.value = false

                                                }
                                            } else {
                                                Log.d("TAG", "API 실패: ${result.errorBody()?.string()}")
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
                                containerColor = colorResource(R.color.main) // 배경색 설정
                            ),
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(35.dp)
                                .onGloballyPositioned { layoutCoordinates ->
                                    buttonSize.value = layoutCoordinates.size
                                },
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
                                modifier = Modifier.align(Alignment.CenterVertically)
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
                            cursorBrush = SolidColor(Color.Black),
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
                                        Log.d("TAG", "이메일 인증 시작, 타이머 시작됨.")

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
                                                            // 일반 회원가입일 경우, 패스워드 입력창 show
                                                            if(type == 1803) {
                                                                isShowSetPwd.value = true

                                                            } else {
                                                                // 구글, 카카오 회원가입일 경우, 화면이동
                                                                navController.navigate("SignUpInfoScreen3/${email.value}/${pwd1.value}/${type}")
                                                            }

                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(
                                                                context,
                                                                R.string.toast_incorrect_autehntication_code_check_please,
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            // 인증없이 회원가입을 위한 코드
//                                                            isShowSetPwd.value = true
//                                                            isEmailVerified.value = true

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
                                        containerColor = colorResource(R.color.main)
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


//            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isShowSetPwd.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500)) + slideInHorizontally(initialOffsetX = { -it / 2 })
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isShowSetPwd.value) {
                        if(type == 1803) {
                            // 비밀번호
                            Text(
                                text = stringResource(R.string.login_pwd),
                                fontSize = fontSize,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.align(Alignment.Start)
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
                                cursorBrush = SolidColor(Color.Black),
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
                                cursorBrush = SolidColor(Color.Black),
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
                                    .height(65.dp)
                                    .padding(bottom = 15.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Image(
                                    painter = painterResource(id = if(isKorea()) R.drawable.btn_next else R.drawable.btn_eng_next),
                                    contentDescription = "다음 버튼",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clickable {
                                            if (email.value.isEmpty()) {
                                                Toast.makeText(context, R.string.toast_request_enter_email, Toast.LENGTH_SHORT).show()
                                            } else if (pwd1.value.isEmpty()) {
                                                Toast.makeText(context, R.string.toast_pwd_empty, Toast.LENGTH_SHORT).show()
                                            } else if (checkPwd(pwd1.value) != PasswordChecker.PwdError.NO_ERROR) {
                                                when(checkPwd(pwd1.value)) {
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
                                            } else if (!isEmailVerified.value) {
                                                Toast.makeText(context, R.string.toast_req_email_authentication, Toast.LENGTH_SHORT).show()
                                            } else {
                                                // 인증 성공시
                                                // 이메일 공백 처리
                                                val trimEmail = email.value.trim()
                                                Log.d("TEST", "originalEmail : ${email.value}\ntrimEmail : $trimEmail")
                                                navController.navigate("SignUpInfoScreen3/${trimEmail}/${pwd1.value}/${type}")
                                            }
                                        }
                               )
                            }
//                            Spacer(modifier = Modifier.height(33.dp))
                        }
                    }
                }
            }
            }
        }
    }
}
