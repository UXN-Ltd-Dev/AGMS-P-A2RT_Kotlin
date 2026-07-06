package kr.co.uxn.agms_p_a2rt.ui.components.login

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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.BuildConfig
import kr.co.uxn.agms_p_a2rt.NetworkUtil.isNetworkAvailable
import kr.co.uxn.agms_p_a2rt.api.model.responseDTO.ResponseDuplicateLoginError
import kr.co.uxn.agms_p_a2rt.api.model.responseDTO.ResponseLoginError
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ui.components.main.AlwaysDialog

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val pwd = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading by viewModel.isLoading.collectAsState()

    var showDuplicateLoginDialog = viewModel.isShowDuplicateLoginDialog.collectAsState()
    var showDuplicateLoginKakaoDialog = viewModel.isShowDuplicateKakaoLoginDialog.collectAsState()


    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

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

    if (showDuplicateLoginDialog.value) {
        AlwaysDialog(
            onDismiss = { viewModel.showDuplicateLoginDialog(false)},
            onConfirm = {
                // 중복 로그인인거 인지했고 진행해주세요라고 서버에 전송하는 로직 필요
                viewModel.showDuplicateLoginDialog(false)

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
                                            pwd.value,
                                            isForced = true
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
                                                Toast.makeText(context, R.string.toast_login_error, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        // 2. 1003 : 횟수 5회 이상 초과
                                        1003 -> {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, R.string.toast_login_error_over_five, Toast.LENGTH_SHORT).show()
                                                navController.navigate("PasswordResetScreen")
                                            }
                                        }

                                    }
                                } else if (httpCode == 409) {
                                    val errorBody = login.errorBody()?.string()
                                    val gson = Gson()
                                    val errorResponse = gson.fromJson(errorBody, ResponseDuplicateLoginError::class.java)
//                                            Log.d("TEST", "errorResponse : $errorResponse")
                                    // 중복로그인 다이얼로그 show
                                    viewModel.showDuplicateLoginDialog(true)
                                } else if (httpCode == 410) {
                                    val errorBody = login.errorBody()?.string()
                                    val gson = Gson()
                                    val errorResponse = gson.fromJson(errorBody, ResponseLoginError::class.java)
                                    when (errorResponse.resultCode) {
                                        // 1. 1004 : 6개월 지났을 경우
                                        1004 -> {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, R.string.toast_login_error_authentication_expired, Toast.LENGTH_SHORT).show()
                                                navController.navigate("PasswordResetScreen")
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("TEST", "API통신 실패 : ${login.errorBody()?.string()}")

                                    // 추후 예외처리

                                }
//                                        else if (httpCode == xxx) {
//                                            viewModel.showDuplicateLoginDialog(true)
//                                        }
                            } catch (exception: Exception) {
                                Log.e("TEST", "네트워크 에러 : ${exception.message}")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, R.string.toast_login_error_incorrect_account_info, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, R.string.toast_request_enter_email, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, R.string.toast_login_error, Toast.LENGTH_SHORT).show()

                }

            },
            title = stringResource(R.string.dialog_detect_other_login_title_in_login_screen),
            content = stringResource(R.string.dialog_detect_other_login_content_in_login_screen)
        )
    }

    if (showDuplicateLoginKakaoDialog.value.isShow) {

        val type = showDuplicateLoginKakaoDialog.value.type

        AlwaysDialog(
            onDismiss = { viewModel.showDuplicateKakaoLoginDialog(false, type)},
            onConfirm = {
                // 중복 로그인인거 인지했고 진행해주세요라고 서버에 전송하는 로직 필요
                viewModel.showDuplicateKakaoLoginDialog(false, type)

                if (isNetworkAvailable(context)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            when (type) {
                                // 1. 구글
                                1801 -> {
                                    viewModel.googleLogin(activityContext = context, isForced = true)
                                }
                                // 2. 카카오
                                1802 -> {
                                    viewModel.kakaoLogin(activityContext = context, isForced = true)
                                }
                            }
                        } catch (exception: Exception) {
                            Log.e("TEST", "네트워크 에러 : ${exception.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, R.string.toast_login_error_incorrect_account_info, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, R.string.toast_login_error, Toast.LENGTH_SHORT).show()
                }
            },
            title = stringResource(R.string.dialog_detect_other_login_title_in_login_screen),
            content = stringResource(R.string.dialog_detect_other_login_content_in_login_screen)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            },
        color = colorResource(id = R.color.background_white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                color = colorResource(id = R.color.background_white)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.7f))
                    // UXN로고
                    Image(
                        modifier = Modifier.size(200.dp),
                        painter = painterResource(R.drawable.always_icon_rt),
                        contentDescription = "로고"
                    )


                    // 간편 로그인 시도시 circular 로딩 띄우기
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    } else {

                    }

                    Spacer(modifier = Modifier.weight(0.2f))

                    // 구글 로그인 버튼
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clickable {
                                if (isNetworkAvailable(context)) {
                                    viewModel.googleLogin(context, false)
                                    viewModel.updateIsLoading(true)
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.toast_network_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
//                    painter = painterResource(id = if (isKorean) R.drawable.google_login_high else R.drawable.btn_eng_start_google),
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
                                    viewModel.kakaoLogin(context, false)
                                    viewModel.updateIsLoading(true)
                                    Log.d(
                                        "TEST",
                                        "카카오 네이티브 키 확인 : ${BuildConfig.kakao_native_app_key}"
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.toast_network_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
//                    painter = painterResource(id = if (isKorean) R.drawable.kakao_icon_high else R.drawable.btn_eng_start_with_kakao),
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

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                navController.navigate("EmailLogin")
                            },
                        color = colorResource(R.color.text_secondary),
                        text = stringResource(R.string.start_with_email),
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))



                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        color = colorResource(R.color.text_secondary),
                        text = stringResource(R.string.guide_privacy_policy),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.weight(0.3f))

                }
            }
        }
    }
}
