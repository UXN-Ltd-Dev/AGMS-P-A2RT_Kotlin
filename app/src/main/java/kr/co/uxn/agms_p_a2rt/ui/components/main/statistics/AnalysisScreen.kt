package kr.co.uxn.agms_p_a2rt.ui.components.main.statistics

import PrimaryOrange
import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
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
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
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
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import kotlin.math.abs
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
private val AnalysisGlucoseNormalColor = Color(0xFF65B66F)
private val AnalysisGlucoseHighColor = Color(0xFFFFA12B)
private val AnalysisGlucoseLowColor = Color(0xFF8FAEFF)
private val AnalysisGlucoseTargetRangeColor = Color(0x2E9CCC65)
private val AnalysisEventTextColor = Color(0xFFC74B3C)
private const val MissingGlucoseValue = -10.0
private const val DailyBucketMinutes = 5L

private data class AnalysisEvent(
    val timeMillis: Long,
    val title: String
)

private data class AnalysisEventPoint(
    val x: Double,
    val y: Double,
    val title: String
)

private data class AverageGlucosePoint(
    val primaryLabel: String,
    val secondaryLabel: String,
    val average: Double?
)

private data class GlucoseAveragePeriod(
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val primaryLabel: String,
    val secondaryLabel: String
)

private enum class DetailAverageUnit {
    DAY,
    WEEK,
    MONTH
}

private fun analysisEventTitle(eventTypeCode: Int): String = when (eventTypeCode) {
    1401 -> "식사"
    1402 -> "운동"
    1403 -> "채혈"
    1404 -> "인슐린"
    else -> "기록"
}

