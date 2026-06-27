package kr.co.uxn.agms_p_a2rt.ui.components.main.statistics

import PrimaryOrange
import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.Paint
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.FadingEdges
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.rememberMarker
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.UserGlucose
import kr.co.uxn.agms_p_a2rt.ui.components.main.home.DemoGlucoseConfig
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

// 테마 컬러
val BackgroundGray = Color(0xFFEBEBEB)
val PrimaryOrange = Color(0xFFFCA937)
val AlertGreen = Color(0xFF4CAF50)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF888888)
val LineDashedBlue = Color(0xFF64B5F6)
val LineDashedOrange = Color(0xFFFFB74D)
private val AnalysisGlucoseLineColor = Color(0xFFD0D0D0)
private val AnalysisGlucoseNormalColor = Color(0xFF65B66F)
private val AnalysisGlucoseHighColor = Color(0xFFFFA12B)
private val AnalysisGlucoseLowColor = Color(0xFF8FAEFF)
private const val MissingGlucoseValue = -10.0
private const val DailyBucketMinutes = 5L

private class AnalysisDashedLineDecoration(
    private val yValue: Double,
    private val color: Color
) : Decoration {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(12f, 10f), 0f)
    }

    override fun drawOverLayers(context: CartesianDrawingContext) {
        val yRange = context.ranges.getYRange(null)
        if (yValue < yRange.minY || yValue > yRange.maxY) return

        val canvasY = context.layerBounds.bottom -
            ((yValue - yRange.minY) / yRange.length).toFloat() *
            context.layerBounds.height()
        paint.color = color.toArgb()
        paint.strokeWidth = 2.dp.value * context.density
        context.canvas.drawLine(
            context.layerBounds.left,
            canvasY,
            context.layerBounds.right,
            canvasY,
            paint
        )
    }
}

private fun fillDailyGlucoseBuckets(
    userId: Int,
    source: List<UserGlucose>,
    dayStartMillis: Long,
    fillEndMillis: Long,
    zoneId: ZoneId
): List<UserGlucose> {
    val minuteMillis = 60 * 1000L
    val intervalMillis = DailyBucketMinutes * minuteMillis
    val startBucket = (dayStartMillis + intervalMillis - 1) / intervalMillis
    val endMinuteMillis = fillEndMillis - (fillEndMillis % minuteMillis)
    val endBucket = endMinuteMillis / intervalMillis
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dataByBucket = source
        .groupBy { it.createdAtLong / intervalMillis }
        .mapValues { (_, values) -> values.maxBy { it.createdAtLong } }
    val result = mutableListOf<UserGlucose>()

    fun missingData(time: Long) = UserGlucose(
        userId = userId,
        glucose = MissingGlucoseValue,
        weo1 = MissingGlucoseValue,
        weo2 = MissingGlucoseValue,
        createdAt = Instant.ofEpochMilli(time).atZone(zoneId).format(formatter),
        createdAtLong = time
    )

    val firstBucketTime = startBucket * intervalMillis
    if (firstBucketTime > dayStartMillis) {
        result += missingData(dayStartMillis)
    }

    for (bucket in startBucket..endBucket) {
        val bucketTime = bucket * intervalMillis
        val existingData = dataByBucket[bucket]
        result += if (existingData != null) {
            existingData.copy(createdAtLong = bucketTime)
        } else {
            missingData(bucketTime)
        }
    }

    if (result.lastOrNull()?.createdAtLong != endMinuteMillis) {
        result += missingData(endMinuteMillis)
    }
    return result
}

