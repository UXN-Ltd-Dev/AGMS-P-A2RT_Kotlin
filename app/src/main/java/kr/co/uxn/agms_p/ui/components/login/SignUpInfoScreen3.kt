package kr.co.uxn.agms_p.ui.components.login

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpOauthDetail
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel


@SuppressLint("LogNotTimber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpInfoScreen3(
    navController: NavController,
    email: String,
    pwd: String,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current

    var expandedForSex by remember { mutableStateOf(false) }
    var expandedForDiabetesType by remember { mutableStateOf(false) }

    val name = remember { mutableStateOf("") }
    val sex = remember { mutableStateOf("") }
//    val age = remember { mutableStateOf(-1) }
    val age = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val diabetesType = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) {
        Log.e("SignUpInfoScreen3", "email: $email, pwd: $pwd")
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
            Spacer(modifier = Modifier.size(90.dp))

            // 내 정보를 입력하세요.
            Text(
                text = "내 정보를 입력하세요.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 혈당, 혈당 추세 ..
            Text(
                text = "혈당, 혈당 추세 등 더욱 정확한 결과를 제공할려면 이 정보가 필요합니다. Always가 제공하는 알고리즘을 적용하기 위해서는 나이 외에도 이러한 추가 정보를 입력해야 여러분에게 꼭 맞는 데이터를 제공할 수 있습니다. 자세히 보기",
                fontSize = 15.sp,
                color = Color(0xFF828282),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 1. 이름
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 50.dp)
            ) {
                Text(text = "이름")
                Spacer(modifier = Modifier.size(60.dp, 27.dp))

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(27.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true
                    )
                }
            }


            // 2. 성별
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "성별")
                Spacer(modifier = Modifier.size(60.dp, 27.dp))
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable {
                            expanded = !expanded
                        }
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sex.value,
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        }
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(100.dp, -200.dp)
                ) {
                    // First section
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "남성",
                            )
                        },
                        onClick = {
                            sex.value = "남성"
                            expanded = !expanded
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("여성") },
                        onClick = {
                            sex.value = "여성"
                            expanded = !expanded
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("선택 안함") },
                        onClick = {
                            sex.value = "선택 안함"
                            expanded = !expanded
                        }
                    )
                }
            }


            // 3. 연령
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "연령")
                Spacer(modifier = Modifier.size(60.dp, 27.dp))

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = age.value,
                        onValueChange = { age.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(27.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true
                    )

//                    NumberPicker(
//                        value = age.value,
//                        range = 13 .. 120,
//                        onValueChange = {
//                            age.value = it
//                        },
//                        dividersColor = Color(0xFF828282)
//                    )

                }
            }

            // 4. 신장
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "신장")
                Spacer(modifier = Modifier.size(60.dp, 27.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = height.value,
                        onValueChange = { height.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(27.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true
                    )
                }
            }


            // 5. 체중
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "체중")
                Spacer(modifier = Modifier.size(60.dp, 27.dp))

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = weight.value,
                        onValueChange = { weight.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(27.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true
                    )
                }
            }

            // 6. 당뇨 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "당뇨 정보")
                // 제1형 당뇨병
                // 제2형 당뇨병
                // 임신성 당뇨병
                // 당뇨 전단계
                // LADA(Latent Autoimmune Diabetes in Adults)
                // 정상
                Spacer(modifier = Modifier.size(27.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable {
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diabetesType.value,
                        modifier = Modifier.clickable {
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                }
                DropdownMenu(
                    expanded = expandedForDiabetesType,
                    onDismissRequest = { expandedForDiabetesType = false },
                    offset = DpOffset(100.dp, -10.dp)
                ) {
                    // First section
                    DropdownMenuItem(
                        text = { Text(text = "정상") },
                        onClick = {
                            diabetesType.value = "정상"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("당뇨 전단계") },
                        onClick = {
                            diabetesType.value = "당뇨 전단계"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("제1형 당뇨병") },
                        onClick = {
                            diabetesType.value = "제1형 당뇨병"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("제2형 당뇨병") },
                        onClick = {
                            diabetesType.value = "제2형 당뇨병"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("임신성 당뇨병") },
                        onClick = {
                            diabetesType.value = "임신성 당뇨병"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("LADA") },
                        onClick = {
                            diabetesType.value = "LADA"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(93.dp))

            // 개인정보 처리방침 및..
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable {
                        checked.value = !checked.value
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked.value,
                    onCheckedChange = { checked.value = it }, // 바꼈을 때, 처리할 곳
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "개인정보 처리방침 및 이용약관에 동의합니다.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(21.dp))

            // 확인 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_confirm),
                    contentDescription = "확인 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            // TODO : int로 변환해야할 체중, 신장, 나이는 string값이 포함되면 runtimeError가 발생한다.
                            // TODO : Picker로 바꿔야 하나.?
                            if (sex.value == "") {
                                Toast.makeText(context, "성별을 선택해 주세요.", Toast.LENGTH_SHORT).show()
                            } else if (age.value == "") {
                                Toast.makeText(context, "나이를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            } else if (height.value == "") {
                                Toast.makeText(context, "신장을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            } else if (weight.value == "") {
                                Toast.makeText(context, "체중을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            } else if (diabetesType.value == "") {
                                Toast.makeText(context, "당뇨 정보를 입력해 주세요.", Toast.LENGTH_SHORT)
                                    .show()
                            } else if (checked.value == false) {
                                Toast.makeText(
                                    context,
                                    "개인정보 처리방침 및 이용약관에 동의해주세요.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                // TODO : 서버에 가입정보 전달
                                // TODO : 가입에 성공하면 로그인 API로 로그인 시도
                                CoroutineScope(Dispatchers.IO).launch {
                                    val diabetesTypeCode: Int =
                                        when (diabetesType.value) {
                                            "제1형 당뇨병" -> 1701
                                            "제2형 당뇨병" -> 1702
                                            "임신성 당뇨병" -> 1703
                                            "당뇨 전단계" -> 1704
                                            "LADA" -> 1705
                                            "정상" -> 1706
                                            else -> throw IllegalArgumentException("Unknown diabetes type")
                                        }

                                    val sexCode: Int =
                                        when (sex.value) {
                                            "남성" -> 1601
                                            "여성" -> 1602
                                            "선택 안함" -> 1603
                                            else -> throw IllegalArgumentException("Unknown diabetes type")
                                        }

                                    val requestSignUpNormal = RequestSignUpNormal(
                                        email = email,
                                        pwd = pwd,
                                        name = name.value,
                                        sex = sexCode,
                                        age = age.value.toInt(),
                                        height = height.value.toInt(),
                                        weight = weight.value.toInt(),
                                        diabetesType = diabetesTypeCode
                                    )

                                    val requestSignUpOauthDetail = RequestSignUpOauthDetail(
                                        userId = loginViewModel.userIdTest,
                                        email = email,
                                        name = name.value,
                                        sex = sexCode,
                                        age = age.value.toInt(),
                                        height = height.value.toInt(),
                                        weight = weight.value.toInt(),
                                        diabetesType = diabetesTypeCode
                                    )

                                    // 이메일 인증 받았다는 전제하에
                                    // 서버에 회원가입 신청

                                    Log.e("TEST", "loginType = ${loginViewModel.signUpType}")
                                    if (loginViewModel.signUpType == 1801 || loginViewModel.signUpType == 1802) {
                                        // 간편로그인 회원가입 및 로그인
                                        try {
                                            // oAuth 상세 정보입력
                                            val result =
                                                tokenRetrofit.oAuthSaveDetailInfo(
                                                    requestSignUpOauthDetail
                                                )
                                            withContext(Dispatchers.Main) {
                                                Log.d(
                                                    "TEST",
                                                    "oAuthSaveDetailInfo requestDto : ${requestSignUpOauthDetail.toString()}"
                                                )
                                            }
                                            if (result.isSuccessful) {
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                    if (resultBody.isSuccess) {

                                                        // 토큰 저장 테스트
                                                        val verifyAccessToken =
                                                            DataStoreManager.getAccessToken()
                                                        val verifyRefreshToken =
                                                            DataStoreManager.getRefreshToken()
                                                        withContext(Dispatchers.Main) {
                                                            Log.d(
                                                                "TEST",
                                                                "oAuthSaveDetailInfo responseBody: $resultBody"
                                                            )

                                                            Log.d(
                                                                "TEST",
                                                                "oAuthSaveDetailInfo responseBody: $resultBody"
                                                            )
                                                            Log.d(
                                                                "TEST",
                                                                "TokenManager | accessToken : $verifyAccessToken\nrefreshToken : $verifyRefreshToken"
                                                            )
                                                            navController.navigate("SettingPermissionScreen")
                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Log.d(
                                                                "TEST", "디테일 정보 입력 실패"
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e(
                                                    "TAG",
                                                    "API 실패: ${result.errorBody()?.string()}"
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                                        }
                                    } else {
                                        // 일반
                                        try {
                                            // 회원가입
                                            val result =
                                                emptyRetrofit.uxnSignUp(requestSignUpNormal)
                                            if (result.isSuccessful) {
                                                Log.d("TAG", "서버 응답: ${result.body()}")
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                    if (!resultBody.isJoined) {
                                                        Log.d("TAG", "회원 가입 성공")
                                                        // TODO: 로그인 시도
                                                        val login =
                                                            emptyRetrofit.uxnLogin(
                                                                signInInfo = RequestSignInNormal(
                                                                    email,
                                                                    pwd
                                                                )
                                                            )
                                                        if (login.isSuccessful) {
                                                            val loginResult = login.body()
                                                            if (loginResult != null) {
                                                                Log.d("TAG", "로그인 성공")

                                                                // 토큰 저장
                                                                DataStoreManager.deleteAccessToken()
                                                                DataStoreManager.saveAccessToken(
                                                                    loginResult.accessToken
                                                                )
                                                                DataStoreManager.saveRefreshToken(
                                                                    loginResult.refreshToken
                                                                )

                                                                // userId 저장
                                                                DataStoreManager.deleteUserId()
                                                                DataStoreManager.saveUserId(
                                                                    loginResult.userId
                                                                )

                                                                // mac 정리
                                                                DataStoreManager.deleteDeviceMac()

                                                                // 화면 이동
                                                                withContext(Dispatchers.Main) {
                                                                    Log.d(
                                                                        "DTO",
                                                                        requestSignUpNormal.toString()
                                                                    )
                                                                    navController.navigate("SettingPermissionScreen")
                                                                }
                                                            }
                                                        } else {
                                                            Log.d("TAG", "로그인 실패")
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "네트워크 연결 오류, 잠시 후 시도해주세요.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    } else {
                                                        Log.d("TAG", "회원 가입 실패")
                                                    }
                                                }
                                            } else {
                                                Log.e(
                                                    "TAG",
                                                    "API 실패: ${result.errorBody()?.string()}"
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                                        }
                                    }
                                }
                            }
                        }
                )
            }
        }
    }
}
