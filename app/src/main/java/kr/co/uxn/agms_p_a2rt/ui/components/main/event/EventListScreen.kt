import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestEventData
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ui.model.ItemData
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.EventScreenViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.map

// 테마 컬러 설정 (이미지의 주황색 포인트 컬러)
val PrimaryOrange = Color(0xFFFCA937)
val BackgroundGray = Color(0xFFF5F5F5)
val RecordCardHeight = 72.dp

// 1. 데이터 모델 및 Enum 정의
enum class RecordCategory(val title: String) {
    ALL("전체"),
    MEAL("식사"),
    EXERCISE("운동"),
    INSULIN("인슐린"),
    BLOOD_SUGAR("채혈")
}

data class RecordItem(
    val category: RecordCategory,
    val title: String,
    val description: String,
    val time: String
)

private fun recordCategoryIconRes(category: RecordCategory): Int {
    return when (category) {
        RecordCategory.EXERCISE -> R.drawable.jogging_icon
        RecordCategory.MEAL -> R.drawable.meal_icon
        RecordCategory.INSULIN -> R.drawable.insulin_icon
        RecordCategory.BLOOD_SUGAR -> R.drawable.blood_icon
        else -> R.drawable.event_icon
    }
}

private fun eventTypeToRecordCategory(eventType: Int): RecordCategory {
    return when (eventType) {
        1401 -> RecordCategory.MEAL
        1402 -> RecordCategory.EXERCISE
        1403 -> RecordCategory.BLOOD_SUGAR
        1404 -> RecordCategory.INSULIN
        else -> RecordCategory.ALL
    }
}

private fun recordCategoryToEventType(category: RecordCategory): Int? {
    return when (category) {
        RecordCategory.MEAL -> 1401
        RecordCategory.EXERCISE -> 1402
        RecordCategory.BLOOD_SUGAR -> 1403
        RecordCategory.INSULIN -> 1404
        RecordCategory.ALL -> null
    }
}

