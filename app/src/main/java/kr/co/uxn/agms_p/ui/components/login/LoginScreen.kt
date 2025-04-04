package kr.co.uxn.agms_p.ui.components.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.api.token.TokenManager

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val pwd = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val systemUiController = rememberSystemUiController()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = true // 상태바 아이콘을 밝게 (흰색)
        )
    }

//    fun validateEmail(email: String) {
//        if (email.length >= 8 &&)
//    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            }
    ) {

        // email: abc@gmail.com, pwd: 1234
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.size(40.dp))
                    // UXN로고
                    Image(
                        modifier = Modifier.size(150.dp),
                        painter = painterResource(R.drawable.always_icon),
                        contentDescription = "로고"
                    )
                    // 로그인을 위해 이메일을 입력하세요.
                    Text(
                        text = "이메일",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // 이메일 주소
                    BasicTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
//                                    .focusRequester(focusRequester) // 포커스 요청
//                                    .onFocusChanged { isFocused = !isFocused } // 포커스 감지
                                    .drawBehind {
                                        val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color(0xFFEEEEEF),
//                                            color = if (isFocused) Color.Gray else Color.Blue,
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
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "비밀번호",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    // 비밀 번호
                    BasicTextField(
                        value = pwd.value,
                        onValueChange = { pwd.value = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
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
//                                            color = Color.Gray,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    .padding(start = 5.dp, end = 40.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (pwd.value.isEmpty()) {
                                    Text(
                                        text = "비밀번호를 입력해 주세요.",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.size(10.dp))

            // 회원가입, 비밀번호 찾기
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 40.dp)
            ) {
                Text(
                    text = "회원가입",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("SignUpAgreeScreen1")
                        }
                    )
                Spacer(modifier = Modifier.size(17.dp))
                Text(
                    text = "비밀번호 찾기",
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {

//                        viewModel.apiTest()

//                        navController.navigate("GuideScreen1") // 기존
//                        navController.navigate("EnterFirstGlucose") // 혈당입력 화면
//                        navController.navigate("MainScreen") // 메인화면
                        navController.navigate("ScanFailScreen") // 스캔 실패 화면
//                        Toast.makeText(context, "기능 개발중입니다. \n홈페이지를 이용해주세요.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            Spacer(modifier = Modifier.size(30.dp))

            // 이메일 로그인 버튼
            // 이메일로 시작하기
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        if (email.value != "" && pwd.value != "") {
//                            navController.navigate("LoginPassword/${email.value}")
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // 로그인
                                    val login =
                                        emptyRetrofit.uxnLogin(signInInfo = RequestSignInNormal(email.value, pwd.value))
                                    if (login.isSuccessful) {
                                        val loginResult = login.body()
                                        // 로그인이 성공적으로 되었을 때
                                        if (loginResult != null) {
                                                 Log.e("login","로그인 결과 : ${loginResult.toString()}")
//
                                                 // 토큰 저장
                                                 TokenManager.deleteAccessToken()
                                                 TokenManager.saveAccessToken(loginResult.accessToken)
                                                 TokenManager.saveRefreshToken(loginResult.refreshToken)

                                                 // 세팅화면으로 이동
                                                 withContext(Dispatchers.Main) {
                                                     navController.navigate("SettingPermissionScreen")
                                                 }
                                        }
                                    } else {
                                        Log.e("TEST", "API통신 실패 : ${login.errorBody()?.string()}")
                                    }
                                } catch (exception: Exception) {
                                    Log.e("TEST", "네트워크 에러 : ${exception.message}")
                                }
                            }
                        } else {
                            Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.email_login_high),
                    contentDescription = "메일 로그인 배경",
                    modifier = Modifier
                        .size(width = 300.dp, height = 50.dp)
                )
                Row() {
                    Spacer(Modifier.size(20.dp))
                    Image(
                        painter = painterResource(id = R.drawable.email_icon_high),
                        contentDescription = "메일 아이콘",
                        modifier = Modifier
                            .size(width = 22.dp, height = 22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(50.dp))

            // 간편 로그인
            Text("또는")

            Spacer(modifier = Modifier.size(50.dp))

            // 구글 로그인 버튼
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        viewModel.googleLogin(context)
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_login_high),
                    contentDescription = "구글 로그인 배경",
                    modifier = Modifier
                        .size(width = 300.dp, height = 50.dp)
                )
                Row() {
                    Spacer(Modifier.size(20.dp))
                    Image(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "구글 아이콘",
                        modifier = Modifier
                            .size(width = 22.dp, height = 22.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(10.dp))

            // 카카오 로그인 버튼
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        viewModel.kakaoLogin(context)
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao_icon_high),
                    contentDescription = "카카오 로그인 배경",
                    modifier = Modifier
                        .size(width = 300.dp, height = 50.dp)
                )
                Row() {
                    Spacer(Modifier.size(20.dp))
                    Image(
                        painter = painterResource(id = R.drawable.kakao_login_icon),
                        contentDescription = "카카오 아이콘",
                        modifier = Modifier
                            .size(width = 22.dp, height = 22.dp)
                    )
                }
            }
        }
    }
}
