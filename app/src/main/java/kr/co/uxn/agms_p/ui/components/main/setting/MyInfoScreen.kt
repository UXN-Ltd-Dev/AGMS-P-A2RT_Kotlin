package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestUpdateUser
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoScreen(navController: NavController, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 계정 정보
    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val sex = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val diabetesType = remember { mutableStateOf("") }

    var expandedForSex by remember { mutableStateOf(false) }
    var expandedForDiabetesType by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val userId = DataStoreManager.getUserId().first() ?: -1
            Log.e("TEST", "내정보 에서 불러온 userId : ${userId}")
            val getUserData = tokenRetrofit.getUser(userId)
            if (getUserData.isSuccessful) {
                val userData = getUserData.body()
                Log.e("TEST", "userDataBody : ${userData}")
                if (userData != null) {
                    if (userData.isSuccess) {
                        email.value = userData.email
                        name.value = userData.name
                        sex.value = userData.sex
                        age.value = userData.age.toString()
                        height.value = userData.height.toString()
                        weight.value = userData.weight.toString()
                        diabetesType.value = userData.diabetesType
                    }
                }
            } else {
                Log.e("TEST", "API 에러 : ${getUserData.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TEST", "네트워크 에러 : ${e.message}")
        }


    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}")
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "내 정보",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
    ) { paddingValues ->
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
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider()


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                        .padding(top = 60.dp)
                ) {
                    // 내 정보를 입력하세요.
                    Text(
                        text = "현재 이메일 계정",
                        fontSize = 18.sp,
                        color = Color(0xFF828282),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = email.value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(70.dp))

                    // 1. 이름
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 50.dp)
                    ) {
                        Text(
                            text = "이름",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

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
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 성별
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = "성별",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                                fontSize = 20.sp,
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
                                    Text(text = "남성")
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 연령
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = "연령",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 신장
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = "신장",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. 체중
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = "체중",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. 당뇨 정보
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 50.dp)
                    ) {
                        Text(
                            text = "당뇨 정보",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        // 제1형 당뇨병
                        // 제2형 당뇨병
                        // 임신성 당뇨병
                        // 당뇨 전단계
                        // LADA(Latent Autoimmune Diabetes in Adults)
                        // 정상
                        Spacer(modifier = Modifier.width(22.dp))

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
                                fontSize = 20.sp,
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
                                text = { androidx.compose.material3.Text(text = "정상") },
                                onClick = {
                                    diabetesType.value = "정상"
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("당뇨 전단계") },
                                onClick = {
                                    diabetesType.value = "당뇨 전단계"
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("제1형 당뇨병") },
                                onClick = {
                                    diabetesType.value = "제1형 당뇨병"
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("제2형 당뇨병") },
                                onClick = {
                                    diabetesType.value = "제2형 당뇨병"
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("임신성 당뇨병") },
                                onClick = {
                                    diabetesType.value = "임신성 당뇨병"
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("LADA") },
                                onClick = {
                                    diabetesType.value = "LADA"
                                    expandedForDiabetesType = !expandedForDiabetesType
                                }
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(120.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.btn_save),
                            contentDescription = "저장 버튼",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable {
                                    // TODO 서버에 저장 요청

                                    coroutineScope.launch(Dispatchers.IO) {

                                        try {
                                            val userId = DataStoreManager.getUserId().first() ?: -1
                                            Log.e("TEST", "저장버튼 에서 불러온 userId : ${userId}")

                                            val sex = when (sex.value) {
                                                "남성" -> 1601
                                                "여성" -> 1602
                                                else -> 1603
                                            }

                                            Log.e("TEST", "코드로 변환된 sex : ${sex}")

                                            val diabetesType = when (diabetesType.value) {
                                                "제1형 당뇨병" -> 1701
                                                "제2형 당뇨병" -> 1702
                                                "임신성 당뇨병" -> 1703
                                                "당뇨 전단계" -> 1704
                                                "LADA" -> 1705
                                                else -> 1706
                                            }

                                            Log.e("TEST", "코드로 변환된 sex : ${sex}")

                                            val requestUpdateUser = RequestUpdateUser(
                                                userId = userId,
                                                name = name.value,
                                                sex = sex,
                                                age = age.value.toInt(),
                                                height = height.value.toInt(),
                                                weight = weight.value.toInt(),
                                                diabetesType = diabetesType
                                            )
                                            val getUserData =
                                                tokenRetrofit.updateUser(requestUpdateUser)
                                            if (getUserData.isSuccessful) {
                                                val userData = getUserData.body()
                                                Log.e("TEST", "userDataBody : ${userData}")
                                                if (userData != null) {
                                                    if (userData.isSuccess) {
                                                        Log.e("TEST", "DB 저장 성공")
                                                        withContext(Dispatchers.Main) {
                                                            navController.navigate("MainScreen/${2}")
                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            navController.navigate("MainScreen/${2}")
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("TEST", "API 에러 발생 : ${getUserData.errorBody()?.string()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("TEST", "네트워크 에러 발생 : ${e.message}")
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

