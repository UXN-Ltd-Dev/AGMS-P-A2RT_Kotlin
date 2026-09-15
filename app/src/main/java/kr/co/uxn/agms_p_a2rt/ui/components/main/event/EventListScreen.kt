import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.pointer.pointerInput
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
import kr.co.uxn.agms_p_a2rt.GuestList
import kr.co.uxn.agms_p_a2rt.NetworkUtil.isNetworkAvailable
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestEventData
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.UserCalibration
import kr.co.uxn.agms_p_a2rt.ui.components.isKorea
import kr.co.uxn.agms_p_a2rt.ui.components.main.home.DemoGlucoseConfig
import kr.co.uxn.agms_p_a2rt.ui.model.ItemData
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.EventScreenViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.map
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import kr.co.uxn.agms_p_a2rt.util.MealPhotoStore
import java.io.File
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.style.TextOverflow

// 테마 컬러 설정 (이미지의 주황색 포인트 컬러)
val PrimaryOrange = Color(0xFFFCA937)
val BackgroundGray = Color(0xFFF5F5F5)
val RecordCardHeight = 72.dp

// 1. 데이터 모델 및 Enum 정의
enum class RecordCategory(@StringRes val titleResId: Int) {
    ALL(R.string.record_category_all),
    MEAL(R.string.record_category_meal),
    EXERCISE(R.string.record_category_exercise),
    INSULIN(R.string.record_category_insulin),
    BLOOD_SUGAR(R.string.record_category_blood_sugar)
}

data class RecordItem(
    val category: RecordCategory,
    val title: String,
    val description: String,
    val time: String,
    /** 기록 시각. 식사 사진 파일을 찾는 열쇠라 화면 표시용 문자열과 별도로 들고 있다. */
    val timeMillis: Long = 0L,
    /** 서버가 준 원본 내용. 상세 화면에서 항목별로 풀어 쓴다. */
    val rawContent: String = ""
)

private data class IntensityOption(
    val value: String,
    @StringRes val labelResId: Int
)

