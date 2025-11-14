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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.network.HttpException
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.NetworkUtil.isNetworkAvailable
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseLoginError
import kr.co.uxn.agms_p.api.token.DataStoreManager

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val pwd = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.isLoading.collectAsState()

    val systemUiController = rememberSystemUiController()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = true // 상태바 아이콘을 밝게 (흰색)
        )
    }


    LaunchedEffect(Unit) {
        viewModel.updateIsLoading(false)
    }

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
//                    Spacer(modifier = Modifier.weight(1f))
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
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 5.dp)
                    )

                    // 이메일 주소
                    BasicTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            autoCorrect = false,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.None
                        ),
                        maxLines = 3,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
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
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "비밀번호",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 5.dp)
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
                        textStyle = TextStyle(
                            fontSize = 18.sp,
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
                                        fontSize = 16.sp
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
                    fontSize = 16.sp,
                    modifier = Modifier
                        // 1803 : uxn 회원가입
                        .clickable {
                            if (isNetworkAvailable(context)) {
                                navController.navigate("SignUpAgreeScreen1/${1803}/${"uxn signup"}")
                            } else {
                                Toast.makeText(context, "네트워크를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                Spacer(modifier = Modifier.size(17.dp))
                Text(
                    text = "비밀번호 찾기",
                    fontSize = 16.sp,
//                    fontFamily = FontFamily(Font(R.font.pretendard)),
//                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        navController.navigate("PasswordResetScreen")
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
//            Spacer(modifier = Modifier.weight(1f))

            // 이메일로 시작하기 버튼
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        if (isNetworkAvailable(context)) {
                            if (email.value != "" && pwd.value != "") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        // 로그인
                                        // 이메일 공백 처리
                                        val trimEmail = email.value.trim()
                                        Log.d(
                                            "TEST",
                                            "originalEmail : ${email.value}\ntrimEmail : $trimEmail"
                                        )

                                        val login =
                                            emptyRetrofit.uxnLogin(
                                                signInInfo = RequestSignInNormal(
                                                    trimEmail,
                                                    pwd.value
                                                )
                                            )

                                        val httpCode = login.code()
                                        Log.e("TEST", "http 코드 : $httpCode")

                                        if (login.isSuccessful && httpCode == 200) {
                                            val loginResult = login.body()
                                            // 로그인이 성공적으로 되었을 때

                                            if (loginResult?.accessToken != null) {
//                                                Log.e("login","로그인 결과 : ${loginResult.toString()}")
//
                                                // 토큰 저장
                                                DataStoreManager.deleteAccessToken()
                                                DataStoreManager.saveAccessToken(loginResult.accessToken)
                                                DataStoreManager.saveRefreshToken(loginResult.refreshToken)

                                                // userId 저장
                                                DataStoreManager.deleteUserId()
                                                DataStoreManager.saveUserId(loginResult.userId)
                                                DataStoreManager.deleteEmail()
                                                DataStoreManager.saveEmail(trimEmail)

                                                // mac 정리
                                                DataStoreManager.deleteDeviceMac()

                                                // 세팅 화면으로 이동
                                                withContext(Dispatchers.Main) {
                                                    navController.navigate("SettingPermissionScreen/${1803}")
                                                }
                                            } else {
                                                Log.e("TEST", "엑세스 토큰 없음 ")
                                            }
                                        } else if (httpCode == 401) {
                                            val errorBody = login.errorBody()?.string()
                                            val gson = Gson()
                                            val errorResponse = gson.fromJson(errorBody, ResponseLoginError::class.java)
                                            when (errorResponse.resultCode) {
                                                // 1. 1002 : 비번 틀릴 때
                                                1002 -> {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "로그인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                // 2. 1003 : 횟수 5회 이상 초과
                                                1003 -> {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "로그인 5회 이상 실패로 계정이 잠겼습니다. 비밀번호를 다시 설정해 주세요.", Toast.LENGTH_SHORT).show()
                                                        navController.navigate("PasswordResetScreen")
                                                    }
                                                }

                                            }
                                        } else if (httpCode == 410) {
                                            val errorBody = login.errorBody()?.string()
                                            val gson = Gson()
                                            val errorResponse = gson.fromJson(errorBody, ResponseLoginError::class.java)
                                            when (errorResponse.resultCode) {
                                                // 1. 1004 : 6개월 지났을 경우
                                                1004 -> {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "인증기간 만료, 비밀번호 재설정으로 재설정 후 로그인해주세요!", Toast.LENGTH_SHORT).show()
                                                        navController.navigate("PasswordResetScreen")
                                                    }
                                                }

                                            }
                                        } else {
                                            Log.e("TEST", "API통신 실패 : ${login.errorBody()?.string()}")
                                        }
                                    } catch (exception: Exception) {
                                    Log.e("TEST", "네트워크 에러 : ${exception.message}")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "계정 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "네트워크를 확인해 주세요.", Toast.LENGTH_SHORT).show()
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
            if (!isLoading) {
                Text(
                    text = "또는",
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.size(50.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.size(24.dp))
            }


            // 구글 로그인 버튼
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        if (isNetworkAvailable(context) ) {
                            viewModel.googleLogin(context)
                            viewModel.updateIsLoading(true)
                        } else {
                            Toast.makeText(context, "네트워크를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                        }
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
                        if (isNetworkAvailable(context)) {
                            viewModel.kakaoLogin(context)
                            viewModel.updateIsLoading(true)
                        } else {
                            Toast.makeText(context, "네트워크를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                        }
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
