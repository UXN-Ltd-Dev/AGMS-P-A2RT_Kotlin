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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.RetrofitClient.preRetrofit
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
    loginViewModel: LoginViewModel,
    type : Int
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

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    LaunchedEffect(key1 = Unit) {
//        Log.e("SignUpInfoScreen3", "email: $email, pwd: $pwd")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                text = stringResource(R.string.title_enter_my_info),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 혈당, 혈당 추세 ..
            Text(
                text = stringResource(R.string.desc_user_info_detail),
                fontSize = 16.sp,
                color = Color(0xFF828282),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 1. 이름
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 50.dp, end = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_name),
                    modifier = Modifier.width(90.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                    )
//                Spacer(modifier = Modifier.size(60.dp, 27.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
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
                modifier = Modifier.fillMaxWidth()
                            .padding(start = 50.dp, end = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_gender),
                    modifier = Modifier.width(90.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
//                Spacer(modifier = Modifier.size(60.dp, 27.dp))
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
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
                            Text(
                                text = stringResource(R.string.item_gender_male),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            sex.value = context.getString(R.string.item_gender_male)
                            expanded = !expanded
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            androidx.compose.material.Text(
                                text = stringResource(R.string.item_gender_female),
                                fontSize = 20.sp
                            )
                        },
                        onClick = {
                            sex.value = context.getString(R.string.item_gender_female)
                            expanded = !expanded
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            androidx.compose.material.Text(
                                text = stringResource(R.string.item_gender_none),
                                fontSize = 20.sp
                            )
                        },
                        onClick = {
                            sex.value = context.getString(R.string.item_gender_none)
                            expanded = !expanded
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. 연령
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 20.dp)
            ) {
                androidx.compose.material.Text(
                    text = stringResource(R.string.label_age),
                    modifier = Modifier.width(90.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )
//                Spacer(modifier = Modifier.size(60.dp, 27.dp))

                Box(
                    modifier = Modifier
//                        .width(120.dp)
                        .fillMaxWidth()
                        .padding(start = 5.dp)
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
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 50.dp, end = 20.dp)
            ) {
                androidx.compose.material.Text(
                    text = stringResource(R.string.label_height),
                    modifier = Modifier.width(90.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )
//                Spacer(modifier = Modifier.size(60.dp, 27.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 20.dp)
            ) {
                androidx.compose.material.Text(
                    text = stringResource(R.string.label_weight),
                    modifier = Modifier.width(90.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Box(
                    modifier = Modifier
//                        .width(120.dp)
                        .fillMaxWidth()
                        .padding(start = 5.dp)
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
//                                    .width(120.dp)
                                    .wrapContentWidth()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 20.dp)
            ) {
                Text(
                    modifier = Modifier.width(90.dp),
                    text = stringResource(R.string.label_diabetes_type),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                // 제1형 당뇨병
                // 제2형 당뇨병
                // 임신성 당뇨병
                // 당뇨 전단계
                // LADA(Latent Autoimmune Diabetes in Adults)
                // 정상
                Box(
                    modifier = Modifier
//                        .width(120.dp)
                        .fillMaxWidth()
                        .padding(start = 5.dp)
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
                        text = {
                            Text(
                                text = stringResource(R.string.item_diabetes_type_normal),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_normal)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            androidx.compose.material.Text(
                                text = stringResource(R.string.item_diabetes_type_prediabetes),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_prediabetes)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.item_diabetes_type_1),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_1)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.item_diabetes_type_2),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_2)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.item_diabetes_type_gestational),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_gestational)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.item_diabetes_type_lada),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_lada)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            androidx.compose.material.Text(
                                text = stringResource(R.string.item_diabetes_type_unknown),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light
                            )
                        },
                        onClick = {
                            diabetesType.value = context.getString(R.string.item_diabetes_type_unknown)
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 개인정보 처리방침 및..
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
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
                    text = stringResource(R.string.label_agree_terms_privacy),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 확인 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(bottom = 5.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
//                    painter = painterResource(id = if (isKorean) R.drawable.btn_confirm else R.drawable.btn_eng_confirm),
                    painter = painterResource(id = R.drawable.btn_confirm),
                    contentDescription = "확인 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            if (name.value == "") {
                                Toast.makeText(context, R.string.toast_req_name, Toast.LENGTH_SHORT).show()
                            } else if (sex.value == "") {
                                Toast.makeText(context, R.string.toast_req_gender, Toast.LENGTH_SHORT).show()
                            } else if (age.value == "") {
                                Toast.makeText(context, R.string.toast_req_age, Toast.LENGTH_SHORT).show()
                            } else if (height.value == "") {
                                Toast.makeText(context, R.string.toast_req_height, Toast.LENGTH_SHORT).show()
                            } else if (weight.value == "") {
                                Toast.makeText(context, R.string.toast_req_weight, Toast.LENGTH_SHORT).show()
                            } else if (diabetesType.value == "") {
                                Toast.makeText(context, R.string.toast_req_diabetes_type, Toast.LENGTH_SHORT).show()
                            } else if (checked.value == false) {
                                Toast.makeText(context, R.string.toast_req_agree_terms, Toast.LENGTH_SHORT).show()
                            } else {
                                // TODO : 서버에 가입정보 전달
                                // TODO : 가입에 성공하면 로그인 API로 로그인 시도
                                CoroutineScope(Dispatchers.IO).launch {
                                    val diabetesTypeCode: Int =
                                        when (diabetesType.value) {
                                            context.getString(R.string.item_diabetes_type_1) -> 1701
                                            context.getString(R.string.item_diabetes_type_2) -> 1702
                                            context.getString(R.string.item_diabetes_type_gestational) -> 1703
                                            context.getString(R.string.item_diabetes_type_prediabetes) -> 1704
                                            context.getString(R.string.item_diabetes_type_lada) -> 1705
                                            context.getString(R.string.item_diabetes_type_normal) -> 1706
                                            context.getString(R.string.item_diabetes_type_unknown) -> 1707
                                            else -> throw IllegalArgumentException("Unknown diabetes type")
                                        }

                                    val sexCode: Int =
                                        when (sex.value) {
                                            context.getString(R.string.item_gender_male) -> 1601
                                            context.getString(R.string.item_gender_female) -> 1602
                                            context.getString(R.string.item_gender_none) -> 1603
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
                                        socialType = type,
                                        deviceType = 1101,
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

                                    Log.e("TEST", "loginType = ${type}")
                                    if (type == 1801 || type == 1802) {
                                        // 간편로그인 회원가입 및 로그인
                                        try {
                                            // oAuth 상세 정보입력
                                            val preToken = DataStoreManager.getPreToken().first() ?: ""
                                            val result =
                                                preRetrofit.oAuthSaveDetailInfo(preToken = "Bearer $preToken", requestSignUpOauthDetail)
                                            withContext(Dispatchers.Main) {
                                                Log.d(
                                                    "TEST",
                                                    "oAuthSaveDetailInfo preToken : ${preToken}"
                                                )
                                            }
                                            if (result.isSuccessful) {
                                                val resultBody = result.body()
                                                if (resultBody != null) {
                                                    if (resultBody.isSuccess) {

                                                        // 토큰 저장 테스트
                                                        val accessToken = resultBody.accessToken
                                                        val refreshToken = resultBody.refreshToken
                                                        val userId = resultBody.userId
                                                        val email = resultBody.email

                                                        DataStoreManager.deleteAccessToken()
                                                        DataStoreManager.deleteRefreshToken()
                                                        DataStoreManager.deleteUserId()
                                                        DataStoreManager.deleteEmail()

                                                        DataStoreManager.saveAccessToken(accessToken)
                                                        DataStoreManager.saveRefreshToken(refreshToken)
                                                        DataStoreManager.saveUserId(userId)
                                                        DataStoreManager.saveEmail(email)

                                                        withContext(Dispatchers.Main) {

                                                            navController.navigate("SettingPermissionScreen/${type}") {
                                                                popUpTo("Splash") {
                                                                    inclusive = true
                                                                }
                                                                launchSingleTop = true
                                                            }
                                                        }

                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Log.d("TEST", "디테일 정보 입력 실패")
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
                                                                DataStoreManager.deleteRefreshToken()
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
//                                                                    Log.d(
//                                                                        "DTO",
//                                                                        requestSignUpNormal.toString()
//                                                                    )
                                                                    navController.navigate("SettingPermissionScreen/${type}") {
                                                                        popUpTo("Splash") {
                                                                            inclusive = true
                                                                        }
                                                                        launchSingleTop = true
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            Log.d("TAG", "로그인 실패")
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(
                                                                    context,
                                                                    R.string.toast_network_error2,
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
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.toast_already_registred,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
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
