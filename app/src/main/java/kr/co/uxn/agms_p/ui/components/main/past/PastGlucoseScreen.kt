package kr.co.uxn.agms_p.ui.components.main.past

import android.annotation.SuppressLint
import android.content.Context
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.FadingEdges
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.rememberMarker
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.room.UserGlucose
import kr.co.uxn.agms_p.ui.components.main.ModeDialog
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.system.exitProcess
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import kr.co.uxn.agms_p.GuestList
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog

// 화면 테스트용 더미 데이터입니다. 실제 DB에는 저장하지 않습니다.
// 더미를 빼려면 아래 값을 false로 바꾸면 됩니다.
private const val USE_PAST_GLUCOSE_DUMMY_DATA = false
private const val PAST_GLUCOSE_DUMMY_INTERVAL_MINUTES = 10

private data class PastGlucoseChartCardData(
    val dateKey: String,
    val title: String,
    val dayStartMillis: Long,
    val glucoseList: List<UserGlucose>
)

@SuppressLint("RestrictedApi")
@Composable
fun PastGlucoseScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    homeViewModel: HomeViewModel,
    bleViewModel: BleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val chartTrigger by bleViewModel.chartTrigger.collectAsState()

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

    var email = rememberSaveable { mutableStateOf("") }
    var chartCards by remember { mutableStateOf<List<PastGlucoseChartCardData>>(emptyList()) }

    val (yMax, setYMax) = remember { mutableStateOf(250.0) }
    val currentYMax by rememberUpdatedState(yMax)
    var forceRecompose by remember { mutableStateOf(0) }

    val isLoading = remember { mutableStateOf(false) }

    // 가로 모드 변수
    val checkedForLandscapeMode = remember { mutableStateOf(false) }

    val customItemPlacer = object : HorizontalAxis.ItemPlacer {
        override fun getLabelValues(
            context: CartesianDrawingContext,
            visibleXRange: ClosedFloatingPointRange<Double>,
            fullXRange: ClosedFloatingPointRange<Double>,
            maxLabelWidth: Float
        ): List<Double> {
//            val start = fullXRange.start
//            val end = fullXRange.endInclusive

            val start = visibleXRange.start
            val end = visibleXRange.endInclusive + 1
            val mid = (start + end) / 2

            return listOf(start, mid, end)
        }

        // 그래프 좌측 마진
        override fun getStartLayerMargin(
            context: CartesianMeasuringContext,
            layerDimensions: CartesianLayerDimensions,
            tickThickness: Float,
            maxLabelWidth: Float
        ): Float = 120f

        override fun getEndLayerMargin(
            context: CartesianMeasuringContext,
            layerDimensions: CartesianLayerDimensions,
            tickThickness: Float,
            maxLabelWidth: Float
        ): Float = 120f


        override fun getWidthMeasurementLabelValues(
            context: CartesianMeasuringContext,
            layerDimensions: CartesianLayerDimensions,
            fullXRange: ClosedFloatingPointRange<Double>
        ): List<Double> {
            val start = fullXRange.start
            val end = fullXRange.endInclusive
            val mid = (start + end) / 2
            return listOf(start, mid, end)
        }

        override fun getHeightMeasurementLabelValues(
            context: CartesianMeasuringContext,
            layerDimensions: CartesianLayerDimensions,
            fullXRange: ClosedFloatingPointRange<Double>,
            maxLabelWidth: Float
        ): List<Double> {
            val start = fullXRange.start
            val end = fullXRange.endInclusive
            val mid = (start + end) / 2
            return listOf(start, mid, end)
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
        email.value = DataStoreManager.getEmail().first() ?: ""
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

    LaunchedEffect(chartTrigger) {
        withContext(Dispatchers.IO) {
            // delay는 추후에 ANR이 발생하면 다시 활성화할 것!!
            delay(500)
            val userId = DataStoreManager.getUserId().first() ?: -1

            // db로부터 불러오기
            val localDBDataList =
                localDbRepository?.dataDao()?.getGlucoseList(userId = userId)
                    ?.sortedBy { it.createdAtLong }
                    ?: emptyList()

            val timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val chartSourceData =
                if (USE_PAST_GLUCOSE_DUMMY_DATA) {
                    (localDBDataList + buildPastGlucoseDummyData(userId, timeZone))
                        .sortedBy { it.createdAtLong }
                } else {
                    localDBDataList
                }

            val dateKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
                this.timeZone = timeZone
            }
            val titleFormatter = SimpleDateFormat("M월 d일", Locale.KOREA).apply {
                this.timeZone = timeZone
            }

            val groupedChartCards = chartSourceData
                .groupBy { dateKeyFormatter.format(Date(it.createdAtLong)) }
                .map { (dateKey, values) ->
                    val dayStartMillis = dateKeyFormatter.parse(dateKey)?.time ?: values.first().createdAtLong
                    PastGlucoseChartCardData(
                        dateKey = dateKey,
                        title = titleFormatter.format(Date(dayStartMillis)),
                        dayStartMillis = dayStartMillis,
                        glucoseList = values.sortedBy { it.createdAtLong }
                    )
                }
                .sortedBy { it.dateKey }

            Log.d("DB", "past chart card count : ${groupedChartCards.size}")

            val trendStatus = getTrendStatus(context, chartSourceData)
            val trendImageResource = when (trendStatus) {
                context.getString(R.string.glucose_trend_level_5) -> R.drawable.level5 // 급 상승
                context.getString(R.string.glucose_trend_level_4) -> R.drawable.level4 // 상승 중
                context.getString(R.string.glucose_trend_level_3) -> R.drawable.level3 // 유지 중
                context.getString(R.string.glucose_trend_level_2) -> R.drawable.level2 // 하강 중
                else -> R.drawable.level1//"급하강"
            }

            withContext(Dispatchers.Main) {
                delay(100)
                chartCards = groupedChartCards
                isLoading.value = true
                glucoseTrend.value = trendStatus
                glucoseTrendImgResource.value = trendImageResource
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
                                        Process.killProcess(Process.myPid())
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        val cardSlotHeight = maxHeight / 2

        if (isLoading.value && chartCards.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = chartCards,
                    key = { it.dateKey }
                ) { chartCardData ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardSlotHeight)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        PastGlucoseChartCard(
                            chartCardData = chartCardData,
                            selectedChartOption = selectedChartOption,
                            yMax = yMax,
                            currentYMax = currentYMax,
                            setYMax = setYMax,
                            forceRecompose = forceRecompose,
                            onForceRecompose = { forceRecompose++ },
                            isVip = GuestList.getVipList().contains(email.value),
                            email = email.value,
                            fontSize = fontSize,
                            customItemPlacer = customItemPlacer,
                            modifier = Modifier.fillMaxSize(),
                            context = context
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.chart_wait_a_moment),
                    color = Color(0xFF385DAB),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PastGlucoseChartCard(
    chartCardData: PastGlucoseChartCardData,
    selectedChartOption: String,
    yMax: Double,
    currentYMax: Double,
    setYMax: (Double) -> Unit,
    forceRecompose: Int,
    onForceRecompose: () -> Unit,
    isVip: Boolean,
    email: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    customItemPlacer: HorizontalAxis.ItemPlacer,
    modifier: Modifier = Modifier,
    context: Context
) {
    val modelProducer = remember(chartCardData.dateKey) { CartesianChartModelProducer() }
    val chartScrollSpec = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth
    )
    val rangeProvider = remember(yMax) {
        CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = yMax)
    }
    val chartPoints = remember(chartCardData, selectedChartOption) {
        val duplicated = chartCardData.glucoseList
            .groupBy {
                ((it.createdAtLong - chartCardData.dayStartMillis) / 1000 / 60)
            }
            .filter { it.value.size > 1 }

        Log.d("DUP", "${chartCardData.title} duplicated x: $duplicated")


        val chartX = mutableListOf<Number>()
        val chartY = mutableListOf<Number>()
        chartCardData.glucoseList.forEach { glucose ->
            val timeDiffMinutes =
                ((glucose.createdAtLong - chartCardData.dayStartMillis) / 1000 / 60).toDouble()
            chartX.add(timeDiffMinutes)
            chartY.add(
                when (selectedChartOption) {
                    context.getString(R.string.chart_option_glucose) -> glucose.glucose.toFloat()
                    context.getString(R.string.chart_option_weo1) -> glucose.weo1.toFloat()
                    else -> glucose.weo2.toFloat()
                }
            )
        }
        chartX to chartY
    }

    LaunchedEffect(chartPoints, selectedChartOption) {
        if (chartPoints.first.isNotEmpty() && chartPoints.second.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(chartPoints.first, chartPoints.second) }
            }
            delay(100)
            chartScrollSpec.animateScroll(Scroll.Absolute.End)
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chartCardData.title,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
                .weight(1f)
                .pointerInput(isVip, selectedChartOption, currentYMax) {
                    if (isVip) {
                        detectTapGestures(
                            onDoubleTap = {
                                val newMax =
                                    if (selectedChartOption == context.getString(R.string.chart_option_glucose)) {
                                        if (currentYMax == 250.0) 500.0 else 250.0
                                    } else {
                                        if (currentYMax == 250.0) {
                                            50.0
                                        } else if (currentYMax == 50.0) {
                                            10.0
                                        } else if (currentYMax == 10.0) {
                                            5.0
                                        } else {
                                            50.0
                                        }
                                    }
                                Log.d("TEST", "email : $email")
                                Log.d("TEST", "더블탭! old: $currentYMax -> $newMax")
                                setYMax(newMax)
                                onForceRecompose()
                            }
                        )
                    } else {
                        detectTapGestures(
                            onDoubleTap = {
                                Log.d("TEST", "I'm guest : $email")
                            }
                        )
                    }
                },
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val lineColor = Color(0xFF6FB0E5)
                val markerDecimalFormat =
                    when (selectedChartOption) {
                        context.getString(R.string.chart_option_glucose) -> DecimalFormat("# mg/dL")
                        else -> DecimalFormat("##.## nA")
                    }
                val yDecimalFormat = DecimalFormat("#")
                val startAxisValueFormatter = CartesianValueFormatter.decimal(yDecimalFormat)
                val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
                    val timeMillis = chartCardData.dayStartMillis + (value * 60 * 1000).toLong()
                    val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    formatter.format(Date(timeMillis))
                }
                val markerValueFormatter =
                    DefaultCartesianMarker.ValueFormatter.default(markerDecimalFormat)

                if (chartPoints.first.isNotEmpty() && chartPoints.second.isNotEmpty()) {
                    key(yMax, forceRecompose, chartCardData.dateKey) {
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(
                                    lineProvider =
                                        LineCartesianLayer.LineProvider.series(
                                            LineCartesianLayer.rememberLine(
                                                fill = LineCartesianLayer.LineFill.single(
                                                    fill(
                                                        lineColor
                                                    )
                                                ),
                                                areaFill =
                                                    LineCartesianLayer.AreaFill.single(
                                                        fill(
                                                            ShaderProvider.verticalGradient(
                                                                arrayOf(
                                                                    lineColor.copy(alpha = 0.8f),
                                                                    Color.Transparent
                                                                )
                                                            )
                                                        )
                                                    ),
                                                pointConnector = LineCartesianLayer.PointConnector.cubic(
                                                    curvature = 0.8f
                                                )
                                            )
                                        ),
                                    rangeProvider = rangeProvider
                                ),
                                startAxis = VerticalAxis.rememberStart(
                                    valueFormatter = startAxisValueFormatter,
                                    label = rememberAxisLabelComponent(color = Color.Black),
                                    itemPlacer = VerticalAxis.ItemPlacer.count({ 6 }),
                                ),
                                bottomAxis = HorizontalAxis.rememberBottom(
                                    valueFormatter = bottomAxisFormatter,
                                    label = rememberAxisLabelComponent(color = Color.Black),
                                    itemPlacer = customItemPlacer,
                                    labelRotationDegrees = 90f
                                ),
                                marker = rememberMarker(markerValueFormatter),
                                fadingEdges = FadingEdges(
                                    startWidthDp = 0f,
                                    endWidthDp = 0f,
                                    visibilityThresholdDp = 15f
                                ),
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            scrollState = chartScrollSpec,
                            zoomState = rememberVicoZoomState(
                                zoomEnabled = true,
                                initialZoom = Zoom.Content
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.chart_wait_a_moment),
                            color = Color(0xFF385DAB),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
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

private fun buildPastGlucoseDummyData(userId: Int, timeZone: TimeZone): List<UserGlucose> {
    val now = Calendar.getInstance(timeZone)
    val start = Calendar.getInstance(timeZone).apply {
        set(Calendar.YEAR, now.get(Calendar.YEAR))
        set(Calendar.MONTH, Calendar.JUNE)
        set(Calendar.DAY_OF_MONTH, 10)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (start.timeInMillis > now.timeInMillis) return emptyList()

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
        this.timeZone = timeZone
    }
    val result = mutableListOf<UserGlucose>()
    var currentTime = start.timeInMillis
    var index = 0

    while (currentTime <= now.timeInMillis) {
        val minutesFromStart = index * PAST_GLUCOSE_DUMMY_INTERVAL_MINUTES
        val dayWave = kotlin.math.sin(minutesFromStart / 180.0) * 28.0
        val mealWave = kotlin.math.max(0.0, kotlin.math.sin(minutesFromStart / 45.0)) * 18.0
        val glucose = (122.0 + dayWave + mealWave).coerceIn(70.0, 210.0)

        result.add(
            UserGlucose(
                userId = userId,
                glucose = glucose,
                weo1 = 2.0 + kotlin.math.sin(minutesFromStart / 90.0) * 0.35,
                weo2 = 1.8 + kotlin.math.cos(minutesFromStart / 120.0) * 0.28,
                createdAt = formatter.format(Date(currentTime)),
                createdAtLong = currentTime
            )
        )

        currentTime += PAST_GLUCOSE_DUMMY_INTERVAL_MINUTES * 60 * 1000L
        index++
    }

    return result
}
