package kr.co.uxn.agms_p_a2rt.ui.components.main.statistics

import PrimaryOrange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.uxn.agms_p_a2rt.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// 테마 컬러
val BackgroundGray = Color(0xFFEBEBEB)
val PrimaryOrange = Color(0xFFFCA937)
val AlertGreen = Color(0xFF4CAF50)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF888888)
val LineDashedBlue = Color(0xFF64B5F6)
val LineDashedOrange = Color(0xFFFFB74D)

@Composable
fun AnalysisScreen(
    paddingValues: PaddingValues,
    onBloodSugarRecordClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("일일기록", "상세")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BackgroundGray)
    ) {
        // 상단 탭 (일일기록 / 상세)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = TextDark,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        color = TextDark, // 검은색 두꺼운 인디케이터
                        height = 4.dp
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == index) TextDark else TextGray
                    )
                }
            }
        }

        // 탭 상태에 따른 화면 분기
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> DailyRecordContent(onBloodSugarRecordClick)
                1 -> DetailRecordContent()
            }
        }
    }
}

// ==========================================
// [첫 번째 탭] 일일기록 화면
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRecordContent(onBloodSugarRecordClick: () -> Unit) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val formattedDate = remember(selectedDateMillis) {
        val formatter = SimpleDateFormat("yy.MM.dd (E)", Locale.KOREAN)
        // Material3 DatePicker는 UTC 기준으로 동작하므로 TimeZone을 UTC로 맞춰줍니다.
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        formatter.format(Date(selectedDateMillis))
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // 확인 클릭 시 선택한 날짜 밀리초를 상태에 반영
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("확인", color = PrimaryOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 날짜 선택기 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 날짜 이동
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", modifier = Modifier.size(20.dp))
                Text("26.06.15 (금)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(10.dp))
            // 달력 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .clickable { showDatePicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
            }
        }

        // 메인 차트 영역 (목업)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
//                .height(250.dp)
                .weight(0.6f)
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                // 임시 차트 UI (캔버스를 활용한 시각적 표현)
                DailyChartMockup()
            }
        }

        // 요약 정보 카드 (평균 / 최고, 최저)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
            ,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 평균 카드
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("평균", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("110", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AlertGreen)
                        Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }

            // 최고/최저 카드
            Card(
                modifier = Modifier.weight(1.5f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("최고", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("110", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    Column {
                        Text("최저", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("110", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                }
            }
        }

        // 하단 버튼 영역
        Button(
            onClick = onBloodSugarRecordClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f),
            shape = RoundedCornerShape(25.dp)
        ) {
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

//        Button(
//            onClick = onNavigateToSelect,
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .height(50.dp),
//            shape = RoundedCornerShape(25.dp)
//        ) {
//            Text("⊕ 기록하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
//        }
    }
}

// ==========================================
// [두 번째 탭] 상세 화면
// ==========================================
@Composable
fun DetailRecordContent() {
    var selectedFilter by remember { mutableStateOf("일") }
    val filters = listOf("일", "주", "월")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 상단 헤더 (평균혈당 타이틀 및 토글 버튼)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("평균혈당", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    // 커스텀 세그먼트 버튼 (일/주/월)
                    Row(
                        modifier = Modifier
                            .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                            .background(Color.White, RoundedCornerShape(20.dp))
                    ) {
                        filters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) Color.Gray else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) Color.White else TextDark,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 상세 차트 영역 (목업)
                DetailChartMockup()
            }
        }
    }
}

// --- 차트 UI 목업용 컴포저블 (Canvas 이용) ---

@Composable
fun DailyChartMockup() {
    // 실제 차트 라이브러리(MPAndroidChart, Vico 등) 대체용 단순 시각적 캔버스
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        // 가로선 및 우측 Y축 라벨
        val yLabels = listOf(300, 250, 200, 150, 100, 60)
        yLabels.forEachIndexed { index, label ->
            val yPos = height * (index / 5f)
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, yPos),
                end = Offset(width - 80f, yPos),
                strokeWidth = 2f
            )
        }

        // 점선 타겟 범위
        drawLine(color = LineDashedOrange, start = Offset(0f, height * 0.4f), end = Offset(width - 80f, height * 0.4f), strokeWidth = 3f, pathEffect = dashEffect)
        drawLine(color = LineDashedBlue, start = Offset(0f, height * 0.8f), end = Offset(width - 80f, height * 0.8f), strokeWidth = 3f, pathEffect = dashEffect)

        // 임시 차트 곡선 그리기
        val path = Path().apply {
            moveTo(10f, height * 0.7f)
            quadraticBezierTo(50f, height * 0.3f, 100f, height * 0.4f)
            quadraticBezierTo(150f, height * 0.8f, 200f, height * 0.7f)
            quadraticBezierTo(300f, height * 0.5f, 400f, height * 0.7f)
            quadraticBezierTo(500f, height * 0.2f, 600f, height * 0.3f)
            lineTo(width - 100f, height * 0.6f)
        }
        drawPath(path = path, color = Color.DarkGray, style = Stroke(width = 5f))
    }
}

@Composable
fun DetailChartMockup() {
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val width = size.width
        val height = size.height

        val points = listOf(
            Offset(0f, height * 0.4f),
            Offset(width * 0.16f, height * 0.5f),
            Offset(width * 0.33f, height * 0.4f),
            Offset(width * 0.5f, height * 0.45f),
            Offset(width * 0.66f, height * 0.6f),
            Offset(width * 0.83f, height * 0.35f),
            Offset(width, height * 0.55f)
        )

        // 선 그리기
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path = path, color = Color.Gray, style = Stroke(width = 6f))

        // 점 그리기
        points.forEach { point ->
            drawCircle(color = Color.LightGray, radius = 12f, center = point)
        }
    }
}
