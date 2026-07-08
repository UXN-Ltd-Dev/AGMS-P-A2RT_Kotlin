package kr.co.uxn.agms_p_a2rt.ui.components.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.point
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
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ble.BleBridge
import kr.co.uxn.agms_p_a2rt.rememberMarker
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.UserGlucose
import kr.co.uxn.agms_p_a2rt.ui.components.main.ModeDialog
import kr.co.uxn.agms_p_a2rt.ui.components.main.NotiDialog
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.HomeViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.system.exitProcess
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.core.app.NotificationManagerCompat
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import kr.co.uxn.agms_p_a2rt.GuestList
import kr.co.uxn.agms_p_a2rt.BuildConfig
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p_a2rt.ble.BleBridge.showLowGlucoseDialog
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

private val GlucoseNormalPointColor = Color(0xFF65B66F)
private val GlucoseHighPointColor = Color(0xFFFFA12B)
private val GlucoseLowPointColor = Color(0xFF8FAEFF)
private val GlucoseTargetRangeColor = Color(0x2E9B9797)
private val HomeInitialZoom = Zoom.x(5.0)

internal object DemoGlucoseConfig {
    val ENABLED = BuildConfig.DEMO_GLUCOSE_ENABLED
    const val HOME_HISTORY_HOURS = 24L
    const val HOME_INTERVAL_MINUTES = 5L
    const val ANALYSIS_HISTORY_DAYS = 10L
    const val ANALYSIS_INTERVAL_MINUTES = 1L
}

private fun createDemoGlucoseData(
    userId: Int,
    currentTimeMillis: Long = System.currentTimeMillis()
): MutableList<UserGlucose> {
    val durationMinutes = DemoGlucoseConfig.HOME_HISTORY_HOURS * 60L
    val intervalMinutes = DemoGlucoseConfig.HOME_INTERVAL_MINUTES
    val intervalMillis = intervalMinutes * 60 * 1000L
    val endTime = currentTimeMillis - (currentTimeMillis % intervalMillis)
    val startTime = endTime - durationMinutes * 60 * 1000L
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }
    val random = Random(20260627)
    var glucose = 115.0
    var targetGlucose = 115.0

    return (0L..durationMinutes step intervalMinutes)
        .mapIndexed { index, elapsedMinutes ->
            if (index % 12 == 0) {
                targetGlucose = random.nextDouble(20.0, 225.0)
            }
            glucose = (glucose +
                (targetGlucose - glucose) * 0.1 +
                random.nextDouble(-1.5, 1.5))
                .coerceIn(0.0, 240.0)
            val time = startTime + elapsedMinutes * 60 * 1000L

            UserGlucose(
                userId = userId,
                glucose = glucose,
                weo1 = 0.0,
                weo2 = 0.0,
                createdAt = formatter.format(Date(time)),
                createdAtLong = time
            )
        }
        .toMutableList()
}

private fun glucosePointColor(value: Double): Color {
    return when {
        value > 180.0 -> GlucoseHighPointColor
        value < 80.0 -> GlucoseLowPointColor
        else -> GlucoseNormalPointColor
    }
}