private fun createAnalysisDemoGlucoseData(
    userId: Int,
    selectedDate: LocalDate,
    currentTimeMillis: Long,
    zoneId: ZoneId
): List<UserGlucose> {
    val today = LocalDate.now(zoneId)
    val firstDemoDate = today.minusDays(DemoGlucoseConfig.ANALYSIS_HISTORY_DAYS - 1)
    if (selectedDate < firstDemoDate || selectedDate > today) return emptyList()

    val historyStartMillis = firstDemoDate
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val selectedStartMillis = selectedDate
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val selectedEndMillis = selectedDate
        .plusDays(1)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val intervalMillis = DemoGlucoseConfig.ANALYSIS_INTERVAL_MINUTES * 60 * 1000L
    val historyEndMillis = currentTimeMillis - (currentTimeMillis % intervalMillis)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
    val random = Random(20260627)
    val result = mutableListOf<UserGlucose>()
    var glucose = 115.0
    var targetGlucose = 115.0
    var pointIndex = 0L
    var time = historyStartMillis

    while (time <= historyEndMillis) {
        if (pointIndex % 12L == 0L) {
            targetGlucose = random.nextDouble(20.0, 225.0)
        }
        val smallVariation = random.nextDouble(-1.5, 1.5)
        glucose = (glucose + (targetGlucose - glucose) * 0.1 + smallVariation)
            .coerceIn(0.0, 240.0)

        if (time >= selectedStartMillis && time < selectedEndMillis) {
            result += UserGlucose(
                userId = userId,
                glucose = glucose,
                weo1 = 0.0,
                weo2 = 0.0,
                createdAt = Instant.ofEpochMilli(time).atZone(zoneId).format(formatter),
                createdAtLong = time
            )
        }

        pointIndex++
        time += intervalMillis
    }
    return result
}

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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var dailyGlucoseData by remember { mutableStateOf<List<UserGlucose>>(emptyList()) }
    var isChartLoading by remember { mutableStateOf(true) }
    var selectedDayStartMillis by remember { mutableLongStateOf(0L) }
    val formattedDate = remember(selectedDate) {
        selectedDate.format(
            DateTimeFormatter.ofPattern("yy.MM.dd(E)", Locale.getDefault())
        )
    }
    val actualGlucoseValues = remember(dailyGlucoseData) {
        dailyGlucoseData
            .map { it.glucose }
            .filter { it != MissingGlucoseValue }
    }
    val averageGlucose = remember(actualGlucoseValues) {
        actualGlucoseValues
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.roundToInt()
    }
    val highestGlucose = remember(actualGlucoseValues) {
        actualGlucoseValues.maxOrNull()?.roundToInt()
    }
    val lowestGlucose = remember(actualGlucoseValues) {
        actualGlucoseValues.minOrNull()?.roundToInt()
    }

    LaunchedEffect(selectedDate) {
        isChartLoading = true
        dailyGlucoseData = emptyList()
        val zoneId = ZoneId.systemDefault()
        val dayStartMillis = selectedDate
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val nextDayStartMillis = selectedDate
            .plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zoneId)

        val chartData = withContext(Dispatchers.IO) {
            val userId = DataStoreManager.getUserId().first() ?: -1
            val source = if (DemoGlucoseConfig.ENABLED) {
                createAnalysisDemoGlucoseData(
                    userId = userId,
                    selectedDate = selectedDate,
                    currentTimeMillis = now,
                    zoneId = zoneId
                )
            } else {
                AppDatabase.getInstance(context)
                    ?.dataDao()
                    ?.getGlucoseListBetween(
                        userId = userId,
                        startTime = dayStartMillis,
                        endTime = nextDayStartMillis
                    )
                    .orEmpty()
            }
                .filter { selectedDate != today || it.createdAtLong <= now }

            if (source.isEmpty()) {
                emptyList()
            } else {
                val fillEndMillis = if (selectedDate == today) {
                    minOf(now, nextDayStartMillis - 1)
                } else {
                    source.last().createdAtLong
                }
                fillDailyGlucoseBuckets(
                    userId = userId,
                    source = source,
                    dayStartMillis = dayStartMillis,
                    fillEndMillis = fillEndMillis,
                    zoneId = zoneId
                )
            }
        }

        selectedDayStartMillis = dayStartMillis
        dailyGlucoseData = chartData
        isChartLoading = false
    }

    if (showDatePicker) {
        val selectedDateUtcMillis = remember(selectedDate) {
            selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateUtcMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                    }
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
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "이전 날짜",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { selectedDate = selectedDate.minusDays(1) }
                )
                Text(formattedDate, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "다음 날짜",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { selectedDate = selectedDate.plusDays(1) }
                )
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
                Image(
                    painter = painterResource(id = R.drawable.calendar_icon),
                    contentDescription = "Calendar"
                )