private data class InsulinTypeOption(
    val value: String,
    @StringRes val labelResId: Int
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
        1403, 1406 -> RecordCategory.BLOOD_SUGAR
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

/**
 * 서버가 준 시각 문자열을 에폭 밀리초로 바꾼다.
 *
 * 식사 사진 파일명이 기록 시각이라 이 값으로 파일을 찾는다.
 * [formatEventTime] 과 같은 규칙(UTC 해석)을 써야 저장할 때와 찾을 때가 어긋나지 않는다.
 */
private fun eventTimeMillis(createdAt: String): Long {
    return runCatching {
        LocalDateTime.parse(
            createdAt,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
            .atZone(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
    }.getOrDefault(0L)
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

private fun localizedExerciseIntensity(context: android.content.Context, value: String): String {
    return when (value) {
        "가벼움" -> context.getString(R.string.exercise_intensity_low)
        "보통" -> context.getString(R.string.exercise_intensity_medium)
        "격렬함" -> context.getString(R.string.exercise_intensity_high)
        else -> value
    }
}

private fun localizedInsulinType(context: android.content.Context, value: String): String {
    return when (value) {
        "초속효성" -> context.getString(R.string.insulin_type_rapid)
        "속효성" -> context.getString(R.string.insulin_type_short)
        "지속형" -> context.getString(R.string.insulin_type_long)
        "혼합형" -> context.getString(R.string.insulin_type_premixed)
        "중간형" -> context.getString(R.string.insulin_type_nph)
        else -> value
    }
}

private fun isKoreanLocale(context: android.content.Context): Boolean {
    return context.resources.configuration.locales[0].language == "ko"
}

private fun formatInsulinDescription(
    context: android.content.Context,
    insulinType: String,
    insulinDose: String
): String {
    val localizedType = localizedInsulinType(context, insulinType)
    val dose = insulinDose.trim()

    return if (isKoreanLocale(context)) {
        listOf(localizedType, dose, "단위")
            .filter { it.isNotBlank() }
            .joinToString(" ")
    } else {
        when {
            dose.isNotBlank() && localizedType.isNotBlank() -> "$dose units of $localizedType"
            dose.isNotBlank() -> "$dose units"
            else -> localizedType
        }
    }
}

private fun ItemData.toRecordItem(context: android.content.Context): RecordItem {
    val category = eventTypeToRecordCategory(eventType)

    if (category == RecordCategory.EXERCISE) {
        val exerciseType = extractEventContentBetween(content, "운동 종류", "운동 시간")
        val exerciseTime = extractEventContentBetween(content, "운동 시간", "강도")
        val exerciseIntensity = localizedExerciseIntensity(
            context = context,
            value = extractEventContentAfter(content, "강도")
        )

        return RecordItem(
            category = category,
            title = exerciseType.ifBlank { context.getString(category.titleResId) },
            description = listOf(exerciseTime, exerciseIntensity)
                .filter { it.isNotBlank() }
                .joinToString(" "),
            time = formatEventTime(time),
            timeMillis = eventTimeMillis(time),
            rawContent = content
        )
    }

    if (category == RecordCategory.MEAL) {
        val mealName = extractEventContentBetween(content, "식사 이름", "식사 내용")
        val mealContent = extractEventContentAfter(content, "식사 내용")

        return RecordItem(
            category = category,
            title = mealName.ifBlank { context.getString(category.titleResId) },
            description = mealContent,
            time = formatEventTime(time),
            timeMillis = eventTimeMillis(time),
            rawContent = content
        )
    }

    if (category == RecordCategory.BLOOD_SUGAR) {
        return RecordItem(
            category = category,
            title = context.getString(R.string.record_self_blood_glucose),
            description = "${content.trim()} mg/dL",
            time = formatEventTime(time),
            timeMillis = eventTimeMillis(time),
            rawContent = content
        )
    }

    if (category == RecordCategory.INSULIN) {
        val insulinType = extractEventContentBetween(content, "인슐린 종류", "투여량")
        val insulinDose = extractEventContentAfter(content, "투여량")

        return RecordItem(
            category = category,
            title = context.getString(R.string.record_insulin_administration),
            description = formatInsulinDescription(
                context = context,
                insulinType = insulinType,
                insulinDose = insulinDose
            ),
            time = formatEventTime(time),
            timeMillis = eventTimeMillis(time),
            rawContent = content
        )
    }

    return RecordItem(
        category = category,
        title = context.getString(category.titleResId),
        description = content,
        time = formatEventTime(time),
        timeMillis = eventTimeMillis(time),
        rawContent = content
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
    startDestination: String = "home",
    eventScreenViewModel: EventScreenViewModel,
    initialRecordTimeMillis: Long? = null
) {
    // 예전에는 "list"/"select" 가 각각 NavHost 의 라우트였다. 지금은 한 화면 안의 탭이라
    // NavHost 에는 home 뿐이다. 다른 화면에서 옛 이름으로 넘어오는 경로가 남아 있어
    // (분석 화면의 혈당 기록 버튼 등) 그 이름을 탭 선택으로 옮긴다.
    // 옛 이름을 그대로 NavHost 에 넘기면 "is not a direct child of this NavGraph" 로 죽는다.
    val initialTab = if (startDestination == "list") RecordHomeTab.LIST else RecordHomeTab.SELECT

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // 첫 번째 화면: 상단 탭으로 기록 선택과 전체 리스트를 오간다.
        composable("home") {
            RecordHomeScreen(
                initialTab = initialTab,
                eventScreenViewModel = eventScreenViewModel,
                onTypeSelected = { category ->
                    navController.navigate(
                        "detail/${category.name}/${initialRecordTimeMillis ?: -1L}"
                    )
                }
            )
        }
        // 두 번째 화면: 상세 입력 화면
        composable("detail/{categoryName}/{recordTimeMillis}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val category = RecordCategory.valueOf(categoryName)
            val recordTimeMillis = backStackEntry.arguments
                ?.getString("recordTimeMillis")
                ?.toLongOrNull()
                ?.takeIf { it >= 0L }
            RecordDetailScreen(
                category = category,
                initialRecordTimeMillis = recordTimeMillis,
                onBackClick = {
                    val returnedToHome = navController.popBackStack(
                        route = "home",
                        inclusive = false
                    )
                    if (!returnedToHome) {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

/** 상단 탭에서 고를 수 있는 화면. 기본은 기록 선택이다. */
enum class RecordHomeTab(@StringRes val titleRes: Int) {
    SELECT(R.string.record_home_tab_select),
    LIST(R.string.record_home_tab_list)
}

/**
 * 기록 탭의 첫 화면.
 *
 * 예전에는 리스트가 먼저 나오고 하단 버튼으로 기록 선택으로 넘어갔다. 기록하러 들어오는
 * 경우가 대부분이라 선택 화면을 기본으로 두고, 지난 기록은 탭으로 넘겨 본다.
 */
@Composable
fun RecordHomeScreen(
    initialTab: RecordHomeTab = RecordHomeTab.SELECT,
    eventScreenViewModel: EventScreenViewModel,
    onTypeSelected: (RecordCategory) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        RecordHomeTabBar(
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )
        when (selectedTab) {
            RecordHomeTab.SELECT -> RecordTypeSelectScreen(onTypeSelected = onTypeSelected)
            RecordHomeTab.LIST -> RecordListScreen(eventScreenViewModel = eventScreenViewModel)
        }
    }
}

/**
 * 상단 탭 막대.
 *
 * 고른 탭만 흰 알약으로 떠오르게 한다. 화면의 다른 카드들이 흰 배경에 둥근 모서리라
 * 같은 결로 맞췄다.
 */
@Composable
private fun RecordHomeTabBar(
    selected: RecordHomeTab,
    onSelect: (RecordHomeTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEDEDED))
            .padding(4.dp)
    ) {
        RecordHomeTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(tab.titleRes),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color.Gray
                )
            }
        }
    }
}

// ==========================================
// [첫 번째 화면] 리스트 및 탭 필터링
// ==========================================
@Composable
fun RecordListScreen(eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(RecordCategory.ALL) }

    // 목록에서 고른 기록. null 이면 상세 화면을 닫아 둔 상태다.
    var selectedRecord by remember { mutableStateOf<RecordItem?>(null) }

    // 선택된 탭에 따라 리스트 필터링
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val eventList by eventScreenViewModel.eventItemList.collectAsState()

    val recordList = eventList.map { it.toRecordItem(context) }

    // 임시 데이터. 시각을 넣어 두지 않으면 정렬할 때 맨 아래로 몰린다.
    val dummyRecords = remember {
        fun todayAt(hour: Int): Long = LocalDate.now()
            .atTime(hour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        listOf(
            RecordItem(RecordCategory.EXERCISE, "조깅", "30분 가볍게", "07:00", todayAt(7)),
            RecordItem(RecordCategory.MEAL, "아침 식사", "밥, 국, 반찬 3가지", "08:00", todayAt(8)),
            RecordItem(RecordCategory.BLOOD_SUGAR, "식후 혈당", "115 mg/dL", "09:00", todayAt(9)),
            RecordItem(RecordCategory.INSULIN, "인슐린 투여", "속효성 4단위", "18:00", todayAt(18))
        )
    }
    val recordsWithDemo = if (DemoGlucoseConfig.ENABLED) {
        recordList + dummyRecords
    } else {
        recordList
    }

    // 정렬은 화면에 내보내는 목록에서 한 번만 한다. 위 단계에서 정렬해 두면
    // 뒤에 무엇이든 덧붙이는 순간 순서가 깨지고, 어느 탭을 눌러도 그대로 남는다.
    // 시각을 읽지 못한 기록은 timeMillis 가 0 이라 맨 아래로 간다.
    val filteredRecords = recordsWithDemo
        .filter { selectedTab == RecordCategory.ALL || it.category == selectedTab }
        .sortedByDescending { it.timeMillis }

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
                        text = stringResource(category.titleResId),
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }


        if (!DemoGlucoseConfig.ENABLED && !isNetworkAvailable(context)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center // 중앙 정렬
            ) {
                Text(
                    text = stringResource(R.string.toast_network_error),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center // 중앙 정렬
            ) {
                Text(
                    text = stringResource(R.string.recent_activity_sub_title),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredRecords) { record ->
                    RecordItemCard(record, onClick = { selectedRecord = record })
                }
            }
        }
    }

    selectedRecord?.let { record ->
        RecordDetailSheet(record = record, onDismiss = { selectedRecord = null })
    }
}

@Composable
fun RecordItemCard(record: RecordItem, onClick: () -> Unit = {}) {
    val iconRes = recordCategoryIconRes(record.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(RecordCardHeight)
            .clickable { onClick() },
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
                    contentDescription = stringResource(record.category.titleResId),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // 기록에서 눈으로 찾는 것은 제목이 아니라 값이다. 혈당 수치, 식사 내용,
            // 인슐린 용량이 그것이다. 제목을 작게 두고 값을 크게 올린다.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    color = Color(0xFF7A7A7A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (record.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.description,
                        color = Color(0xFF222222),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(text = record.time, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==========================================
// [두 번째 화면] 기록 종류 선택
// ==========================================

/**
 * 목록에서 고른 기록 하나를 자세히 본다.
 *
 * 목록은 한 줄에 요약만 담아 놓쳐 버리는 값이 있다. 식사의 이름과 내용, 사진,
 * 운동의 시간과 강도 같은 것이다. 여기서 입력할 때 넣은 값을 그대로 되짚는다.
 *
 * 읽기 전용이다. 고치는 기능은 아직 없다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordDetailSheet(
    record: RecordItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val photo = remember(record.timeMillis) {
        if (record.category == RecordCategory.MEAL) {
            MealPhotoStore.photoOf(context, record.timeMillis)
        } else null
    }

    // 중간 단계를 건너뛰고 한 번에 끝까지 펼친다. 반쯤 열리면 사진이 있는 식사 기록에서
    // 닫기 단추가 화면 밖에 남아, 값을 보려고 열었다가 한 번 더 끌어올려야 한다.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 내용만 스크롤한다. 닫기는 이 바깥에 두어 사진이 길어도 화면에 남는다.
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(recordCategoryIconRes(record.category)),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(record.category.titleResId),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            RecordDetailRow(stringResource(R.string.record_detail_view_time), record.time)
            RecordDetailFields(record)

            if (record.category == RecordCategory.MEAL) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.record_detail_view_photo),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (photo != null) {
                    val bitmap = remember(photo.path) {
                        runCatching { BitmapFactory.decodeFile(photo.path)?.asImageBitmap() }.getOrNull()
                    }
                    if (bitmap != null) {
                        // 목록에서는 작게 보이던 사진을 여기서는 원본 비율로 크게 보여 준다.
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.record_detail_view_no_photo),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

        }

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp)
                .height(50.dp)
        ) {
            Text(
                stringResource(R.string.record_detail_view_close),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
    }
}

/**
 * 기록 종류마다 입력했던 항목을 풀어 쓴다.
 *
 * 목록의 요약 문자열을 그대로 쓰지 않고 원본에서 다시 뽑는다. 요약은 한 줄에 넣으려고
 * 붙여 놓은 것이라 어느 값이 무엇인지 알 수 없다.
 */
@Composable
private fun RecordDetailFields(record: RecordItem) {
    val context = LocalContext.current
    val content = record.rawContent

    when (record.category) {
        RecordCategory.MEAL -> {
            RecordDetailRow(
                stringResource(R.string.record_detail_view_meal_name),
                extractEventContentBetween(content, "식사 이름", "식사 내용")
            )
            RecordDetailRow(
                stringResource(R.string.record_detail_view_meal_content),
                extractEventContentAfter(content, "식사 내용")
            )
        }
        RecordCategory.EXERCISE -> {
            RecordDetailRow(
                stringResource(R.string.record_detail_view_exercise_type),
                extractEventContentBetween(content, "운동 종류", "운동 시간")
            )
            RecordDetailRow(
                stringResource(R.string.record_detail_view_exercise_time),
                extractEventContentBetween(content, "운동 시간", "강도")
            )
            RecordDetailRow(
                stringResource(R.string.record_detail_exercise_description_sub_title),
                localizedExerciseIntensity(context, extractEventContentAfter(content, "강도"))
            )
        }
        RecordCategory.BLOOD_SUGAR -> {
            RecordDetailRow(
                stringResource(R.string.record_detail_view_bg_value),
                record.description
            )
        }
        RecordCategory.INSULIN -> {
            RecordDetailRow(
                stringResource(R.string.record_detail_insulin_type),
                localizedInsulinType(context, extractEventContentBetween(content, "인슐린 종류", "투여량"))
            )
            RecordDetailRow(
                stringResource(R.string.record_detail_insulin),
                extractEventContentAfter(content, "투여량")
            )
        }
        else -> {
            RecordDetailRow(stringResource(record.category.titleResId), content)
        }
    }
}

@Composable
private fun RecordDetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

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
        Text(stringResource(R.string.record_type_select_screen_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

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
    title: String? = null,
    modifier: Modifier = Modifier
) {
    val displayTitle = title ?: if (type == RecordCategory.BLOOD_SUGAR) {
        stringResource(R.string.record_blood_glucose_value)
    } else {
        stringResource(type.titleResId)
    }
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
                contentDescription = stringResource(type.titleResId),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = recordCategoryDescription(type, isKorea()), color = Color.Gray, fontSize = 12.sp)
            }
            if (showArrow) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

private fun recordCategoryDescription(type: RecordCategory, isKorea: Boolean): String {
    return when (type) {

        RecordCategory.EXERCISE -> if (isKorea) "운동 종류와 시간을 기록합니다." else "Log exercise type and duration."
        RecordCategory.MEAL -> if (isKorea) "식사 내용을 기록합니다." else "Log your meal details."
        RecordCategory.BLOOD_SUGAR -> if (isKorea) "자가 채혈 혈당 측정값을 기록합니다." else "Log manual blood test readings."
        RecordCategory.INSULIN -> if (isKorea) "인슐린 종류와 용량을 기록합니다." else "Log insulin type and dosage."
        else -> ""
    }
}

private fun formatRecordTime(millis: Long, isKorea: Boolean): String {
    val deviceZoneId = TimeZone.getDefault().toZoneId()
    val pattern = if (isKorea) {
        "M월 d일 HH:mm"
    } else {
        "MMM d, HH:mm"
    }
    val formattedTime = Instant.ofEpochMilli(millis)
        .atZone(deviceZoneId)
        .format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    return if(isKorea) "기록 시간: $formattedTime" else "Log Time: $formattedTime"
}


/**
 * 기록 시각과 그것을 고치는 연필 버튼.
 *
 * 기본값은 지금 시각이다. 대개는 그대로 두면 되지만, 먹고 나서 한참 뒤에 적는 일이
 * 흔해서 고칠 길을 열어 둔다. 화면 아래쪽에 조용히 놓여야 하는 줄이라 연필은 작게 둔다.
 */
@Composable
private fun RecordTimeRow(text: String, onEditClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onEditClick() }
    ) {
        Text(text, color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Create,
            contentDescription = stringResource(R.string.record_time_edit),
            tint = PrimaryOrange,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 기록 시각을 고르는 두 단계 대화상자. 날짜를 먼저 고르고 시간을 고른다.
 *
 * 한 화면에 달력과 시계를 같이 올리면 작은 기기에서 잘린다. 그래서 나눴다.
 *
 * 앞으로의 시각은 받지 않는다. 아직 하지 않은 식사가 혈당 그래프에 먼저 찍히면
 * 나중에 그래프를 볼 때 앞뒤가 맞지 않는다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTimeEditDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val context = LocalContext.current
    val zone = remember { TimeZone.getDefault().toZoneId() }
    val initial = remember(initialMillis) { Instant.ofEpochMilli(initialMillis).atZone(zone) }

    var pickingTime by remember { mutableStateOf(false) }

    // 달력은 UTC 자정 기준으로 값을 주고받는다. 기기 시간대로 그냥 넘기면 하루가 밀린다.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )
    // 24시간 다이얼은 13~24 가 안쪽 원에 붙어 있어 손가락으로 집기 어렵다.
    // 12시간제로 두면 바깥 원 12 칸만 쓰고 오전/오후는 단추로 고른다.
    val timePickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false
    )

    // 다이얼로 어림잡기 어려운 분을 넣을 때를 위해 숫자로 적는 길도 둔다.
    var typingTime by remember { mutableStateOf(false) }

    val datePickerColors = DatePickerDefaults.colors(
        containerColor = Color.White,
        titleContentColor = Color.Black,
        headlineContentColor = Color.Black,
        weekdayContentColor = Color.Gray,
        subheadContentColor = Color.Black,
        navigationContentColor = Color.Black,
        yearContentColor = Color.Black,
        dayContentColor = Color.Black,
        selectedDayContainerColor = PrimaryOrange,
        selectedDayContentColor = Color.White,
        todayContentColor = PrimaryOrange,
        todayDateBorderColor = PrimaryOrange,
        selectedYearContainerColor = PrimaryOrange,
        selectedYearContentColor = Color.White
    )
    val timePickerColors = TimePickerDefaults.colors(
        clockDialColor = Color(0xFFF3F3F3),
        clockDialSelectedContentColor = Color.White,
        clockDialUnselectedContentColor = Color.Black,
        selectorColor = PrimaryOrange,
        containerColor = Color.White,
        periodSelectorSelectedContainerColor = PrimaryOrange,
        periodSelectorSelectedContentColor = Color.White,
        periodSelectorUnselectedContainerColor = Color.White,
        timeSelectorSelectedContainerColor = Color(0xFFFDEBCB),
        timeSelectorSelectedContentColor = Color.Black,
        timeSelectorUnselectedContainerColor = Color(0xFFF3F3F3),
        timeSelectorUnselectedContentColor = Color.Black
    )

    fun confirm() {
        val dayMillis = datePickerState.selectedDateMillis ?: return
        val picked = Instant.ofEpochMilli(dayMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .atTime(timePickerState.hour, timePickerState.minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        if (picked > System.currentTimeMillis()) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_record_time_future),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        onConfirm(picked)
    }

    if (!pickingTime) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            colors = DatePickerDefaults.colors(containerColor = Color.White),
            confirmButton = {
                TextButton(onClick = { pickingTime = true }) {
                    Text(
                        stringResource(R.string.record_time_edit_next),
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        ) {
            val headlineText = datePickerState.selectedDateMillis
                ?.let { millis ->
                    Instant.ofEpochMilli(millis)
                        .atZone(ZoneOffset.UTC)
                        .format(
                            DateTimeFormatter.ofPattern(
                                if (isKorea()) "yyyy년 M월 d일" else "MMM d, yyyy",
                                Locale.getDefault()
                            )
                        )
                }
                .orEmpty()

            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        stringResource(R.string.record_time_edit_date),
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                headline = {
                    Text(
                        headlineText,
                        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                showModeToggle = false,
                colors = datePickerColors
            )
        }
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.record_time_edit_time),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                // 아이콘을 쓰면 라이브러리를 늘려야 해서 글자로 둔다. 확인·취소와 같은 모양이다.
                TextButton(onClick = { typingTime = !typingTime }) {
                    Text(
                        stringResource(
                            if (typingTime) R.string.record_time_edit_dial_mode
                            else R.string.record_time_edit_input_mode
                        ),
                        color = PrimaryOrange,
                        fontSize = 13.sp
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (typingTime) {
                    TimeInput(state = timePickerState, colors = timePickerColors)
                } else {
                    TimePicker(state = timePickerState, colors = timePickerColors)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { confirm() }) {
                Text(
                    stringResource(R.string.confirm),
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { pickingTime = false }) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        }
    )
}

// ==========================================
// [세 번째 화면] 동적 입력 화면
// ==========================================
@Composable
fun RecordDetailScreen(
    category: RecordCategory,
    initialRecordTimeMillis: Long?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val localDbRepository = remember(context) { AppDatabase.getInstance(context) }
    var recordTimeMillis by remember(initialRecordTimeMillis) {
        mutableStateOf(initialRecordTimeMillis ?: System.currentTimeMillis())
    }
    // 사용자가 직접 고른 시각인지. 홈 화면에서 들고 온 시각과 저장 규칙이 달라 구분한다.
    var recordTimeEdited by remember(initialRecordTimeMillis) { mutableStateOf(false) }
    var showTimeEditor by remember { mutableStateOf(false) }
    val isKorea = isKorea()
    val recordTimeText = remember(recordTimeMillis) { formatRecordTime(recordTimeMillis, isKorea) }
    var exerciseType by remember { mutableStateOf("") }
    var exerciseTime by remember { mutableStateOf("") }
    var exerciseIntensity by remember { mutableStateOf("보통") }
    var mealName by remember { mutableStateOf("") }
    var mealContent by remember { mutableStateOf("") }
    // 촬영된 임시 파일. 저장을 눌러야 기록 시각 이름으로 옮긴다.
    var mealPhoto by remember { mutableStateOf<File?>(null) }

    // 앞서 찍고 저장하지 않은 사진이 남아 있을 수 있다. 화면에 들어올 때 치운다.
    LaunchedEffect(Unit) { MealPhotoStore.clearTemp(context) }
    var bloodSugarValue by remember { mutableStateOf("") }
    var useCalibration by remember(category) { mutableStateOf(false) }
    var isVipUser by remember { mutableStateOf(false) }
    var insulinDose by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf("초속효성") }

    LaunchedEffect(Unit) {
        val email = withContext(Dispatchers.IO) {
            DataStoreManager.getEmail().first().orEmpty()
        }
        isVipUser = GuestList.getVipList().contains(email)
        if (!isVipUser) useCalibration = false
    }

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
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
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
            title = if (category == RecordCategory.BLOOD_SUGAR) {
                stringResource(R.string.record_blood_glucose_input)
            } else {
                stringResource(category.titleResId)
            },
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 아이템 종류에 따라 다른 UI 출력.
        // 식사 사진을 원본 비율로 크게 보여 주면 화면을 넘길 수 있어 스크롤을 둔다.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (category) {
                RecordCategory.EXERCISE -> ExerciseInputForm(
                    recordTimeText = recordTimeText,
                    onEditTime = { showTimeEditor = true },
                    type = exerciseType,
                    onTypeChange = { exerciseType = it },
                    time = exerciseTime,
                    onTimeChange = { exerciseTime = it },
                    intensity = exerciseIntensity,
                    onIntensityChange = { exerciseIntensity = it }
                )
                RecordCategory.MEAL -> MealInputForm(
                    recordTimeText = recordTimeText,
                    onEditTime = { showTimeEditor = true },
                    mealName = mealName,
                    onMealNameChange = { mealName = it },
                    mealContent = mealContent,
                    onMealContentChange = { mealContent = it },
                    photoFile = mealPhoto,
                    onPhotoCaptured = { mealPhoto = it }
                )
                RecordCategory.BLOOD_SUGAR -> BloodSugarInputForm(
                    recordTimeText = recordTimeText,
                    onEditTime = { showTimeEditor = true },
                    value = bloodSugarValue,
                    onValueChange = { bloodSugarValue = it },
                    useCalibration = useCalibration,
                    onUseCalibrationChange = { useCalibration = it },
                    showCalibrationOption = isVipUser
                )
                RecordCategory.INSULIN -> InsulinInputForm(
                    recordTimeText = recordTimeText,
                    onEditTime = { showTimeEditor = true },
                    dose = insulinDose,
                    onDoseChange = { insulinDose = it },
                    insulinType = insulinType,
                    onInsulinTypeChange = { insulinType = it }
                )
                else -> {}
            }
        }

        if (showTimeEditor) {
            RecordTimeEditDialog(
                initialMillis = recordTimeMillis,
                onDismiss = { showTimeEditor = false },
                onConfirm = { picked ->
                    recordTimeMillis = picked
                    recordTimeEdited = true
                    showTimeEditor = false
                }
            )
        }

        // 하단 저장 버튼
        Button(
            onClick = {
                val defaultEventTypeCode = recordCategoryToEventType(category)
                val eventTypeCode = if (
                    category == RecordCategory.BLOOD_SUGAR && isVipUser && useCalibration
                ) {
                    1406
                } else {
                    defaultEventTypeCode
                }
                val content = buildContent()

                if (eventTypeCode == null || content.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.toast_enter_record), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val calibrationGlucose = if (category == RecordCategory.BLOOD_SUGAR) {
                    content.toDoubleOrNull()
                } else {
                    null
                }
                if (category == RecordCategory.BLOOD_SUGAR && calibrationGlucose == null) {
                    Toast.makeText(context, context.getString(R.string.toast_req_glucose), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val hasEmptyRequiredField = when (category) {
                    RecordCategory.EXERCISE -> {
                        exerciseType.isBlank() || exerciseTime.isBlank()
                    }

                    RecordCategory.MEAL -> {
                        mealName.isBlank() || mealContent.isBlank()
                    }

                    RecordCategory.BLOOD_SUGAR -> {
                        bloodSugarValue.isBlank()
                    }

                    RecordCategory.INSULIN -> {
                        insulinDose.isBlank()
                    }
                    else -> false
                }

                if (hasEmptyRequiredField) {
                    Toast.makeText(context, context.getString(R.string.toast_enter_record), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val userId = DataStoreManager.getUserId().first() ?: -1
                        // 홈 화면 그래프에서 넘어온 시각은 그 분의 끝(:59)으로 맞춘다.
                        // 같은 분의 혈당값보다 뒤에 놓여야 그래프에서 순서가 맞는다.
                        // 사용자가 직접 고른 시각은 손대지 않는다.
                        val uploadRecordTimeMillis = if (
                            initialRecordTimeMillis != null && !recordTimeEdited
                        ) {
                            Instant.ofEpochMilli(recordTimeMillis)
                                .atZone(ZoneId.systemDefault())
                                .withSecond(59)
                                .withNano(0)
                                .toInstant()
                                .toEpochMilli()
                        } else {
                            recordTimeMillis
                        }
                        // 사진은 서버로 보내지 않는다. 기기 안에 기록 시각 이름으로 남긴다.
                        // 서버 전송이 실패해도 사진은 남도록 전송보다 먼저 옮긴다.
                        if (category == RecordCategory.MEAL) {
                            mealPhoto?.let { temp ->
                                MealPhotoStore.commit(context, temp, uploadRecordTimeMillis)
                            }
                        }

                        val recordInstant = Instant.ofEpochMilli(uploadRecordTimeMillis)
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val createdAtUtc = recordInstant
                            .atZone(ZoneOffset.UTC)
                            .format(formatter)
                        val createdAtKst = recordInstant
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(formatter)

                        Log.e(
                            "TEST",
                            "이벤트 전송 전 userId : $userId eventCode : $eventTypeCode, " +
                                "createdAt(UTC) : $createdAtUtc, createdAt(KST) : $createdAtKst, " +
                                "createdAtLong : $uploadRecordTimeMillis, originalCreatedAtLong : $recordTimeMillis content : $content"
                        )

                        if (
                            category == RecordCategory.BLOOD_SUGAR &&
                            isVipUser &&
                            useCalibration &&
                            calibrationGlucose != null
                        ) {
                            try {
                                localDbRepository?.dataDao()?.insertCalibration(
                                    UserCalibration(
                                        userId = userId,
                                        glucoseValue = calibrationGlucose,
                                        createdAt = createdAtKst,
                                        createdAtLong = uploadRecordTimeMillis
                                    )
                                )
                                Log.d("EVENT", "채혈 보정값 Room 저장 성공")
                            } catch (dbException: Exception) {
                                Log.e(
                                    "EVENT",
                                    "채혈 보정값 Room 저장 실패 : ${dbException.message}",
                                    dbException
                                )
                            }
                        }

                        val upload = tokenRetrofit.uploadEvent(
                            RequestEventData(
                                userId = userId,
                                createdAt = createdAtUtc,
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
                            Toast.makeText(context, context.getString(R.string.toast_network_error2), Toast.LENGTH_SHORT).show()
                            Log.e("EVENT", "이벤트 업로드 실패")
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            // 기록 시각 줄과 저장 버튼이 붙어 있으면 저장을 누르려다 연필을 건드린다.
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(stringResource(R.string.record_detail_screen_save_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    onEditTime: () -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    intensity: String,
    onIntensityChange: (String) -> Unit
) {

    val intensityOptions = listOf(
        IntensityOption("가벼움", R.string.exercise_intensity_low),
        IntensityOption("보통", R.string.exercise_intensity_medium),
        IntensityOption("격렬함", R.string.exercise_intensity_high)
    )
    val exerciseTextSelectionColors = TextSelectionColors(
        handleColor = Color.Black,
        backgroundColor = Color.Black.copy(alpha = 0.3f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides exerciseTextSelectionColors) {
        OutlinedTextField(
            value = type, onValueChange = onTypeChange,
            label = { Text(stringResource(R.string.record_detail_exercise_description_1)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.Black
            )
        )
        OutlinedTextField(
            value = time, onValueChange = onTimeChange,
            label = { Text(stringResource(R.string.record_detail_exercise_description_2)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.Black
            )
        )
    }
    Text(stringResource(R.string.record_detail_exercise_description_sub_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        intensityOptions.forEach { option ->
            RecordFilterChip(
                selected = intensity == option.value,
                onClick = { onIntensityChange(option.value) },
                label = stringResource(option.labelResId)
            )
        }
    }
    RecordTimeRow(text = recordTimeText, onEditClick = onEditTime)
}

@Composable
fun MealInputForm(
    recordTimeText: String,
    onEditTime: () -> Unit,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    mealContent: String,
    onMealContentChange: (String) -> Unit,
    photoFile: File?,
    onPhotoCaptured: (File?) -> Unit
) {

    val mealTextSelectionColors = TextSelectionColors(
        handleColor = Color.Black,
        backgroundColor = Color.Black.copy(alpha = 0.3f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides mealTextSelectionColors) {
        OutlinedTextField(
            value = mealName, onValueChange = onMealNameChange,
            label = { Text(stringResource(R.string.record_detail_meal_description_1)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.Black
            )
        )
        OutlinedTextField(
            value = mealContent, onValueChange = onMealContentChange,
            label = { Text(stringResource(R.string.record_detail_meal_description_2)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.Black
            )
        )
    }

    MealPhotoField(photoFile = photoFile, onPhotoCaptured = onPhotoCaptured)

    RecordTimeRow(text = recordTimeText, onEditClick = onEditTime)
}

/**
 * 식사 사진 촬영 영역.
 *
 * 찍기 전에는 점선 상자를, 찍은 뒤에는 사진과 다시 촬영·삭제 버튼을 보여 준다.
 * 사진은 기기 안에만 남는다. 서버 기록 API 에는 사진을 담을 자리가 없다.
 */
@Composable
private fun MealPhotoField(
    photoFile: File?,
    onPhotoCaptured: (File?) -> Unit
) {
    val context = LocalContext.current
    // 촬영 시점에는 최종 기록 시각을 알 수 없어 임시 파일에 먼저 담는다.
    val tempFile = remember { MealPhotoStore.tempFile(context) }
    val tempUri = remember(tempFile) { MealPhotoStore.uriFor(context, tempFile) }

    // 같은 파일에 덮어써도 화면이 바뀌도록 버전을 하나 올려 준다.
    var version by remember { mutableStateOf(0) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempFile.exists() && tempFile.length() > 0) {
            version++
            onPhotoCaptured(tempFile)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.record_detail_meal_photo_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            takePicture.launch(tempUri)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.record_detail_meal_photo_permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 앨범은 사진 선택기가 고른 한 장만 넘겨 주는 구조라 저장소 권한을 묻지 않아도 된다.
    // 안드로이드 13 미만에서는 androidx 가 대신할 화면을 띄운다.
    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        if (MealPhotoStore.copyInto(context, uri, tempFile)) {
            version++
            onPhotoCaptured(tempFile)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.record_detail_meal_photo_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 어디서 가져올지 먼저 묻는다. 시스템 선택기에 카메라와 갤러리를 섞어 넣는 방법도
    // 있지만 기기마다 목록이 달라져, 앱에서 두 갈래만 내보이는 편이 한결같다.
    var showSourceChooser by remember { mutableStateOf(false) }

    fun capture() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) takePicture.launch(tempUri) else requestCamera.launch(Manifest.permission.CAMERA)
    }

    if (showSourceChooser) {
        MealPhotoSourceDialog(
            onDismiss = { showSourceChooser = false },
            onCamera = {
                showSourceChooser = false
                capture()
            },
            onGallery = {
                showSourceChooser = false
                pickPhoto.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.record_detail_meal_photo),
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (photoFile == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable { showSourceChooser = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.record_detail_meal_photo),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            val bitmap = remember(photoFile.path, version) {
                runCatching { BitmapFactory.decodeFile(photoFile.path)?.asImageBitmap() }.getOrNull()
            }
            if (bitmap != null) {
                // 잘라내지 않고 원본 비율 그대로 보여 준다. 무엇을 먹었는지 확인하는 사진이라
                // 가장자리가 잘리면 쓸모가 준다. 세로로 길어지면 화면이 스크롤된다.
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                TextButton(onClick = { showSourceChooser = true }) {
                    Text(stringResource(R.string.record_detail_meal_photo_retake), color = PrimaryOrange)
                }
                TextButton(onClick = {
                    MealPhotoStore.clearTemp(context)
                    onPhotoCaptured(null)
                }) {
                    Text(stringResource(R.string.record_detail_meal_photo_remove), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BloodSugarInputForm(
    recordTimeText: String,
    onEditTime: () -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    useCalibration: Boolean,
    onUseCalibrationChange: (Boolean) -> Unit,
    showCalibrationOption: Boolean
) {

    val bloodSugarTextSelectionColors = TextSelectionColors(
        handleColor = Color.Black,
        backgroundColor = Color.Black.copy(alpha = 0.3f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides bloodSugarTextSelectionColors) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            label = { Text(stringResource(R.string.record_detail_bg_label)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.Black
            )
        )
    }

    RecordTimeRow(text = recordTimeText, onEditClick = onEditTime)

    if (showCalibrationOption) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUseCalibrationChange(!useCalibration) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = useCalibration,
                onCheckedChange = onUseCalibrationChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryOrange,
                    checkmarkColor = Color.White
                )
            )
            Text(stringResource(R.string.record_detail_use_calibration), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InsulinInputForm(
    recordTimeText: String,
    onEditTime: () -> Unit,
    dose: String,
    onDoseChange: (String) -> Unit,
    insulinType: String,
    onInsulinTypeChange: (String) -> Unit
) {
    val firstRowOptions = listOf(
        InsulinTypeOption("초속효성", R.string.insulin_type_rapid),
        InsulinTypeOption("속효성", R.string.insulin_type_short),
        InsulinTypeOption("지속형", R.string.insulin_type_long)
    )
    val secondRowOptions = listOf(
        InsulinTypeOption("혼합형", R.string.insulin_type_premixed),
        InsulinTypeOption("중간형", R.string.insulin_type_nph)
    )

    Text(stringResource(R.string.record_detail_insulin_type), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    // 2줄로 칩 배치 (가상의 그리드 느낌)
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            firstRowOptions.forEach { option ->
                RecordFilterChip(
                    selected = insulinType == option.value,
                    onClick = { onInsulinTypeChange(option.value) },
                    label = stringResource(option.labelResId)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            secondRowOptions.forEach { option ->
                RecordFilterChip(
                    selected = insulinType == option.value,
                    onClick = { onInsulinTypeChange(option.value) },
                    label = stringResource(option.labelResId)
                )
            }
        }
    }

    val insulinTextSelectionColors = TextSelectionColors(
        handleColor = Color.Black,
        backgroundColor = Color.Black.copy(alpha = 0.3f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides insulinTextSelectionColors) {
        OutlinedTextField(
            value = dose, onValueChange = onDoseChange,
            label = { Text(stringResource(R.string.record_detail_insulin)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.Black
            )
        )
    }

    RecordTimeRow(text = recordTimeText, onEditClick = onEditTime)
}


/**
 * 사진을 어디서 가져올지 고르는 대화상자.
 *
 * 앱 안에서 두 갈래만 내보인다. 시스템 선택기에 카메라 앱을 끼워 넣는 방법도 있지만,
 * 제조사와 설치된 앱에 따라 목록이 제각각이라 같은 앱을 쓰는 사람마다 다른 화면을 본다.
 */
@Composable
private fun MealPhotoSourceDialog(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.record_detail_meal_photo),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCamera,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        stringResource(R.string.record_detail_meal_photo_camera),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onGallery,
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, PrimaryOrange),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        stringResource(R.string.record_detail_meal_photo_gallery),
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        }
    }
}