private class DashedGlucoseLineDecoration(
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

        val canvasY =
            context.layerBounds.bottom -
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

private class GlucoseTargetRangeDecoration(
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

@SuppressLint("RestrictedApi")
@Composable
fun HomeScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    homeViewModel: HomeViewModel,
    bleViewModel: BleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isSensorEnded by DataStoreManager.getIsSensorEnded().collectAsState(initial = false)
    val day by homeViewModel.day.collectAsState()
    val remainingTimeText by homeViewModel.remainingTimeText.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val totalEntryCount = remember { mutableStateOf(0) }

    val glucose by bleViewModel.glucose.collectAsState()
    val chartTrigger by bleViewModel.chartTrigger.collectAsState()
    val chartReferenceTime = remember(chartTrigger) {
        val now = System.currentTimeMillis()
        now - (now % (60 * 1000L))
    }

    // 다이얼로그 변수 모음
    var showCaliDialog = bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog = bleViewModel.showBleConnectDialog.collectAsState()
    var showBluetoothOnDialog = bleViewModel.showBluetoothOnDialog.collectAsState()
    var showLowGlucoseDialog = bleViewModel.showLowGlucoseDialog.collectAsState()
    var showHighGlucoseDialog = bleViewModel.showHighGlucoseDialog.collectAsState()
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()
    var showModeDialog = remember { mutableStateOf(false) }

    var selectedChartOption by remember { mutableStateOf(context.getString(R.string.chart_option_glucose)) }
    val glucoseTrend =
        remember { mutableStateOf(context.getString(R.string.glucose_trend_level_3)) }
    val glucoseTrendImgResource = remember { mutableStateOf(R.drawable.level3) }
    val demoCurrentGlucose = remember(chartReferenceTime) {
        if (DemoGlucoseConfig.ENABLED) {
            createDemoGlucoseData(userId = -1, currentTimeMillis = chartReferenceTime)
                .last()
                .glucose
                .roundToInt()
        } else {
            0
        }
    }
    val displayedGlucose = if (DemoGlucoseConfig.ENABLED) {
        demoCurrentGlucose
    } else {
        glucose
    }
    val currentGlucoseColor = when {
//        displayedGlucose == 0 -> Color.Black
//        displayedGlucose > 180 -> Color.Red
        displayedGlucose < 70 -> Color.Red
        else -> Color.Black
    }

    var email = rememberSaveable { mutableStateOf("") }

    // Vico Chart
    val modelProducer = remember { CartesianChartModelProducer() }

    val chartScrollSpec = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth
    )
    val (yMax, setYMax) = remember { mutableStateOf(250.0) }
    val currentYMax by rememberUpdatedState(yMax)
    var forceRecompose by remember { mutableStateOf(0) }
    val rangeProvider = remember(yMax) {
        CartesianLayerRangeProvider.fixed(minY = 50.0, maxY = 350.0)
    }

    val x = remember { mutableListOf<Number>() }
    val y = remember { mutableListOf<Number>() }
    var earliestChartX by remember { mutableDoubleStateOf(0.0) }
    var latestChartX by remember { mutableDoubleStateOf(0.0) }
    var latestVisibleChartX by remember { mutableDoubleStateOf(0.0) }

    val isLoading = remember { mutableStateOf(false) }

    // 가로 모드 변수
    val checkedForLandscapeMode = remember { mutableStateOf(false) }

    // RadioButton
    var selectedTimeOption by remember { mutableStateOf(context.getString(R.string.chart_hour_6)) }
    var appliedTimeOption by remember { mutableStateOf(selectedTimeOption) }

    fun getFixedAxisLabelValues(): List<Double> {
        val durationMinutes = when (appliedTimeOption) {
            context.getString(R.string.chart_hour_6) -> 6 * 60.0
            context.getString(R.string.chart_hour_12) -> 12 * 60.0
            else -> 24 * 60.0
        }
        return listOf(
            latestChartX - durationMinutes,
            latestChartX - durationMinutes / 2.0,
            latestChartX
        )
    }

    val customItemPlacer = object : HorizontalAxis.ItemPlacer {
        override fun getFirstLabelValue(
            context: CartesianMeasuringContext,
            maxLabelWidth: Float
        ): Double = earliestChartX

        override fun getLastLabelValue(
            context: CartesianMeasuringContext,
            maxLabelWidth: Float
        ): Double = latestChartX

        override fun getLabelValues(
            context: CartesianDrawingContext,
            visibleXRange: ClosedFloatingPointRange<Double>,
            fullXRange: ClosedFloatingPointRange<Double>,
            maxLabelWidth: Float
        ): List<Double> {
            return if (x.isEmpty()) {
                listOf(
                    visibleXRange.start,
                    (visibleXRange.start + visibleXRange.endInclusive) / 2.0,
                    visibleXRange.endInclusive
                ).distinct()
            } else {
                getFixedAxisLabelValues()
            }
        }

        // 그래프 좌측 마진
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
        ): List<Double> {
            if (x.isEmpty()) {
                return listOf(fullXRange.start, fullXRange.endInclusive).distinct()
            }
            return getFixedAxisLabelValues()
        }

        override fun getHeightMeasurementLabelValues(
            context: CartesianMeasuringContext,
            layerDimensions: CartesianLayerDimensions,
            fullXRange: ClosedFloatingPointRange<Double>,
            maxLabelWidth: Float
        ): List<Double> {
            if (x.isEmpty()) {
                return listOf(fullXRange.start, fullXRange.endInclusive).distinct()
            }
            return getFixedAxisLabelValues()
        }
    }

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val fontSize = when {
        screenHeightDp == 783 -> 17.sp // a시리즈
        else -> 16.sp
    }

    LaunchedEffect(Unit) {
        try {
            email.value = withContext(Dispatchers.IO) {
                val storedEmail = DataStoreManager.getEmail().first() ?: ""
                if (storedEmail.isNotBlank()) {
                    storedEmail
                } else {
                    val userId = DataStoreManager.getUserId().first() ?: -1
                    val getUserData = tokenRetrofit.getUser(userId)
                    val fetchedEmail = if (getUserData.isSuccessful) {
                        getUserData.body()?.email.orEmpty()
                    } else {
                        Log.e("TEST", "이메일 조회 API 에러 : ${getUserData.errorBody()?.string()}")
                        ""
                    }
                    if (fetchedEmail.isNotBlank()) {
                        DataStoreManager.saveEmail(fetchedEmail)
                    }
                    fetchedEmail
                }
            }
        } catch (e: Exception) {
            Log.e("TEST", "이메일 조회 실패 : ${e.message}", e)
        }
        Log.d("TEST", "email.value = ${email.value}")
    }

    LaunchedEffect(Unit) {
        val verifiedDSLandscapeMode = DataStoreManager.getLandScapeMode().first() ?: false
        checkedForLandscapeMode.value = verifiedDSLandscapeMode
    }

    LaunchedEffect(selectedChartOption) {
        when (selectedChartOption) {
            context.getString(R.string.chart_option_glucose) -> setYMax(250.0)
            context.getString(R.string.chart_option_weo1), context.getString(R.string.chart_option_weo2) -> setYMax(
                50.0
            ) // 시작은 50.0, 필요시 5.0/10.0 등으로 조정
        }
        forceRecompose++
    }

    LaunchedEffect(chartTrigger, selectedTimeOption, selectedChartOption) {
        withContext(Dispatchers.IO) {
            // delay는 추후에 ANR이 발생하면 다시 활성화할 것!!
            delay(500)
            val nextX = mutableListOf<Number>()
            val nextY = mutableListOf<Number>()
            val userId = DataStoreManager.getUserId().first() ?: -1
            val userEmail = DataStoreManager.getEmail().first().orEmpty()
            val isGuest = GuestList.getGuestList().contains(userEmail)

            Log.e("TEST", "selectedOption : ${selectedTimeOption}")
            val selectedDurationMinutes = when (selectedTimeOption) {
                context.getString(R.string.chart_hour_6) -> 6 * 60
                context.getString(R.string.chart_hour_12) -> 12 * 60
                else -> 24 * 60
            }
            val lastTime =
                chartReferenceTime - (selectedDurationMinutes * 60 * 1000L)
            Log.e("DB", "lastTime : ${lastTime}")
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val convertedLastTime = formatter.format(Date(lastTime))

            Log.e("DB", "converted : ${convertedLastTime}")

            val baseTime = 1743442800000L // 25년 4월 1일 00시 00분 00초
            val isGlucoseChart =
                selectedChartOption == context.getString(R.string.chart_option_glucose)
            val glucoseData = mutableListOf<UserGlucose>()

            if (isGlucoseChart) {
                glucoseData += if (DemoGlucoseConfig.ENABLED) {
                    createDemoGlucoseData(userId, chartReferenceTime)
                } else {
                    localDbRepository?.dataDao()
                        ?.getGlucoseListAfterLastTime(userId = userId, lastTime = lastTime)
                        ?.toMutableList()
                        ?: mutableListOf()
                }
                glucoseData.sortBy { it.createdAtLong }
                totalEntryCount.value = glucoseData.size

                if (glucoseData.isNotEmpty() && !isGuest) {
                    val minuteFormatter =
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val intervalMinutes = if (isGuest) 1L else 5L
                    val latestMinute = glucoseData.last().createdAtLong / (60 * 1000L)
                    val latestBucket = latestMinute / intervalMinutes
                    val startBucket =
                        (latestMinute - selectedDurationMinutes) / intervalMinutes
                    val dataByBucket = glucoseData
                        .groupBy {
                            (it.createdAtLong / (60 * 1000L)) / intervalMinutes
                        }
                        .mapValues { (_, values) -> values.maxBy { it.createdAtLong } }

                    glucoseData.clear()
                    for (bucket in startBucket..latestBucket) {
                        val existingData = dataByBucket[bucket]
                        if (existingData != null) {
                            glucoseData.add(existingData)
                        } else {
                            val minute = bucket * intervalMinutes
                            val time = minute * 60 * 1000L
                            glucoseData.add(
                                UserGlucose(
                                    userId = userId,
                                    glucose = -10.0,
                                    weo1 = -10.0,
                                    weo2 = -10.0,
                                    createdAt = minuteFormatter.format(Date(time)),
                                    createdAtLong = time
                                )
                            )
                        }
                    }
                }

                glucoseData.sortBy { it.createdAtLong }
                if (isGuest && glucoseData.isNotEmpty()) {
                    val rangeStartMinutes = (lastTime - baseTime) / 60_000.0
                    nextX.add((rangeStartMinutes * 10_000).roundToLong() / 10_000.0)
                    nextY.add(-10f)
                }
                glucoseData.forEach { data ->
                    val rawTimeDiffMinutes =
                        (data.createdAtLong - baseTime) / 60_000.0
                    val timeDiffMinutes =
                        (rawTimeDiffMinutes * 10_000).roundToLong() / 10_000.0
                    nextX.add(timeDiffMinutes)
                    nextY.add(data.glucose.toFloat())
                }
            } else {
                val intervalMinutes = 5L
                val minuteMillis = 60 * 1000L
                val intervalMillis = intervalMinutes * minuteMillis
                val userValueList = localDbRepository?.dataDao()
                    ?.getListAfterLastTime(userId = userId, lastTime = lastTime)
                    .orEmpty()
                totalEntryCount.value = userValueList.size

                val dataByBucket = userValueList
                    .groupBy {
                        (it.createdAtLong / minuteMillis) / intervalMinutes
                    }
                    .mapValues { (_, values) -> values.maxBy { it.createdAtLong } }
                val startBucket = (lastTime + intervalMillis - 1) / intervalMillis
                val endBucket = chartReferenceTime / intervalMillis
                val firstBucketTime = startBucket * intervalMillis

                if (firstBucketTime > lastTime) {
                    nextX.add((lastTime - baseTime).toDouble() / (1000.0 * 60.0))
                    nextY.add(-10f)
                }

                for (bucket in startBucket..endBucket) {
                    val time = bucket * intervalMillis
                    val data = dataByBucket[bucket]
                    val value = when (selectedChartOption) {
                        context.getString(R.string.chart_option_weo1) -> data?.weCurrent
                        else -> data?.aeCurrent
                    } ?: -10.0

                    nextX.add(((time - baseTime) / 1000 / 60).toDouble())
                    nextY.add(value.toFloat())
                }

                val currentTimeX =
                    (chartReferenceTime - baseTime).toDouble() / (1000.0 * 60.0)
                if (nextX.lastOrNull()?.toDouble() != currentTimeX) {
                    nextX.add(currentTimeX)
                    nextY.add(-10f)
                }
            }

            Log.d("TEST", "x : $nextX  y : ${nextY.size}")

            withContext(Dispatchers.Main) {
                delay(100)
                val shouldResetTimeRange = appliedTimeOption != selectedTimeOption
                earliestChartX = nextX.firstOrNull()?.toDouble() ?: 0.0
                latestChartX = nextX.lastOrNull()?.toDouble() ?: 0.0
                latestVisibleChartX = nextX.indices
                    .lastOrNull { index -> nextY[index].toDouble() != -10.0 }
                    ?.let { index -> nextX[index].toDouble() }
                    ?: latestChartX
//                modelProducer.setEntries(dataSetForModel)

                if (nextX.isNotEmpty() && nextY.isNotEmpty()) {
                    x.clear()
                    x.addAll(nextX)
                    y.clear()
                    y.addAll(nextY)
                    modelProducer.runTransaction {
                        lineSeries { series(nextX, nextY) }
                    }
                    appliedTimeOption = selectedTimeOption
                    if (shouldResetTimeRange) {
                        forceRecompose++
                    }
                    isLoading.value = true
                } else {
                    Log.d("VICO", "Empty dataset! Skipping model update.")
                }

                isLoading.value = true
                delay(100)
                chartScrollSpec.animateScroll(
                    Scroll.Absolute.End
                )
            }

            if (isGlucoseChart) {
                val actualGlucoseData = glucoseData.filter { it.glucose >= 0.0 }
                glucoseTrend.value = getTrendStatus(context, actualGlucoseData)
                glucoseTrendImgResource.value = when (glucoseTrend.value) {
                    context.getString(R.string.glucose_trend_level_5) -> R.drawable.level5
                    context.getString(R.string.glucose_trend_level_4) -> R.drawable.level4
                    context.getString(R.string.glucose_trend_level_3) -> R.drawable.level3
                    context.getString(R.string.glucose_trend_level_2) -> R.drawable.level2
                    else -> R.drawable.level1
                }
            }
        }
    }

    // 실제 타이머
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // onResume 시점에만 실행!
                homeViewModel.startTimer()
                Log.d("TEST", "홈 화면에서 타이머 실행")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 1. Dialog : 일일 혈당 입력
    if (showCaliDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showCaliDialog(false) },
            onConfirm = {
                BleBridge.showCaliDialog(false)
                navController.navigate("GlucoseRegisterScreen")
            },
            title = stringResource(R.string.dialog_daily_enter_glucose_title),
            content = stringResource(R.string.dialog_daily_enter_glucose_content)
        )
    }

    // 2. Dialog : BLE 끊김
    if (showBleConnectDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
            },
            title = stringResource(R.string.dialog_ble_disconnected_title),
            content = stringResource(R.string.dialog_ble_disconnected_content)
        )
    }

    // 3. 그래프 모드 다이얼로그
    if (showModeDialog.value) {
        ModeDialog(
            options = listOf(
                stringResource(R.string.chart_option_glucose),
                stringResource(R.string.chart_option_weo1),
                stringResource(R.string.chart_option_weo2)
            ),
            selectedOption = selectedChartOption,
            onOptionSelected =
                {
                    showModeDialog.value = false
                    selectedChartOption = it
                },
            onDismissRequest = { showModeDialog.value = false }
        )
    }

    // 4. Dialog : 블루투스 ON
    if (showBluetoothOnDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBluetoothOnDialog(false) },
            onConfirm = {
                BleBridge.showBluetoothOnDialog(false)
            },
            title = stringResource(R.string.dialog_bluetooth_off_title),
            content = stringResource(R.string.dialog_bluetooth_off_content),
        )
    }

    // 5. Dialog : 측정 종료
    if (showEndMeasurementDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
                coroutineScope.launch(Dispatchers.IO) {
                    val userId = DataStoreManager.getUserId().first() ?: -1
                    try {
                        val sensorOff = tokenRetrofit.doSensorOff(userId)
                        if (sensorOff.isSuccessful) {
                            val sensorOffBody = sensorOff.body()
                            if (sensorOffBody != null) {
                                Log.w("TEST", "sensorOff responseBody : ${sensorOffBody}")
                                if (sensorOffBody.isSuccess) {
                                    // userId의 db삭제
                                    localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                                    Log.w("TEST", "sensorOff 성공")
                                    DataStoreManager.saveIsMain(false)
                                    DataStoreManager.deleteRoute()
                                    DataStoreManager.saveRoute("Splash")
                                    Log.d("TEST", "${DataStoreManager.getIsMain().first()}")
                                    DataStoreManager.deleteAccessToken()
                                    DataStoreManager.deleteRefreshToken()
                                    DataStoreManager.deleteUserId()
                                    DataStoreManager.deleteDeviceMac()
                                    DataStoreManager.deleteStartTime()
                                    DataStoreManager.deleteEndTime()
                                    DataStoreManager.deleteDailyCalibrationTime()
                                    DataStoreManager.deleteDailyCalibrationLastTime()
                                    DataStoreManager.setLandScapeMode(false)
                                    DataStoreManager.deleteTargetLowGlucose()
                                    DataStoreManager.deleteTargetHighGlucose()
                                    DataStoreManager.deleteEmail()
                                    withContext(Dispatchers.Main) {
                                        // 1. 서비스 종료
                                        bleViewModel.emit("STOP_SERVICE")
                                        // 앱 강제 종료
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                        exitProcess(0)
                                    }
                                } else {
                                    Log.w("TEST", "sensorOff 실패")
                                }
                            }
                        } else {
                            Log.w("TEST", "sensorOff API통신 실패 : ${sensorOff.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.d("TEST", "sensorOff API통신 실패 : ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_network_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            title = stringResource(R.string.dialog_end_measurement_title),
            content = stringResource(R.string.dialog_end_measurement_content),
        )
    }

    // 6.1 Dialog : 저혈당
    if (showLowGlucoseDialog.value) {
        NotiDialog(
            onDismiss = { showLowGlucoseDialog(false) },
            onConfirm = {
                showLowGlucoseDialog(false)
                NotificationManagerCompat.from(context).cancel(95)
            },
            title = stringResource(R.string.dialog_low_glucose_title),
            content = stringResource(R.string.dialog_low_glucose_content),
        )
    }

    // 6.2 Dialog : 고혈당
    if (showHighGlucoseDialog.value) {
        NotiDialog(
            onDismiss = { showHighGlucoseDialog(false) },
            onConfirm = {
                showHighGlucoseDialog(false)
                NotificationManagerCompat.from(context).cancel(96)
            },
            title = stringResource(R.string.dialog_high_glucose_title),
            content = stringResource(R.string.dialog_high_glucose_content)
        )
    }

    if (isSensorEnded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background_grey))
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .padding(vertical = 48.dp), // 상하 여백 추가
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // 상단 텍스트
                    Text(
                        text = "센서가 종료되었습니다.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(id = R.drawable.add_sensor_icon),
                        contentDescription = "플러그 및 플러스 아이콘",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(50.dp) // 이미지 크기 조절
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Spacer(modifier = Modifier.weight(1f))

                    // 하단 버튼
                    Button(
                        onClick = { navController.navigate("RegisterDeviceQRScreen") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA733) // 주황색 버튼
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "새로운 센서로 시작하기",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background_grey))
                .padding(paddingValues)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 혈당 표시 카드
            Card(
                modifier = Modifier
                    .weight(0.7f)
                    .height(130.dp)
                    .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                    .pointerInput(Unit) {
                        if (GuestList.getVipList().contains(email.value)) {
                            detectTapGestures(
                                onLongPress = {
                                    Log.d("TEST", "email : ${email.value}")
                                    showModeDialog.value = true
                                }
                            )
                        } else { detectTapGestures(
                                onLongPress = {
                                    // 롱클릭 시 실행할 코드
                                    Log.d("TEST", "I'm guest : ${email.value}")
                                }
                            )
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.background_white), // 카드 배경색 설정
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text(
                            text = stringResource(R.string.current_glucose),
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(start = 20.dp, top = 15.dp)
                                .weight(0.4f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.6f)
                                .padding(start = 40.dp, bottom = 20.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = if (displayedGlucose == 0) {
                                    "_ _"
                                } else {
                                    "$displayedGlucose"
                                },
                                color = currentGlucoseColor,
                                fontSize = 45.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                            )
                            Text(
                                text = "mg/dL",
                                color = Color.Black,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 5.dp),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(0.2f)
                    ) {
                        // 이전에 만들었던 5단계 방향 지표 컴포저블 호출
                        val mappedTrend = when (glucoseTrend.value) {
                            context.getString(R.string.glucose_trend_level_5) -> GlucoseTrend.UP_FAST
                            context.getString(R.string.glucose_trend_level_4) -> GlucoseTrend.UP
                            context.getString(R.string.glucose_trend_level_3) -> GlucoseTrend.STEADY
                            context.getString(R.string.glucose_trend_level_2) -> GlucoseTrend.DOWN
                            context.getString(R.string.glucose_trend_level_1) -> GlucoseTrend.DOWN_FAST
                            else -> GlucoseTrend.STEADY // 예외 발생 시 기본값은 안정(STEADY)
                        }

                        GlucoseTrendIndicator(
                            trend = mappedTrend
                        )
                    }

                }
            }

            // 2. 잔여 시간 박스
            Card(
                modifier = Modifier
                    .weight(0.3f)
                    .height(130.dp) // 필요에 따라 높이 조절
                    .padding(10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White, // 카드의 전체 배경색을 흰색으로 설정
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 상단 부분 (오렌지색 배경)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 상단과 하단의 비율 조절 (여기는 1 비율)
                            .background(colorResource(R.color.main_grey)), // 상단 영역에만 메인 컬러 적용
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${day}일차",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp // 이미지 비율에 맞게 폰트 크기 살짝 키움
                        )
                    }

                    // 하단 부분 (흰색 배경)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.7f) // 하단 영역이 상단보다 조금 더 넓게 비율 설정
                            .padding(vertical = 10.dp), // 상하 여백
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 잔여 시간 타이틀
                        Text(
                            text = "잔여 시간",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp)) // 두 텍스트 사이 간격

                        // 잔여 시간 데이터
                        Text(
                            text = remainingTimeText, // 예: "8일 12시간"
                            fontSize = 15.sp,
                            color = Color(0xFF5B5B5B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
//            Card(
//                modifier = Modifier
//                    .weight(0.3f)
//                    .height(130.dp)
//                    .padding(10.dp),
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = colorResource(R.color.main), // 카드 배경색 설정
//                ),
//                elevation = CardDefaults.cardElevation(
//                    defaultElevation = 10.dp
//                )
//            ) {
//                Spacer(modifier = Modifier.height(10.dp))
//
//
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//
//                    Spacer(modifier = Modifier.weight(0.5f))
//                    // 1. 몇 일차
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "${day}",
//                            color = colorResource(R.color.white),
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 16.sp
//                        )
//                        Text(
//                            text = "일차",
//                            color = colorResource(R.color.white),
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 16.sp
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.weight(1f))
//                    // 2. 가운데 줄
//                    DashedDivider(
//                        color = Color(0xFF707070),
//                        thickness = 0.dp,
//                        dashWidth = 5.dp,
//                        gapWidth = 3.dp
//                    )
//
//                    Spacer(modifier = Modifier.weight(1f))
//                    // 잔여 시간
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        text = "잔여 시간",
//                        fontSize = 13.sp,
//                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    // 잔여 시간 데이터
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        text = remainingTimeText,
//                        fontSize = 16.sp,
//                        color = Color(0xFF5B5B5B),
//                        fontWeight = FontWeight.Bold,
//                        textAlign = TextAlign.Center
//                    )
//                    Spacer(modifier = Modifier.weight(1f))
//                }
//            }
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White, // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            val mode = when (selectedChartOption) {
                context.getString(R.string.chart_option_glucose) -> context.getString(R.string.glucose_chart)
                context.getString(R.string.chart_option_weo1) -> context.getString(R.string.weo1_chart)
                else -> context.getString(R.string.weo2_chart)
            }
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = mode,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = fontSize
//                )
//            }

            // VICO 그래프
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .weight(1f),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val yDecimalFormat = DecimalFormat("#")
                    val startAxisValueFormatter = CartesianValueFormatter.decimal(yDecimalFormat)
                    val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
                        val baseTime = 1743442800000L
                        val timeMillis = baseTime + (value * 60 * 1000).toLong()
                        Instant.ofEpochMilli(timeMillis)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
                    }
                    val emphasizedMarkerValueSizePx = with(LocalDensity.current) {
                        14.sp.toPx().roundToInt()
                    }
                    val markerValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
                        val point = targets
                            .filterIsInstance<LineCartesianLayerMarkerTarget>()
                            .firstOrNull()
                            ?.points
                            ?.firstOrNull()
                            ?: return@ValueFormatter ""

                        val baseTime = 1743442800000L
                        val timeMillis = baseTime + (point.entry.x * 60 * 1000).toLong()
                        val valueText = if (
                            selectedChartOption == context.getString(R.string.chart_option_glucose)
                        ) {
                            DecimalFormat("#").format(point.entry.y)
                        } else {
                            DecimalFormat("0.##").format(point.entry.y)
                        }
                        val unitText =
                            if (selectedChartOption == context.getString(R.string.chart_option_glucose)) {
                                "mg/dL"
                            } else {
                                "nA"
                            }
                        val timeText = Instant.ofEpochMilli(timeMillis)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
                        val markerText = SpannableString("$timeText, $valueText $unitText")
                        val valueStart = timeText.length + 2
                        val valueEnd = valueStart + valueText.length
                        markerText.setSpan(
                            StyleSpan(Typeface.BOLD),
                            valueStart,
                            valueEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        markerText.setSpan(
                            AbsoluteSizeSpan(emphasizedMarkerValueSizePx),
                            valueStart,
                            valueEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        markerText
                    }
                    val defaultMarker = rememberMarker(
                        valueFormatter = markerValueFormatter,
                        indicatorColor = colorResource(R.color.text_secondary),
                        labelLineHeight = 16.sp,
                        labelTopMargin = 4.dp
                    )
                    val filteredMarker = remember(defaultMarker) {
                        object : CartesianMarker by defaultMarker {
                            private fun hasVisibleValue(
                                targets: List<CartesianMarker.Target>
                            ): Boolean {
                                val points = targets
                                    .filterIsInstance<LineCartesianLayerMarkerTarget>()
                                    .flatMap { it.points }
                                return points.isEmpty() || points.any { it.entry.y != -10.0 }
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
                    val normalPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black),
                            shape = CorneredShape.Pill
                        ),
                        size = 4.dp
                    )
                    val highPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black),
                            shape = CorneredShape.Pill
                        ),
                        size = 4.dp
                    )
                    val lowPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black),
                            shape = CorneredShape.Pill
                        ),
                        size = 4.dp
                    )
                    val latestPointTransition =
                        rememberInfiniteTransition(label = "latestGlucosePointBlink")
                    val latestPointAlpha by latestPointTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "latestGlucosePointAlpha"
                    )
                    val latestNormalPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black.copy(alpha = latestPointAlpha)),
                            shape = CorneredShape.Pill
                        ),
                        size = 8.dp
                    )
                    val latestHighPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black.copy(alpha = latestPointAlpha)),
                            shape = CorneredShape.Pill
                        ),
                        size = 8.dp
                    )
                    val latestLowPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black.copy(alpha = latestPointAlpha)),
                            shape = CorneredShape.Pill
                        ),
                        size = 8.dp
                    )
                    val weoPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Black),
                            shape = CorneredShape.Pill
                        ),
                        size = 4.dp
                    )
                    val latestWeoPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(
                                Color.Black.copy(alpha = latestPointAlpha)
                            ),
                            shape = CorneredShape.Pill
                        ),
                        size = 8.dp
                    )
                    val hiddenPoint = LineCartesianLayer.point(
                        rememberShapeComponent(
                            fill = fill(Color.Transparent),
                            shape = CorneredShape.Pill
                        ),
                        size = 1.dp
                    )
                    val glucosePointProvider =
                        remember(
                            normalPoint,
                            highPoint,
                            lowPoint,
                            latestNormalPoint,
                            latestHighPoint,
                            latestLowPoint,
                            hiddenPoint,
                            latestChartX
                        ) {
                        object : LineCartesianLayer.PointProvider {
                            override fun getPoint(
                                entry: LineCartesianLayerModel.Entry,
                                seriesIndex: Int,
                                extraStore: ExtraStore
                            ): LineCartesianLayer.Point {
                                return when {
                                    entry.y == -10.0 -> hiddenPoint
//                                    entry.x == latestChartX && entry.y > 180.0 -> latestHighPoint
//                                    entry.x == latestChartX && entry.y < 80.0 -> latestLowPoint
                                    entry.x == latestChartX -> latestNormalPoint
//                                    entry.y > 180.0 -> highPoint
//                                    entry.y < 80.0 -> lowPoint
                                    else -> normalPoint
                                }
                            }

                            override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point {
                                return normalPoint
                            }
                        }
                    }
                    val weoPointProvider = remember(
                        weoPoint,
                        latestWeoPoint,
                        hiddenPoint,
                        latestVisibleChartX
                    ) {
                        object : LineCartesianLayer.PointProvider {
                            override fun getPoint(
                                entry: LineCartesianLayerModel.Entry,
                                seriesIndex: Int,
                                extraStore: ExtraStore
                            ): LineCartesianLayer.Point {
                                return when {
                                    entry.y == -10.0 -> hiddenPoint
                                    entry.x == latestVisibleChartX -> latestWeoPoint
                                    else -> weoPoint
                                }
                            }

                            override fun getLargestPoint(
                                extraStore: ExtraStore
                            ): LineCartesianLayer.Point = latestWeoPoint
                        }
                    }
                    val chartDecorations =
                        if (selectedChartOption == context.getString(R.string.chart_option_glucose)) {
                            listOf(
                                // 가운데 배경
                                GlucoseTargetRangeDecoration(
                                    minYValue = 80.0,
                                    maxYValue = 180.0,
                                    color = GlucoseTargetRangeColor
                                ),
                                // 위아래 점선
//                                DashedGlucoseLineDecoration(80.0, GlucoseLowPointColor),
//                                DashedGlucoseLineDecoration(180.0, GlucoseHighPointColor)
                            )
                        } else {
                            emptyList()
                        }

                    if (isLoading.value == true && x.isNotEmpty() && y.isNotEmpty()) {
                        key(yMax, forceRecompose) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberLineCartesianLayer(
                                        lineProvider =
                                            LineCartesianLayer.LineProvider.series(
                                                LineCartesianLayer.rememberLine(
                                                    fill = LineCartesianLayer.LineFill.single(
                                                        fill(Color.Transparent)
                                                    ),
                                                    stroke = LineCartesianLayer.LineStroke.Continuous(
                                                        thicknessDp = 0f
                                                    ),
                                                    pointProvider =
                                                        if (selectedChartOption == context.getString(R.string.chart_option_glucose)) {
                                                            glucosePointProvider
                                                        } else {
                                                            weoPointProvider
                                                        },
                                                    pointConnector = LineCartesianLayer.PointConnector.cubic(
                                                        curvature = 0.8f
                                                    )
                                                )
                                            ),
                                        rangeProvider = rangeProvider
                                    ),
                                    endAxis = VerticalAxis.rememberEnd(
                                        valueFormatter = startAxisValueFormatter,
                                        label = rememberAxisLabelComponent(color = Color.Black),
                                        itemPlacer = if (yMax == 500.0) {
                                            VerticalAxis.ItemPlacer.count({ 6 })
                                        } else {
                                            VerticalAxis.ItemPlacer.count({ 6 })
                                        },
//                                            guideline = null
                                    ),
                                    bottomAxis = HorizontalAxis.rememberBottom(
                                        valueFormatter = bottomAxisFormatter,
                                        label = rememberAxisLabelComponent(color = Color.Black),
//                                            itemPlacer = HorizontalAxis.ItemPlacer.aligned(
//                                                spacing = { 20 }, // 5개의 xStep마다 하나의 라벨
//                                                offset = { 2 },
//                                                shiftExtremeLines = true,
//                                                addExtremeLabelPadding = true
//                                            ),
//                                            itemPlacer = HorizontalAxis.ItemPlacer.segmented(false),
                                        itemPlacer = customItemPlacer,
//                                            guideline = null
                                        labelRotationDegrees = 0f
                                    ),
                                    marker = filteredMarker,
                                    fadingEdges = FadingEdges( // 또는 FadingEdges.horizontal() 도 가능
                                        startWidthDp = 0f,
                                        endWidthDp = 0f,
                                        visibilityThresholdDp = 15f
                                    ),
                                    decorations = chartDecorations,
//                                        layerPadding = {
//                                            cartesianLayerPadding(
//                                                scalableStart = 10.dp,
//                                                unscalableStart = 10.dp,
//                                                scalableEnd = 10.dp,
//                                                unscalableEnd = 10.dp
//                                            )
//                                        }
                                ),
                                modelProducer = modelProducer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                scrollState = chartScrollSpec,
                                animationSpec = null,
                                animateIn = false,
                                zoomState = rememberVicoZoomState(
                                    zoomEnabled = true,
                                    initialZoom = HomeInitialZoom
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.chart_wait_a_moment),
                                color = colorResource(R.color.main),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    RadioButtonSingleSelection(
                        selectedOption = selectedTimeOption,
                        onOptionSelected = { selectedTimeOption = it },
                        context = context
                    )
                } // column
            } // surface
        }
    }
    }
}

