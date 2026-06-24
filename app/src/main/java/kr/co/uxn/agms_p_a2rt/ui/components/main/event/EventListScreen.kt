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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.uxn.agms_p_a2rt.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "list",
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // 첫 번째 화면: 기록 리스트 화면
        composable("list") {
            RecordListScreen(
                onNavigateToSelect = { navController.navigate("select") }
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
fun RecordListScreen(onNavigateToSelect: () -> Unit) {
    var selectedTab by remember { mutableStateOf(RecordCategory.ALL) }

    // 선택된 탭에 따라 리스트 필터링
    val filteredRecords = if (selectedTab == RecordCategory.ALL) {
        dummyRecords
    } else {
        dummyRecords.filter { it.category == selectedTab }
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
            Text("⊕ 기록하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    val recordTimeText = remember { formatRecordTime(System.currentTimeMillis()) }

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
                RecordCategory.EXERCISE -> ExerciseInputForm(recordTimeText)
                RecordCategory.MEAL -> MealInputForm(recordTimeText)
                RecordCategory.BLOOD_SUGAR -> BloodSugarInputForm(recordTimeText)
                RecordCategory.INSULIN -> InsulinInputForm(recordTimeText)
                else -> {}
            }
        }

        // 하단 저장 버튼
        Button(
            onClick = { /* 저장 로직 */ },
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
fun ExerciseInputForm(recordTimeText: String) {
    var type by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf("보통") }

    OutlinedTextField(
        value = type, onValueChange = { type = it },
        label = { Text("운동 종류 (예: 조깅, 수영)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    OutlinedTextField(
        value = time, onValueChange = { time = it },
        label = { Text("운동 시간 (분)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text("강도", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        listOf("가벼움", "보통", "격렬함").forEach { level ->
            RecordFilterChip(
                selected = intensity == level,
                onClick = { intensity = level },
                label = level
            )
        }
    }
    Text(recordTimeText, color = Color.Gray)
}

@Composable
fun MealInputForm(recordTimeText: String) {
    var mealName by remember { mutableStateOf("") }
    var mealContent by remember { mutableStateOf("") }

    OutlinedTextField(
        value = mealName, onValueChange = { mealName = it },
        label = { Text("식사 이름 (예: 아침)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    OutlinedTextField(
        value = mealContent, onValueChange = { mealContent = it },
        label = { Text("식사 내용 (예: 밥, 된장국)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text(recordTimeText, color = Color.Gray)
}

@Composable
fun BloodSugarInputForm(recordTimeText: String) {
    var value by remember { mutableStateOf("") }

    OutlinedTextField(
        value = value, onValueChange = { value = it },
        label = { Text("혈당값 (mg/dL)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text(recordTimeText, color = Color.Gray)
}

@Composable
fun InsulinInputForm(recordTimeText: String) {
    var dose by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf("초속효성") }

    Text("인슐린 종류", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    // 2줄로 칩 배치 (가상의 그리드 느낌)
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("초속효성", "속효성", "지속형").forEach { level ->
                RecordFilterChip(
                    selected = insulinType == level,
                    onClick = { insulinType = level },
                    label = level
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("혼합형", "중간형").forEach { level ->
                RecordFilterChip(
                    selected = insulinType == level,
                    onClick = { insulinType = level },
                    label = level
                )
            }
        }
    }

    OutlinedTextField(
        value = dose, onValueChange = { dose = it },
        label = { Text("투여량 (Unit)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
    Text(recordTimeText, color = Color.Gray)
}