private fun parseServerEventTime(createdAt: String): Long? = runCatching {
    LocalDateTime.parse(
        createdAt,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
        .atZone(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()

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

private class AnalysisTargetRangeDecoration(
    private val minYValue: Double,
    private val maxYValue: Double,
    color: Color
) : Decoration {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color.toArgb()
    }

    override fun drawUnderLayers(context: CartesianDrawingContext) {
        val yRange = context.ranges.getYRange(null)
        val visibleMin = minYValue.coerceAtLeast(yRange.minY)
        val visibleMax = maxYValue.coerceAtMost(yRange.maxY)
        if (visibleMin >= visibleMax) return

        fun canvasY(value: Double): Float =
            context.layerBounds.bottom -
                ((value - yRange.minY) / yRange.length).toFloat() *
                context.layerBounds.height()

        context.canvas.drawRect(
            context.layerBounds.left,
            canvasY(visibleMax),
            context.layerBounds.right,
            canvasY(visibleMin),
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

private fun buildDetailAveragePeriods(
    unit: DetailAverageUnit,
    today: LocalDate,
    zoneId: ZoneId,
    locale: Locale
): List<GlucoseAveragePeriod> {
    val dayFormatter = DateTimeFormatter.ofPattern("MM/dd", locale)

    return when (unit) {
        DetailAverageUnit.DAY -> {
            (6L downTo 0L).map { dayOffset ->
                val date = today.minusDays(dayOffset)
                GlucoseAveragePeriod(
                    startTimeMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    endTimeMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    primaryLabel = date.format(dayFormatter),
                    secondaryLabel = date.format(DateTimeFormatter.ofPattern("E", locale))
                )
            }
        }

        DetailAverageUnit.WEEK -> {
            val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))

            (6L downTo 0L).map { weekOffset ->
                val startDate = currentWeekStart.minusWeeks(weekOffset)
                val endDate = startDate.plusWeeks(1)
                GlucoseAveragePeriod(
                    startTimeMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    endTimeMillis = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    primaryLabel = startDate.format(dayFormatter),
                    secondaryLabel = "주"
                )
            }
        }

        DetailAverageUnit.MONTH -> {
            val currentMonth = YearMonth.from(today)
            (6L downTo 0L).map { monthOffset ->
                val month = currentMonth.minusMonths(monthOffset)
                val startDate = month.atDay(1)
                val endDate = month.plusMonths(1).atDay(1)
                GlucoseAveragePeriod(
                    startTimeMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    endTimeMillis = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    primaryLabel = month.format(DateTimeFormatter.ofPattern("yy/MM", locale)),
                    secondaryLabel = "월"
                )
            }
        }
    }
}

private fun createDetailDemoAverages(
    periods: List<GlucoseAveragePeriod>,
    currentTimeMillis: Long
): List<AverageGlucosePoint> {
    if (periods.isEmpty()) return emptyList()

    val sums = DoubleArray(periods.size)
    val counts = IntArray(periods.size)
    val random = Random(20260627)
    val intervalMillis = DemoGlucoseConfig.ANALYSIS_INTERVAL_MINUTES * 60_000L
    val generationEnd = minOf(periods.last().endTimeMillis, currentTimeMillis + 1L)
    var glucose = 115.0
    var targetGlucose = 115.0
    var pointIndex = 0L
    var periodIndex = 0
    var time = periods.first().startTimeMillis

    while (time < generationEnd && periodIndex < periods.size) {
        while (periodIndex < periods.lastIndex && time >= periods[periodIndex].endTimeMillis) {
            periodIndex++
        }

        if (pointIndex % 12L == 0L) {
            targetGlucose = random.nextDouble(20.0, 225.0)
        }
        glucose = (glucose +
            (targetGlucose - glucose) * 0.1 +
            random.nextDouble(-1.5, 1.5))
            .coerceIn(0.0, 240.0)

        val period = periods[periodIndex]
        if (time >= period.startTimeMillis && time < period.endTimeMillis) {
            sums[periodIndex] += glucose
            counts[periodIndex]++
        }

        pointIndex++
        time += intervalMillis
    }

    return periods.mapIndexed { index, period ->
        AverageGlucosePoint(
            primaryLabel = period.primaryLabel,
            secondaryLabel = period.secondaryLabel,
            average = counts[index].takeIf { it > 0 }?.let { sums[index] / it }
        )
    }
}

private fun aggregateRoomGlucose(
    periods: List<GlucoseAveragePeriod>,
    glucoseValues: List<UserGlucose>,
    currentTimeMillis: Long
): List<AverageGlucosePoint> {
    return periods.map { period ->
        val effectiveEnd = minOf(period.endTimeMillis, currentTimeMillis + 1L)
        val values = glucoseValues.asSequence()
            .filter {
                it.glucose >= 0.0 &&
                    it.createdAtLong >= period.startTimeMillis &&
                    it.createdAtLong < effectiveEnd
            }
            .map(UserGlucose::glucose)
            .toList()

        AverageGlucosePoint(
            primaryLabel = period.primaryLabel,
            secondaryLabel = period.secondaryLabel,
            average = values.takeIf { it.isNotEmpty() }?.average()
        )
    }
}

@Composable
fun AnalysisScreen(
    paddingValues: PaddingValues,
    onBloodSugarRecordClick: (Long) -> Unit
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
fun DailyRecordContent(onBloodSugarRecordClick: (Long) -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var dailyGlucoseData by remember { mutableStateOf<List<UserGlucose>>(emptyList()) }
    var dailyEvents by remember { mutableStateOf<List<AnalysisEvent>>(emptyList()) }
    var isChartLoading by remember { mutableStateOf(true) }
    var selectedDayStartMillis by remember { mutableLongStateOf(0L) }
    var selectedMarkerTimeMillis by remember(selectedDate) { mutableStateOf<Long?>(null) }
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
        dailyEvents = emptyList()
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

        val (chartData, events) = withContext(Dispatchers.IO) {
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

            val filledChartData = if (source.isEmpty()) {
                emptyList()
            } else {
                val fillEndMillis = if (selectedDate == today) {
                    nextDayStartMillis - 1
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

            val selectedDayEvents = runCatching {
                val response = tokenRetrofit.getEventList(userId)
                if (response.isSuccessful) {
                    response.body().orEmpty()
                        .mapNotNull { event ->
                            parseServerEventTime(event.createdAt)?.let { timeMillis ->
                                AnalysisEvent(
                                    timeMillis = timeMillis,
                                    title = analysisEventTitle(event.eventTypeCode)
                                )
                            }
                        }
                        .filter { it.timeMillis in dayStartMillis until nextDayStartMillis }
                } else {
                    emptyList()
                }
            }.getOrDefault(emptyList())

            filledChartData to selectedDayEvents
        }

        selectedDayStartMillis = dayStartMillis
        dailyGlucoseData = chartData
        dailyEvents = events
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
                        events = dailyEvents,
                        dayStartMillis = selectedDayStartMillis,
                        zoneId = ZoneId.systemDefault(),
                        onMarkerTimeSelected = { selectedMarkerTimeMillis = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 요약 정보 카드 (평균 / 최고, 최저)
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(0.2f)
//            ,
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // 평균 카드
//            Card(
//                modifier = Modifier.weight(1f),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text("평균", fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Row(verticalAlignment = Alignment.Bottom) {
//                        Text(
//                            text = averageGlucose?.toString() ?: "--",
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = AlertGreen
//                        )
//                        Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
//                    }
//                }
//            }
//
//            // 최고/최저 카드
//            Card(
//                modifier = Modifier.weight(1.5f),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth().padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Column {
//                        Text("최고", fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Row(verticalAlignment = Alignment.Bottom) {
//                            Text(
//                                text = highestGlucose?.toString() ?: "--",
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
//                        }
//                    }
//                    Column {
//                        Text("최저", fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Row(verticalAlignment = Alignment.Bottom) {
//                            Text(
//                                text = lowestGlucose?.toString() ?: "--",
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(" mg/dL", fontSize = 9.sp, modifier = Modifier.padding(bottom = 4.dp))
//                        }
//                    }
//                }
//            }
//        }

        // 하단 버튼 영역
        Button(
            onClick = {
                onBloodSugarRecordClick(
                    selectedMarkerTimeMillis ?: System.currentTimeMillis()
                )
            },
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
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    var averagePoints by remember { mutableStateOf<List<AverageGlucosePoint>>(emptyList()) }

    LaunchedEffect(selectedFilter) {
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zoneId)
        val unit = when (selectedFilter) {
            "주" -> DetailAverageUnit.WEEK
            "월" -> DetailAverageUnit.MONTH
            else -> DetailAverageUnit.DAY
        }
        val periods = buildDetailAveragePeriods(
            unit = unit,
            today = today,
            zoneId = zoneId,
            locale = Locale.getDefault()
        )

        averagePoints = withContext(Dispatchers.IO) {
            val userId = DataStoreManager.getUserId().first() ?: -1

            if (DemoGlucoseConfig.ENABLED) {
                createDetailDemoAverages(periods, now)
            } else {
                val glucoseValues = AppDatabase.getInstance(context)
                    ?.dataDao()
                    ?.getGlucoseListBetween(
                        userId = userId,
                        startTime = periods.first().startTimeMillis,
                        endTime = minOf(periods.last().endTimeMillis, now + 1L)
                    )
                    .orEmpty()
                aggregateRoomGlucose(periods, glucoseValues, now)
            }
        }
    }

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

                AverageGlucoseChart(averagePoints)
            }
        }
    }
}

// --- 차트 UI 목업용 컴포저블 (Canvas 이용) ---

@SuppressLint("RestrictedApi")
@Composable
private fun DailyGlucoseChart(
    data: List<UserGlucose>,
    events: List<AnalysisEvent>,
    dayStartMillis: Long,
    zoneId: ZoneId,
    onMarkerTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val xValues = remember(data, dayStartMillis) {
        data.map { ((it.createdAtLong - dayStartMillis) / 60_000L).toDouble() }
    }
    val yValues = remember(data) { data.map { it.glucose } }
    val eventPoints = remember(data, events, dayStartMillis) {
        val validGlucosePoints = data
            .mapIndexedNotNull { index, glucose ->
                glucose.glucose
                    .takeIf { it != MissingGlucoseValue }
                    ?.let { xValues[index] to it }
            }

        events.mapNotNull { event ->
            val eventMinute = (event.timeMillis - dayStartMillis) / 60_000.0
            val snappedX =
                (eventMinute / DailyBucketMinutes).roundToInt() * DailyBucketMinutes.toDouble()
            if (snappedX < xValues.first() || snappedX > xValues.last()) {
                return@mapNotNull null
            }

            val exactIndex = xValues.indices.minByOrNull { abs(xValues[it] - snappedX) }
            val exactY = exactIndex
                ?.let(yValues::get)
                ?.takeIf { it != MissingGlucoseValue }
            val eventY = exactY ?: validGlucosePoints
                .minByOrNull { (x, _) -> abs(x - snappedX) }
                ?.second
                ?: return@mapNotNull null

            AnalysisEventPoint(x = snappedX, y = eventY, title = event.title)
        }.distinctBy { it.x }
    }
    val firstX = xValues.first()
    val middleX = xValues[xValues.lastIndex / 2]
    val lastX = xValues.last()
    val scrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth
    )
    val rangeProvider = remember {
        CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = 250.0)
    }

    LaunchedEffect(xValues, yValues, eventPoints) {
        modelProducer.runTransaction {
            lineSeries {
                series(xValues, yValues)
                if (eventPoints.isNotEmpty()) {
                    series(
                        eventPoints.map { it.x },
                        eventPoints.map { it.y }
                    )
                }
            }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
        val timeMillis = dayStartMillis + (value * 60_000.0).toLong()
        Instant.ofEpochMilli(timeMillis)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }
    val yAxisFormatter = CartesianValueFormatter.decimal(DecimalFormat("#"))
    val emphasizedGlucoseSizePx = with(LocalDensity.current) {
        14.sp.toPx().roundToInt()
    }
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
        val eventTitle = eventPoints
            .firstOrNull { abs(it.x - point.entry.x) < 0.0001 }
            ?.title
        val markerText = SpannableString(
            buildString {
                append("$timeText, $valueText mg/dL")
                eventTitle?.let { append(", $it") }
            }
        )
        val valueStart = timeText.length + 2
        val valueEnd = valueStart + valueText.length
        markerText.setSpan(
            StyleSpan(Typeface.BOLD),
            valueStart,
            valueEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        markerText.setSpan(
            AbsoluteSizeSpan(emphasizedGlucoseSizePx),
            valueStart,
            valueEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        eventTitle?.let { title ->
            val titleStart = markerText.length - title.length
            markerText.setSpan(
                ForegroundColorSpan(AnalysisEventTextColor.toArgb()),
                titleStart,
                markerText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        markerText
    }
    val defaultMarker = rememberMarker(
        valueFormatter = markerValueFormatter,
        indicatorColorProvider = { sourceColor ->
            if (sourceColor == Color.Red) Color.Red else PrimaryOrange
        }
    )
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
    var persistentMarkerX by remember(dayStartMillis) { mutableStateOf<Double?>(null) }
    val markerVisibilityListener = remember(dayStartMillis) {
        object : CartesianMarkerVisibilityListener {
            private var latestValidX: Double? = null

            private fun updateLatestX(targets: List<CartesianMarker.Target>) {
                latestValidX = targets
                    .filterIsInstance<LineCartesianLayerMarkerTarget>()
                    .flatMap { it.points }
                    .firstOrNull { it.entry.y != MissingGlucoseValue }
                    ?.entry
                    ?.x
            }

            override fun onShown(
                marker: CartesianMarker,
                targets: List<CartesianMarker.Target>
            ) {
                persistentMarkerX = null
                latestValidX = null
                updateLatestX(targets)
            }

            override fun onUpdated(
                marker: CartesianMarker,
                targets: List<CartesianMarker.Target>
            ) {
                updateLatestX(targets)
            }

            override fun onHidden(marker: CartesianMarker) {
                latestValidX?.let { xValue ->
                    persistentMarkerX = xValue
                    onMarkerTimeSelected(
                        dayStartMillis + (xValue * 60_000.0).toLong()
                    )
                }
                latestValidX = null
            }
        }
    }
    val persistentMarkers:
        (CartesianChart.PersistentMarkerScope.(ExtraStore) -> Unit)? =
        persistentMarkerX?.let { xValue ->
            { filteredMarker at xValue }
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
//                    entry.y > 180.0 -> highPoint
//                    entry.y < 80.0 -> lowPoint
                    else -> normalPoint
                }
            }

            override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point {
                return normalPoint
            }
        }
    }
    val eventPoint = LineCartesianLayer.point(
        rememberShapeComponent(
            fill = fill(Color.Red),
            shape = CorneredShape.Pill
        ),
        size = 7.dp
    )
    val eventPointProvider = remember(eventPoint) {
        LineCartesianLayer.PointProvider.single(eventPoint)
    }
    val itemPlacer = remember(firstX, middleX, lastX) {
        object : HorizontalAxis.ItemPlacer {
            override fun getFirstLabelValue(
                context: CartesianMeasuringContext,
                maxLabelWidth: Float
            ): Double = firstX

            override fun getLastLabelValue(
                context: CartesianMeasuringContext,
                maxLabelWidth: Float
            ): Double = lastX

            override fun getLabelValues(
                context: CartesianDrawingContext,
                visibleXRange: ClosedFloatingPointRange<Double>,
                fullXRange: ClosedFloatingPointRange<Double>,
                maxLabelWidth: Float
            ): List<Double> {
                return listOf(firstX, middleX, lastX).distinct()
            }

            override fun getStartLayerMargin(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                tickThickness: Float,
                maxLabelWidth: Float
            ): Float = maxLabelWidth / 2f + 4.dp.value * context.density

            override fun getEndLayerMargin(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                tickThickness: Float,
                maxLabelWidth: Float
            ): Float = maxLabelWidth / 2f + 4.dp.value * context.density

            override fun getWidthMeasurementLabelValues(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                fullXRange: ClosedFloatingPointRange<Double>
            ): List<Double> = listOf(firstX, middleX, lastX).distinct()

            override fun getHeightMeasurementLabelValues(
                context: CartesianMeasuringContext,
                layerDimensions: CartesianLayerDimensions,
                fullXRange: ClosedFloatingPointRange<Double>,
                maxLabelWidth: Float
            ): List<Double> = listOf(firstX, middleX, lastX).distinct()
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(
                            fill(Color.Transparent)
                        ),
                        stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 0f),
                        pointProvider = pointProvider,
                        pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.8f)
                    ),
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(Color.Transparent)),
                        stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 0f),
                        pointProvider = eventPointProvider
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
                labelRotationDegrees = 0f
            ),
            marker = filteredMarker,
            markerVisibilityListener = markerVisibilityListener,
            persistentMarkers = persistentMarkers,
            fadingEdges = FadingEdges(
                startWidthDp = 0f,
                endWidthDp = 0f,
                visibilityThresholdDp = 15f
            ),
            decorations = listOf(
                AnalysisTargetRangeDecoration(
                    minYValue = 80.0,
                    maxYValue = 180.0,
                    color = AnalysisGlucoseTargetRangeColor
                ),
//                AnalysisDashedLineDecoration(80.0, AnalysisGlucoseLowColor),
//                AnalysisDashedLineDecoration(180.0, AnalysisGlucoseHighColor)
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
private fun AverageGlucoseChart(data: List<AverageGlucosePoint>) {
    val displayData = if (data.isEmpty()) {
        List(7) { AverageGlucosePoint("--", "", null) }
    } else {
        data
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        val chartLeft = 18.dp.toPx()
        val chartRight = size.width - 18.dp.toPx()
        val chartTop = 28.dp.toPx()
        val chartBottom = size.height - 58.dp.toPx()
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        val xStep = chartWidth / (displayData.size - 1).coerceAtLeast(1)

        val validValues = displayData.mapNotNull(AverageGlucosePoint::average)
        val minValue = ((validValues.minOrNull() ?: 80.0) - 15.0).coerceAtLeast(0.0)
        var maxValue = (validValues.maxOrNull() ?: 180.0) + 15.0
        if (maxValue <= minValue) maxValue = minValue + 1.0
        val valueRange = maxValue - minValue

        fun pointFor(index: Int, value: Double): Offset {
            val x = chartLeft + xStep * index
            val normalized = ((value - minValue) / valueRange).toFloat().coerceIn(0f, 1f)
            val y = chartBottom - chartHeight * normalized
            return Offset(x, y)
        }

        val linePath = Path()
        var hasPreviousPoint = false
        displayData.forEachIndexed { index, item ->
            val average = item.average
            if (average == null) {
                hasPreviousPoint = false
            } else {
                val point = pointFor(index, average)
                if (hasPreviousPoint) {
                    linePath.lineTo(point.x, point.y)
                } else {
                    linePath.moveTo(point.x, point.y)
                }
                hasPreviousPoint = true
            }
        }

        drawPath(
            path = linePath,
            color = Color(0xFF808080),
            style = Stroke(width = 3.dp.toPx())
        )

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TextDark.toArgb()
            textSize = 13.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TextDark.toArgb()
            textSize = 12.sp.toPx()
            textAlign = Paint.Align.CENTER
        }

        displayData.forEachIndexed { index, item ->
            val x = chartLeft + xStep * index
            val average = item.average

            if (average != null) {
                val point = pointFor(index, average)
                drawCircle(
                    color = Color(0xFFD3D3D3),
                    radius = 8.dp.toPx(),
                    center = point
                )
                drawContext.canvas.nativeCanvas.drawText(
                    average.roundToInt().toString(),
                    point.x,
                    (point.y - 14.dp.toPx()).coerceAtLeast(valuePaint.textSize),
                    valuePaint
                )
            } else {
                drawContext.canvas.nativeCanvas.drawText(
                    "--",
                    x,
                    chartTop + chartHeight / 2f,
                    valuePaint
                )
            }

            drawContext.canvas.nativeCanvas.drawText(
                item.primaryLabel,
                x,
                chartBottom + 28.dp.toPx(),
                axisPaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                item.secondaryLabel,
                x,
                chartBottom + 48.dp.toPx(),
                axisPaint
            )
        }
    }
}