//                Icon(Icons.Default.DateRange, contentDescription = "Calendar")
            }
        }

        // 메인 차트 영역 (목업)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            Box(modifier = Modifier.padding(4.dp).fillMaxSize()) {
                when {
                    isChartLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryOrange
                    )
                    dailyGlucoseData.isEmpty() -> Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "해당 날짜에는 데이터가 없습니다.",
                        color = TextGray,
                        fontSize = 15.sp
                    )
                    else -> DailyGlucoseChart(
                        data = dailyGlucoseData,
                        dayStartMillis = selectedDayStartMillis,
                        zoneId = ZoneId.systemDefault(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                        Text(
                            text = averageGlucose?.toString() ?: "--",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertGreen
                        )
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
                            Text(
                                text = highestGlucose?.toString() ?: "--",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    Column {
                        Text("최저", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = lowestGlucose?.toString() ?: "--",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
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

@SuppressLint("RestrictedApi")
@Composable
private fun DailyGlucoseChart(
    data: List<UserGlucose>,
    dayStartMillis: Long,
    zoneId: ZoneId,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val xValues = remember(data, dayStartMillis) {
        data.map { ((it.createdAtLong - dayStartMillis) / 60_000L).toDouble() }
    }
    val yValues = remember(data) { data.map { it.glucose } }
    val firstX = xValues.first()
    val lastX = xValues.last()
    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth
    )
    val rangeProvider = remember {
        CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = 300.0)
    }

    LaunchedEffect(xValues, yValues) {
        modelProducer.runTransaction {
            lineSeries { series(xValues, yValues) }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
        val timeMillis = dayStartMillis + (value * 60_000.0).toLong()
        Instant.ofEpochMilli(timeMillis)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }
    val yAxisFormatter = CartesianValueFormatter.decimal(DecimalFormat("#"))
    val markerValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        val point = targets
            .filterIsInstance<LineCartesianLayerMarkerTarget>()
            .firstOrNull()
            ?.points
            ?.firstOrNull()
            ?: return@ValueFormatter ""
        val timeMillis = dayStartMillis + (point.entry.x * 60_000.0).toLong()
        val timeText = Instant.ofEpochMilli(timeMillis)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
        val valueText = DecimalFormat("#").format(point.entry.y)
        "$timeText, $valueText mg/dL"
    }
    val defaultMarker = rememberMarker(markerValueFormatter)
    val filteredMarker = remember(defaultMarker) {
        object : CartesianMarker by defaultMarker {
            private fun hasVisibleValue(targets: List<CartesianMarker.Target>): Boolean {
                val points = targets
                    .filterIsInstance<LineCartesianLayerMarkerTarget>()
                    .flatMap { it.points }
                return points.isEmpty() ||
                    points.any { it.entry.y != MissingGlucoseValue }
            }

            override fun drawUnderLayers(
                context: CartesianDrawingContext,
                targets: List<CartesianMarker.Target>
            ) {
                if (hasVisibleValue(targets)) {
                    defaultMarker.drawUnderLayers(context, targets)
                }
            }

            override fun drawOverLayers(
                context: CartesianDrawingContext,
                targets: List<CartesianMarker.Target>
            ) {
                if (hasVisibleValue(targets)) {
                    defaultMarker.drawOverLayers(context, targets)
                }
            }
        }
    }

    val hiddenPoint = LineCartesianLayer.point(
        rememberShapeComponent(
            fill = fill(Color.Transparent),
            shape = CorneredShape.Pill
        ),
        size = 1.dp
    )
    val normalPoint = LineCartesianLayer.point(
        rememberShapeComponent(
            fill = fill(AnalysisGlucoseNormalColor),
            shape = CorneredShape.Pill
        ),
        size = 4.dp
    )
    val highPoint = LineCartesianLayer.point(
        rememberShapeComponent(
            fill = fill(AnalysisGlucoseHighColor),
            shape = CorneredShape.Pill
        ),
        size = 4.dp
    )
    val lowPoint = LineCartesianLayer.point(
        rememberShapeComponent(
            fill = fill(AnalysisGlucoseLowColor),
            shape = CorneredShape.Pill
        ),
        size = 4.dp
    )
    val pointProvider = remember(hiddenPoint, normalPoint, highPoint, lowPoint) {
        object : LineCartesianLayer.PointProvider {
            override fun getPoint(
                entry: LineCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineCartesianLayer.Point {
                return when {
                    entry.y == MissingGlucoseValue -> hiddenPoint
                    entry.y > 180.0 -> highPoint
                    entry.y < 80.0 -> lowPoint
                    else -> normalPoint
                }
            }

            override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point {
                return normalPoint
            }
        }
    }
    val itemPlacer = remember(firstX, lastX) {
        object : HorizontalAxis.ItemPlacer {
            override fun getLabelValues(
                context: CartesianDrawingContext,
                visibleXRange: ClosedFloatingPointRange<Double>,
                fullXRange: ClosedFloatingPointRange<Double>,
                maxLabelWidth: Float
            ): List<Double> {
                return listOf(firstX, (firstX + lastX) / 2.0, lastX)
            }

            override fun getStartLayerMargin(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                tickThickness: Float,
                maxLabelWidth: Float
            ): Float = 32f

            override fun getEndLayerMargin(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                tickThickness: Float,
                maxLabelWidth: Float
            ): Float = 32f

            override fun getWidthMeasurementLabelValues(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                fullXRange: ClosedFloatingPointRange<Double>
            ): List<Double> = listOf(firstX, (firstX + lastX) / 2.0, lastX)

            override fun getHeightMeasurementLabelValues(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                fullXRange: ClosedFloatingPointRange<Double>,
                maxLabelWidth: Float
            ): List<Double> = listOf(firstX, (firstX + lastX) / 2.0, lastX)
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(
                            fill(AnalysisGlucoseLineColor)
                        ),
                        stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 1f),
                        pointProvider = pointProvider,
                        pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.8f)
                    )
                ),
                rangeProvider = rangeProvider
            ),
            endAxis = VerticalAxis.rememberEnd(
                valueFormatter = yAxisFormatter,
                label = rememberAxisLabelComponent(color = Color.Black),
                itemPlacer = VerticalAxis.ItemPlacer.count({ 6 })
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisFormatter,
                label = rememberAxisLabelComponent(color = Color.Black),
                itemPlacer = itemPlacer,
                labelRotationDegrees = 90f
            ),
            marker = filteredMarker,
            fadingEdges = FadingEdges(
                startWidthDp = 0f,
                endWidthDp = 0f,
                visibilityThresholdDp = 15f
            ),
            decorations = listOf(
                AnalysisDashedLineDecoration(80.0, AnalysisGlucoseLowColor),
                AnalysisDashedLineDecoration(180.0, AnalysisGlucoseHighColor)
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        scrollState = scrollState,
        zoomState = rememberVicoZoomState(
            zoomEnabled = true,
            initialZoom = Zoom.Content
        )
    )
}

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