@Composable
fun RadioButtonSingleSelection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    val radioOptions = listOf(
        context.getString(R.string.chart_hour_6),
        context.getString(R.string.chart_hour_12),
        context.getString(R.string.chart_hour_24)
    )
    val containerColor = Color(0xFFEFEFF4)
    val unselectedTextColor = Color(0xFF7A8293)
    val selectedTextColor = Color(0xFF1A1E27)
    val dotColor = Color(0xFFFF7A00)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .padding(horizontal = 25.dp)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(50)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        radioOptions.forEach { text ->
            val isSelected = text == selectedOption

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onOptionSelected(text)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colorResource(R.color.main), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = text,
                    color = if (isSelected) selectedTextColor else unselectedTextColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    }
}

fun getTrendStatus(context: Context, glucoseValueList: List<UserGlucose>): String {
    if (glucoseValueList.size < 4) return context.getString(R.string.glucose_trend_level_3)// 유지 중
//    val list = glucoseValueList.takeLast(5)

    val sorted = glucoseValueList.sortedBy { it.createdAtLong } // 시간 순 정렬 보장
    val list = sorted.takeLast(5) // 최신 5개

//    val list = glucoseValueList.take(5)

    Log.d("TEST", "glucoseValeList : ${glucoseValueList}")
    Log.d("TEST", "glucoseValeList last 5 : ${list}")
    val n = list.size
    val x = (0 until n).toList()
    val y = list.map { it.glucose }

    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y) { xi, yi -> xi * yi }.sum()
    val sumXSquare = x.sumOf { it * it }

    val numerator = n * sumXY - sumX * sumY
    val denominator = n * sumXSquare - sumX * sumX

    val slope = if (denominator != 0) numerator.toDouble() / denominator else 0.0
    Log.d("TEST", "slope : $slope")

    return when {
        slope >= 10.0 -> context.getString(R.string.glucose_trend_level_5) // "급상승"
        slope in 2.1..4.9 -> context.getString(R.string.glucose_trend_level_4) // "상승 중"
        slope in -2.0..2.0 -> context.getString(R.string.glucose_trend_level_3) // "유지 중"
        slope in -4.9..-2.1 -> context.getString(R.string.glucose_trend_level_2) // "하강 중"
        slope <= -10.0 -> context.getString(R.string.glucose_trend_level_1)// "급하강"
        else -> context.getString(R.string.glucose_trend_level_1)
    }
}