private fun formatEventTime(createdAt: String): String {
    return runCatching {
        LocalDateTime.parse(
            createdAt,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MM.dd HH:mm"))
    }.getOrDefault(createdAt)
}

private fun parseEventInstant(createdAt: String): Instant? {
    return runCatching {
        LocalDateTime.parse(
            createdAt,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
            .atZone(ZoneId.of("UTC"))
            .toInstant()
    }.getOrNull()
}

private fun extractEventContentValue(content: String, key: String): String {
    return content
        .split(",")
        .map { it.trim() }
        .firstOrNull { it.startsWith("$key:") }
        ?.substringAfter(":")
        ?.trim()
        .orEmpty()
}

private fun extractEventContentBetween(content: String, startKey: String, endKey: String): String {
    return content
        .substringAfter("$startKey:", missingDelimiterValue = "")
        .substringBefore("$endKey:", missingDelimiterValue = "")
        .trim()
        .trimEnd(',')
        .trim()
}

private fun extractEventContentAfter(content: String, key: String): String {
    return content
        .substringAfter("$key:", missingDelimiterValue = "")
        .trim()
}

private fun ItemData.toRecordItem(): RecordItem {
    val category = eventTypeToRecordCategory(eventType)

    if (category == RecordCategory.EXERCISE) {
        val exerciseType = extractEventContentBetween(content, "운동 종류", "운동 시간")
        val exerciseTime = extractEventContentBetween(content, "운동 시간", "강도")
        val exerciseIntensity = extractEventContentAfter(content, "강도")

        return RecordItem(
            category = category,
            title = exerciseType.ifBlank { category.title },
            description = listOf(exerciseTime, exerciseIntensity)
                .filter { it.isNotBlank() }
                .joinToString(" "),
            time = formatEventTime(time)
        )
    }

    if (category == RecordCategory.MEAL) {
        val mealName = extractEventContentBetween(content, "식사 이름", "식사 내용")
        val mealContent = extractEventContentAfter(content, "식사 내용")

        return RecordItem(
            category = category,
            title = mealName.ifBlank { category.title },
            description = mealContent,
            time = formatEventTime(time)
        )
    }

    if (category == RecordCategory.BLOOD_SUGAR) {
        return RecordItem(
            category = category,
            title = "자가 채혈",
            description = "${content.trim()} mg/dL",
            time = formatEventTime(time)
        )
    }

    if (category == RecordCategory.INSULIN) {
        val insulinType = extractEventContentBetween(content, "인슐린 종류", "투여량")
        val insulinDose = extractEventContentAfter(content, "투여량")

        return RecordItem(
            category = category,
            title = "인슐린 투여",
            description = listOf(insulinType, insulinDose, "단위")
                .filter { it.isNotBlank() }
                .joinToString(" "),
            time = formatEventTime(time)
        )
    }

    return RecordItem(
        category = category,
        title = category.title,
        description = content,
        time = formatEventTime(time)
    )
}

// 임시 데이터
val dummyRecords = listOf(
    RecordItem(RecordCategory.EXERCISE, "조깅", "30분 가볍게", "07:00"),
    RecordItem(RecordCategory.MEAL, "아침 식사", "밥, 국, 반찬 3가지", "08:00"),
    RecordItem(RecordCategory.BLOOD_SUGAR, "식후 혈당", "115 mg/dL", "09:00"),
    RecordItem(RecordCategory.INSULIN, "인슐린 투여", "속효성 4단위", "18:00")
)

// 2. 메인 네비게이션 호스트 (Scaffold의 content 영역에 들어갈 부분)
@Composable
fun EventListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "list",
    eventScreenViewModel: EventScreenViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // 첫 번째 화면: 기록 리스트 화면
        composable("list") {
            RecordListScreen(
                onNavigateToSelect = { navController.navigate("select") },
                eventScreenViewModel = eventScreenViewModel
            )
        }
        // 두 번째 화면: 기록 종류 선택 화면
        composable("select") {
            RecordTypeSelectScreen(
                onTypeSelected = { category ->
                    navController.navigate("detail/${category.name}")
                }
            )
        }
        // 세 번째 화면: 상세 입력 화면
        composable("detail/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val category = RecordCategory.valueOf(categoryName)
            RecordDetailScreen(
                category = category,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// ==========================================
// [첫 번째 화면] 리스트 및 탭 필터링
// ==========================================
@Composable
fun RecordListScreen(onNavigateToSelect: () -> Unit, eventScreenViewModel: EventScreenViewModel) {
    var selectedTab by remember { mutableStateOf(RecordCategory.ALL) }

    // 선택된 탭에 따라 리스트 필터링
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val eventList by eventScreenViewModel.eventItemList.collectAsState()

    val recordList = eventList
        .sortedByDescending { parseEventInstant(it.time) ?: Instant.EPOCH }
        .map { it.toRecordItem() }
    val filteredRecords = if (selectedTab == RecordCategory.ALL) {
        recordList
    } else {
        recordList.filter { it.category == selectedTab }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // onResume 시점에만 실행!
                // 서버로부터 이벤트 목록 받아와서 화면 갱신해주기
                Log.d("TEST", "이벤트 화면에서 onResume일때 DisposableEffect 실행")
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val userId = DataStoreManager.getUserId().first() ?: -1
                        val eventList = tokenRetrofit.getEventList(userId)
                        if (eventList.isSuccessful) {
                            val eventListBody = eventList.body()
                            if (eventListBody != null) {
                                Log.e("TEST", "불러온 eventListBody : ${eventListBody}")
                                val items = eventListBody.map { it ->
                                    ItemData(
                                        eventType = it.eventTypeCode,
                                        time = it.createdAt,
                                        content = it.content
                                    )
                                }
                                eventScreenViewModel.setItems(items)
                            }
                        } else {
                            Log.e("TEST", "API 에러 : ${eventList.errorBody()}")
                        }
                    } catch (e: Exception) {
                        Log.e("TEST", "네트워크 에러 : ${e.message}")
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // 탭 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RecordCategory.values().forEach { category ->
                val isSelected = selectedTab == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) PrimaryOrange else Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedTab = category
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.title,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // 리스트 영역
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(filteredRecords) { record ->
                RecordItemCard(record)
            }
        }

        // 하단 기록하기 버튼




        Button(
            onClick = onNavigateToSelect,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
//            Text("⊕ 기록하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("기록하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

            }
        }
    }
}

@Composable
fun RecordItemCard(record: RecordItem) {
    val iconRes = recordCategoryIconRes(record.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(RecordCardHeight),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 임시 아이콘 영역
            Box(
                modifier = Modifier
                    .size(40.dp),
//                    .background(BackgroundGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
//                Text(record.category.title.first().toString())
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = record.category.title,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = record.description, color = Color.Gray, fontSize = 12.sp)
            }
            Text(text = record.time, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Create, contentDescription = "Edit", tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

// ==========================================
// [두 번째 화면] 기록 종류 선택
// ==========================================
@Composable
fun RecordTypeSelectScreen(onTypeSelected: (RecordCategory) -> Unit) {
    // '전체'를 제외한 항목들
    val selectableTypes = listOf(
        RecordCategory.EXERCISE,
        RecordCategory.MEAL,
        RecordCategory.BLOOD_SUGAR,
        RecordCategory.INSULIN
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("무엇을 기록할까요?", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(selectableTypes) { type ->
                RecordTypeCard(
                    type = type,
                    onClick = { onTypeSelected(type) },
                    showArrow = true
                )
            }
        }
    }
}

@Composable
fun RecordTypeCard(
    type: RecordCategory,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = false,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(RecordCardHeight)
            .then(clickableModifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(recordCategoryIconRes(type)),
                contentDescription = type.title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = type.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = recordCategoryDescription(type), color = Color.Gray, fontSize = 12.sp)
            }
            if (showArrow) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

private fun recordCategoryDescription(type: RecordCategory): String {
    return when (type) {
        RecordCategory.EXERCISE -> "운동 종류와 시간을 기록합니다."
        RecordCategory.MEAL -> "식사 내용을 기록합니다."
        RecordCategory.BLOOD_SUGAR -> "자가 채혈 혈당 측정값을 기록합니다."
        RecordCategory.INSULIN -> "인슐린 종류와 용량을 기록합니다."
        else -> ""
    }
}

private fun formatRecordTime(millis: Long): String {
    val formattedTime = Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN))
    return "기록 시간: $formattedTime"
}

// ==========================================
// [세 번째 화면] 동적 입력 화면
// ==========================================
@Composable
fun RecordDetailScreen(category: RecordCategory, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recordTimeText = remember { formatRecordTime(System.currentTimeMillis()) }
    var exerciseType by remember { mutableStateOf("") }
    var exerciseTime by remember { mutableStateOf("") }
    var exerciseIntensity by remember { mutableStateOf("보통") }
    var mealName by remember { mutableStateOf("") }
    var mealContent by remember { mutableStateOf("") }
    var bloodSugarValue by remember { mutableStateOf("") }
    var insulinDose by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf("초속효성") }

    fun buildContent(): String {
        return when (category) {
            RecordCategory.EXERCISE -> "운동 종류: $exerciseType, 운동 시간: $exerciseTime, 강도: $exerciseIntensity"
            RecordCategory.MEAL -> "식사 이름: $mealName, 식사 내용: $mealContent"
            RecordCategory.BLOOD_SUGAR -> bloodSugarValue
            RecordCategory.INSULIN -> "인슐린 종류: $insulinType, 투여량: $insulinDose"
            else -> ""
        }.trim()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.back_icons),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(40.dp)
                .clickable { onBackClick() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        RecordTypeCard(
            type = category,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 아이템 종류에 따라 다른 UI 출력
        Column(modifier = Modifier.weight(1f)) {
            when (category) {
                RecordCategory.EXERCISE -> ExerciseInputForm(
                    recordTimeText = recordTimeText,
                    type = exerciseType,
                    onTypeChange = { exerciseType = it },
                    time = exerciseTime,
                    onTimeChange = { exerciseTime = it },
                    intensity = exerciseIntensity,
                    onIntensityChange = { exerciseIntensity = it }
                )
                RecordCategory.MEAL -> MealInputForm(
                    recordTimeText = recordTimeText,
                    mealName = mealName,
                    onMealNameChange = { mealName = it },
                    mealContent = mealContent,
                    onMealContentChange = { mealContent = it }
                )
                RecordCategory.BLOOD_SUGAR -> BloodSugarInputForm(
                    recordTimeText = recordTimeText,
                    value = bloodSugarValue,
                    onValueChange = { bloodSugarValue = it }
                )
                RecordCategory.INSULIN -> InsulinInputForm(
                    recordTimeText = recordTimeText,
                    dose = insulinDose,
                    onDoseChange = { insulinDose = it },
                    insulinType = insulinType,
                    onInsulinTypeChange = { insulinType = it }
                )
                else -> {}
            }
        }

        // 하단 저장 버튼
        Button(
            onClick = {
                val eventTypeCode = recordCategoryToEventType(category)
                val content = buildContent()

                if (eventTypeCode == null || content.isBlank()) {
                    Toast.makeText(context, "기록 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val userId = DataStoreManager.getUserId().first() ?: -1
                        val createdAt = LocalDateTime.now(ZoneId.of("UTC"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                        Log.e("TEST", "이벤트 전송하기 전 값 확인 userId : $userId eventCode : $eventTypeCode, createdAt : $createdAt content : $content")


                        val upload = tokenRetrofit.uploadEvent(
                            RequestEventData(
                                userId = userId,
                                createdAt = createdAt,
                                eventTypeCode = eventTypeCode,
                                content = content
                            )
                        )

                        if (upload.isSuccessful) {
                            val uploadBody = upload.body()
                            Log.d("EVENT", "uploadBody : $uploadBody")
                            if (uploadBody?.isSuccess == true) {
                                Log.d("EVENT", "EVENT 업로드 성공, ${uploadBody.message}")
                                withContext(Dispatchers.Main) {
                                    onBackClick()
                                }
                            }
                        } else {
                            Log.e("EVENT", "API 에러 : ${upload.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("EVENT", "네트워크 또는 userId null 에러 : ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("EVENT", "이벤트 업로드 실패")
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("저장", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// --- 개별 입력 폼 컴포저블 ---

@Composable
fun RecordFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryOrange,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
fun ExerciseInputForm(
    recordTimeText: String,
    type: String,
    onTypeChange: (String) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    intensity: String,
    onIntensityChange: (String) -> Unit
) {
    OutlinedTextField(
        value = type, onValueChange = onTypeChange,
        label = { Text("운동 종류 (예: 조깅, 수영)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    OutlinedTextField(
        value = time, onValueChange = onTimeChange,
        label = { Text("운동 시간 (분)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text("강도", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        listOf("가벼움", "보통", "격렬함").forEach { level ->
            RecordFilterChip(
                selected = intensity == level,
                onClick = { onIntensityChange(level) },
                label = level
            )
        }
    }
    Text(recordTimeText, color = Color.Gray)
}

@Composable
fun MealInputForm(
    recordTimeText: String,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    mealContent: String,
    onMealContentChange: (String) -> Unit
) {
    OutlinedTextField(
        value = mealName, onValueChange = onMealNameChange,
        label = { Text("식사 이름 (예: 아침)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    OutlinedTextField(
        value = mealContent, onValueChange = onMealContentChange,
        label = { Text("식사 내용 (예: 밥, 된장국)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text(recordTimeText, color = Color.Gray)
}

@Composable
fun BloodSugarInputForm(
    recordTimeText: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text("혈당값 (mg/dL)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text(recordTimeText, color = Color.Gray)
}

@Composable
fun InsulinInputForm(
    recordTimeText: String,
    dose: String,
    onDoseChange: (String) -> Unit,
    insulinType: String,
    onInsulinTypeChange: (String) -> Unit
) {
    Text("인슐린 종류", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    // 2줄로 칩 배치 (가상의 그리드 느낌)
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("초속효성", "속효성", "지속형").forEach { level ->
                RecordFilterChip(
                    selected = insulinType == level,
                    onClick = { onInsulinTypeChange(level) },
                    label = level
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("혼합형", "중간형").forEach { level ->
                RecordFilterChip(
                    selected = insulinType == level,
                    onClick = { onInsulinTypeChange(level) },
                    label = level
                )
            }
        }
    }

    OutlinedTextField(
        value = dose, onValueChange = onDoseChange,
        label = { Text("투여량 (Unit)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text(recordTimeText, color = Color.Gray)
}
