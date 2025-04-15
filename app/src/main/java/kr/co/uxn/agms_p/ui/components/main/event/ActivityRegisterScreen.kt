package kr.co.uxn.agms_p.ui.components.main.event

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityRegisterScreen(navController: NavController, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val time = remember { mutableStateOf<String>("") }
    val isSelected = remember { mutableStateOf(0) }
    val memo = remember { mutableStateOf<String>("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current



    var interactionSource = remember { MutableInteractionSource() }

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
                                navController.navigate("MainScreen/${1}")
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "생활 등록",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm"))
                    time.value = now
                    Text(
                        modifier = Modifier.padding(start = 20.dp),
                        text = time.value
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Image(
                        modifier = Modifier.size(15.dp),
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "수정 아이콘"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                SegmentedControl(
                    items = listOf("식사", "운동", "인슐린"),
                    selectedIndex = isSelected.value,
                    onItemSelected = { selectedIndex ->
                        if (isSelected.value != selectedIndex) {
                            memo.value = ""
                        }
                        isSelected.value = selectedIndex
                    }
                )


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(start = 30.dp, end = 30.dp, top = 60.dp, bottom = 60.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFFF), // 카드 배경색 설정
                    )
                ) {
                    Text(
                        text = "메모",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 15.dp, top = 15.dp)
                        )
                    TextField(
                        onValueChange = { memo.value = it },
                        value = memo.value,
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
//                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
//                        keyboardActions = KeyboardActions(onDone = {
//                            keyboardController?.hide()
//                        })
                    )
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.btn_save),
                        contentDescription = "저장 버튼",
                        modifier = Modifier.align(Alignment.Center)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (memo.value != "") {
                                    coroutineScope.launch(Dispatchers.IO) {

                                        try {
                                            val userId = DataStoreManager.getUserId().first() ?: -1
                                            val formatterOld = DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm")
                                            val formatterNew = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                            val oldParsedTime = LocalDateTime.parse(time.value, formatterOld)
                                            val newParsedTime = oldParsedTime.format(formatterNew)
                                            val eventTypeCode = when (isSelected.value) {
                                                0 -> 1401
                                                1 -> 1402
                                                else -> 1404
                                            }
                                            Log.e("TEST", "이벤트 전송하기 전 값 확인 userId : $userId eventCode : $eventTypeCode, createdAt : $newParsedTime content : ${memo.value}")
                                            // 서버에 이벤트 전송
                                            val upload = tokenRetrofit.uploadEvent(
                                                RequestEventData(
                                                    userId = userId,
                                                    createdAt = newParsedTime,
                                                    eventTypeCode = eventTypeCode,
                                                    content = memo.value
                                                )
                                            )
                                            if (upload.isSuccessful) {
                                                val uploadBody = upload.body()
                                                Log.e("EVENT", "uploadBody : $uploadBody")
                                                if (uploadBody != null) {
                                                    if (uploadBody.isSuccess) {
                                                        Log.e("EVENT", "EVENT 업로드 성공, ${uploadBody.message}")
                                                        withContext(Dispatchers.Main) {

                                                            val image = when (eventTypeCode) {
                                                                1401 -> R.drawable.event_meal
                                                                1402 -> R.drawable.event_activity
                                                                1403 -> R.drawable.event_calibration
                                                                else -> R.drawable.event_insulin
                                                            }
//                                                            eventScreenViewModel.addItem(ItemData(imageId = image, eventType = eventTypeCode, time = time.value, content = memo.value))
                                                            navController.navigate("MainScreen/${1}")
                                                            Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("EVENT", "API 에러 : ${upload.errorBody()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("EVENT", "네트워크 또는 userId null 에러 : ${e.message}")

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show()
                                                Log.e("EVENT", "활동 이벤트 업로드 실패")
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "메모를 입력해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .fillMaxWidth()
            .background(Color(0xFFEFEFF5),
        shape = RoundedCornerShape(50))
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val backgroundColor = if (isSelected) Color(0xFF385DAB) else Color.Transparent
            val textColor = if (isSelected) Color.White else Color.Black

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(35.dp)
                    .clip(RoundedCornerShape(50))
                    .background(backgroundColor)
                    .clickable { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