@Composable
fun DashedDivider(
    color: Color,
    thickness: androidx.compose.ui.unit.Dp,
    dashWidth: androidx.compose.ui.unit.Dp,
    gapWidth: androidx.compose.ui.unit.Dp
) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(thickness)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashWidth.toPx(), gapWidth.toPx()),
                phase = 0f
            )
        )
    }
}

enum class GlucoseTrend {
    DOWN_FAST,  // 매우 낮음 (파란색 화살표 2개)
    DOWN,       // 낮음 (하늘색 화살표 1개)
    STEADY,     // 안정 (초록색 원)
    UP,         // 높음 (주황색 화살표 1개)
    UP_FAST     // 매우 높음 (주황색 화살표 2개)
}

// 2. 메인 지표 카드 컴포저블
@Composable
fun GlucoseTrendIndicator(
    trend: GlucoseTrend,
    modifier: Modifier = Modifier
) {
    // 사용할 색상 정의 (이미지 기반)
    val inactiveColor = Color(0xFFDCDCDC) // 비활성화 회색
    val colorGreen = Color(0xFF7CB342)    // 안정 (초록)
    val colorOrange = Color(0xFFF2994A)   // 높음 (주황)
    val colorLightOrange = Color(0xFFF8C496) // 매우 높음 서브 (연한 주황)
    val colorBlue = Color(0xFF6A9DFF)     // 매우 낮음 (파랑)
    val colorLightBlue = Color(0xFFA5C3FF) // 낮음 서브 (연한 파랑)

    // 상태에 따른 각 도형의 색상 결정
    val topArrowColor = if (trend == GlucoseTrend.UP_FAST) colorOrange else inactiveColor
    val secondArrowColor = when (trend) {
        GlucoseTrend.UP_FAST -> colorLightOrange
        GlucoseTrend.UP -> colorOrange
        else -> inactiveColor
    }
    val circleColor = if (trend == GlucoseTrend.STEADY) colorGreen else inactiveColor
    val fourthArrowColor = when (trend) {
        GlucoseTrend.DOWN_FAST -> colorLightBlue
        GlucoseTrend.DOWN -> colorBlue
        else -> inactiveColor
    }
    val bottomArrowColor = if (trend == GlucoseTrend.DOWN_FAST) colorBlue else inactiveColor

    // UI 레이아웃
    Column(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp) // 도형 사이의 간격
    ) {
        val iconSize = 14.dp

        // 1. 맨 위 화살표 (위쪽)
        TriangleUp(color = topArrowColor, size = iconSize)

        // 2. 두 번째 화살표 (위쪽)
        TriangleUp(color = secondArrowColor, size = iconSize)

        // 3. 가운데 원
        Canvas(modifier = Modifier.size(iconSize)) {
            drawCircle(color = circleColor, radius = size.width / 2.5f)
        }

        // 4. 네 번째 화살표 (아래쪽)
        TriangleDown(color = fourthArrowColor, size = iconSize)

        // 5. 맨 아래 화살표 (아래쪽)
        TriangleDown(color = bottomArrowColor, size = iconSize)
    }
}

// --- 아래는 도형을 그리는 헬퍼 컴포저블입니다 ---

@Composable
fun TriangleUp(color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val path = Path().apply {
            moveTo(size.toPx() / 2f, 0f)         // 위쪽 꼭짓점
            lineTo(size.toPx(), size.toPx())     // 오른쪽 아래
            lineTo(0f, size.toPx())              // 왼쪽 아래
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun TriangleDown(color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val path = Path().apply {
            moveTo(0f, 0f)                       // 왼쪽 위
            lineTo(size.toPx(), 0f)              // 오른쪽 위
            lineTo(size.toPx() / 2f, size.toPx())// 아래쪽 꼭짓점
            close()
        }
        drawPath(path = path, color = color)
    }
}
