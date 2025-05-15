package kr.co.uxn.agms_p.ui.components.main.home

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.component.shape.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import kotlinx.coroutines.CoroutineScope
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    homeViewModel: HomeViewModel,
    bleViewModel: BleViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val day by homeViewModel.day.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val totalEntryCount = remember { mutableStateOf(0) }

    val glucose by bleViewModel.glucose.collectAsState()
    val chartTrigger by bleViewModel.chartTrigger.collectAsState()

    var showCaliDialog =  bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog =  bleViewModel.showBleConnectDialog.collectAsState()
    var showModeDialog = remember { mutableStateOf(false) }
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()
    var selectedChartOption by remember { mutableStateOf("혈당") }

    val glucoseTrend = remember { mutableStateOf("유지 중") }
    val glucoseTrendImgResource = remember { mutableStateOf(R.drawable.level3) }

    var baseMinY by remember {mutableStateOf(0f)}
    var baseMaxY by remember {mutableStateOf(0f)}

    // Vico Chart
    val modelProducer = remember { ChartEntryModelProducer() }
    val dataSetForModel = remember { mutableStateListOf(listOf<FloatEntry>()) }
    val dataSetLineSpec = remember { arrayListOf<LineChart.LineSpec>() }
    val scrollState = rememberChartScrollState()

    val oldModel = remember { mutableStateOf(modelProducer.getModel()) } // ← 추가!
    val scrollSpec = rememberChartScrollSpec(
        initialScroll = InitialScroll.End,
        autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased
    )

    val isLoading = remember { mutableStateOf(false) }

    // Zoom 변수
    var zoomFactor by remember { mutableStateOf(1f) }
    baseMinY = when (selectedChartOption) {
        "혈당" -> 0f
        else -> 0f
    }
    baseMaxY = when (selectedChartOption) {
        "혈당" -> 250f
        else -> {
            if (zoomFactor == 1f) {
                50f
            } else if (zoomFactor == 2f) {
                10f
            } else {
                5f
            }
        }
    }

    val minY = baseMinY
    val maxY = baseMaxY

    val customItemPlacer = object : AxisItemPlacer.Horizontal {
        override fun getLabelValues(
            context: ChartDrawContext,
            visibleXRange: ClosedFloatingPointRange<Float>,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float> {
            val range = visibleXRange.endInclusive - visibleXRange.start
            val offset = range * 0.1f
//            val offset = range

//            val rawOffset = range * 0.1f
//            val offset = rawOffset.coerceIn(0.5f, 5f) // 최소 0.5, 최대 10으로 제한

            val start = visibleXRange.start + offset
            val end = visibleXRange.endInclusive - offset
            val mid = (start + end) / 2f
            return listOf(start, mid, end)
        }

        override fun getMeasuredLabelValues(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float> {
            // 측정 단계에서는 fullXRange를 기준으로 해야 함 (visibleXRange는 없음)
            val start = fullXRange.start
            val end = fullXRange.endInclusive
            val mid = (start + end) / 2f
            return listOf(start, mid, end)
        }

        override fun getStartHorizontalAxisInset(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
//        ): Float = context.dpToPx(30f)
        ): Float = 0f

        override fun getEndHorizontalAxisInset(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float = 0f
//        ): Float = context.dpToPx(24f)

    }



    // RadioButton
    var selectedOption by remember { mutableStateOf("3시간") }

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val fontSize = when {
        screenHeightDp == 783 -> 17.sp // a시리즈
        else -> 16.sp
    }



    LaunchedEffect(selectedOption, selectedChartOption) {
        withContext(Dispatchers.IO) {
            dataSetForModel.clear()
//            dataSetLineSpec.clear()
            val dataPoints = arrayListOf<FloatEntry>()

            // 차트 디자인 옵션
            dataSetLineSpec.add(
                LineChart.LineSpec(
                    lineColor = Color(0xFF6FB0E5).toArgb(),
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF6FB0E5).copy(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                                Color(0xFF6FB0E5).copy(DefaultAlpha.LINE_BACKGROUND_SHADER_START)
                            )
                        )
                    )
                )
            )

            val userId = DataStoreManager.getUserId().first() ?: -1

            Log.e("TEST", "selectedOption : ${selectedOption}")
            val lastTime = when (selectedOption) {
                "3시간" -> System.currentTimeMillis() - (3 * 60 * 60 * 1000L)
                "6시간" -> System.currentTimeMillis() - (6 * 60 * 60 * 1000L)
                else -> System.currentTimeMillis() - (12 * 60 * 60 * 1000L)
            }
            Log.e("DB", "lastTime : ${lastTime}")
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val convertedLastTime = formatter.format(Date(lastTime))

            Log.e("DB", "converted : ${convertedLastTime}")

            val localDBDataListAfterLastTime =
                localDbRepository?.dataDao()
                    ?.getGlucoseListAfterLastTime(userId = userId, lastTime = lastTime)

            if (localDBDataListAfterLastTime != null) {
                totalEntryCount.value = localDBDataListAfterLastTime.size
            }

            Log.d("TEST", "localDbList : ${localDBDataListAfterLastTime}")

            val baseTime = 1743442800000L // 25년 4월 1일 00시 00분 00초
            for (i in 0 until localDBDataListAfterLastTime!!.size) {
                val timeDiffMillis = localDBDataListAfterLastTime[i].createdAtLong - baseTime
                val timeDiffMinutes =
                    (timeDiffMillis / 1000 / 60).toFloat()  // millis → seconds → minutes

//                Log.d("TEST", "timeDiffMinutes : ${timeDiffMinutes}")
                when(selectedChartOption) {
                    "혈당" -> {
                        dataPoints.add(
                            FloatEntry(
                                x = (timeDiffMinutes),
                                y = localDBDataListAfterLastTime[i].glucose.toFloat()
                            )
                        )
                    }
                    "WEO1" -> {
                        dataPoints.add(
                            FloatEntry(
                                x = (timeDiffMinutes),
                                y = localDBDataListAfterLastTime[i].weo1.toFloat()
                            )
                        )
                    }
                    else -> {
                        dataPoints.add(
                            FloatEntry(
                                x = (timeDiffMinutes),
                                y = localDBDataListAfterLastTime[i].weo2.toFloat()
                            )
                        )
                    }
                }
            }

            dataSetForModel.add(dataPoints)

            withContext(Dispatchers.Main) {

                delay(100)
                modelProducer.setEntries(dataSetForModel)

//                oldModel.value = modelProducer.getModel()

                scrollSpec.performAutoScroll(
                    model = modelProducer.getModel(),
                    oldModel = oldModel.value,
                    chartScrollState = scrollState
                )

                isLoading.value = true
                delay(100)
                if (!scrollState.isScrollInProgress) {
                    Log.e("TEST", "scrollState.isScrollInProgress : ${scrollState.isScrollInProgress}")
                    scrollState.scroll(MutatePriority.Default) {
                        // 강제로 끝까지 스크롤
                        scrollBy(scrollState.maxValue)
                    }
                } else {
                    Log.e("TEST", "scrollState.isScrollInProgress : ${scrollState.isScrollInProgress}")
                }
            }
        }
    }


    LaunchedEffect(chartTrigger) {
        withContext(Dispatchers.IO) {
            // delay는 추후에 ANR이 발생하면 다시 활성화할 것!!
//            delay(2000)
            delay(500)
            dataSetForModel.clear()
//            dataSetLineSpec.clear()
            val dataPoints = arrayListOf<FloatEntry>()

            withContext(Dispatchers.Main) {
                oldModel.value = modelProducer.getModel()
            }
            // 차트 디자인 옵션
            dataSetLineSpec.add(
                LineChart.LineSpec(
                    lineColor = Color(0xFF6FB0E5).toArgb(),
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF6FB0E5).copy(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                                Color(0xFF6FB0E5).copy(DefaultAlpha.LINE_BACKGROUND_SHADER_START)
                            )
                        )
                    )
                )
            )

            val userId = DataStoreManager.getUserId().first() ?: -1

            Log.e("TEST", "selectedOption : ${selectedOption}")
            val lastTime = when (selectedOption) {
                "3시간" -> System.currentTimeMillis() - (3 * 60 * 60 * 1000L)
                "6시간" -> System.currentTimeMillis() - (6 * 60 * 60 * 1000L)
                else -> System.currentTimeMillis() - (12 * 60 * 60 * 1000L)
            }
            Log.e("DB", "lastTime : ${lastTime}")
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val convertedLastTime = formatter.format(Date(lastTime))

            Log.e("DB", "converted : ${convertedLastTime}")

            // db로부터 불러오기
            val localDBDataListAfterLastTime =
                localDbRepository?.dataDao()
                    ?.getGlucoseListAfterLastTime(userId = userId, lastTime = lastTime)

            if (localDBDataListAfterLastTime != null) {
                totalEntryCount.value = localDBDataListAfterLastTime.size
            }

            val baseTime = 1743442800000L // 25년 4월 1일 00시 00분 00초
            for (i in 0 until localDBDataListAfterLastTime!!.size) {
                val timeDiffMillis = localDBDataListAfterLastTime[i].createdAtLong - baseTime
                val timeDiffMinutes =
                    (timeDiffMillis / 1000 / 60).toFloat()  // millis → seconds → minutes

//                Log.d("TEST", "timeDiffMinutes : ${timeDiffMinutes}")

                when (selectedChartOption) {
                    "혈당" -> {
                        dataPoints.add(
                            FloatEntry(
                                x = (timeDiffMinutes),
                                y = localDBDataListAfterLastTime[i].glucose.toFloat()
                            )
                        )
                    }
                    "WEO1" -> {
                        dataPoints.add(
                            FloatEntry(
                                x = (timeDiffMinutes),
                                y = localDBDataListAfterLastTime[i].weo1.toFloat()
                            )
                        )
                    }
                    else -> {
                        dataPoints.add(
                            FloatEntry(
                                x = (timeDiffMinutes),
                                y = localDBDataListAfterLastTime[i].weo2.toFloat()
                            )
                        )
                    }
                }
            }



            Log.e("DB", "localDBDataListAfterLastTime : ${localDBDataListAfterLastTime}")

            Log.d("CHART", "dataPoints size: ${dataPoints.size}")

            dataSetForModel.add(dataPoints)

            withContext(Dispatchers.Main) {

                delay(100)
                modelProducer.setEntries(dataSetForModel)

//                oldModel.value = modelProducer.getModel()

                scrollSpec.performAutoScroll(
                    model = modelProducer.getModel(),
                    oldModel = oldModel.value,
                    chartScrollState = scrollState
                )

                isLoading.value = true
                delay(100)
                if (!scrollState.isScrollInProgress) {
                    Log.e("TEST", "scrollState.isScrollInProgress : ${scrollState.isScrollInProgress}")
                    scrollState.scroll(MutatePriority.Default) {
                        // 강제로 끝까지 스크롤
                        scrollBy(scrollState.maxValue)
                    }
                } else {
                    Log.e("TEST", "scrollState.isScrollInProgress : ${scrollState.isScrollInProgress}")
                }
            }

            // 추세 변화 알고리즘
            glucoseTrend.value = getTrendStatus(localDBDataListAfterLastTime)

            glucoseTrendImgResource.value = when (glucoseTrend.value) {
                "급상승" -> R.drawable.level5
                "상승 중" -> R.drawable.level4
                "유지 중" -> R.drawable.level3
                "하강 중" -> R.drawable.level2
                else -> R.drawable.level1//"급하강"
            }
        }
    }

    // 실제 타이머
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // onResume 시점에만 실행!
                homeViewModel.startTimer()
                Log.e("TEST", "홈 화면에서 타이머 실행")
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 1. 노티 : 혈당입력
    if (showCaliDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showCaliDialog(false) },
            onConfirm = {
                BleBridge.showCaliDialog(false)
                navController.navigate("GlucoseRegisterScreen")
            },
            title = "혈당 입력 시간입니다",
            content = "정확한 측정을 위해 공복 상태에서 자가 채혈한 혈당을 입력해주세요.",
        )
    }

    // 2. 노티 : BLE 끊김
    if (showBleConnectDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
            },
            title = "블루투스 연결이 끊어졌습니다",
            content = "센서와의 연결이 일시적으로 끊어졌어요\n스마트폰을 가까이 두고 앱을 다시 실행해보세요",
        )
    }

    // 3. 그래프 모드 다이얼로그
    if (showModeDialog.value) {
        ModeDialog(
            options = listOf("혈당", "WEO1", "WEO2"),
            selectedOption = selectedChartOption,
            onOptionSelected =
                {
                    showModeDialog.value = false
                    selectedChartOption = it
                },
            onDismissRequest = { showModeDialog.value = false}
        )
    }

    // 4. 측정 종료 다이얼로그
    if (showEndMeasurementDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager.saveIsMain(false)
                    DataStoreManager.deleteRoute()
                    DataStoreManager.saveRoute("Splash")
                    Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                    Log.e("TEST", "DS에 저장된 Route는${DataStoreManager.getRoute().first()}")
                    DataStoreManager.deleteAccessToken()
                    DataStoreManager.deleteRefreshToken()
                    DataStoreManager.deleteUserId()
                    DataStoreManager.deleteDeviceMac()
                    DataStoreManager.deleteStartTime()
                    DataStoreManager.deleteEndTime()

                    delay(500)
                    // 1. 서비스 종료
//                            stopSelf()
                    // 앱 강제종료
                    android.os.Process.killProcess(android.os.Process.myPid())
                    exitProcess(0)
                }
            },
            title = "센서의 사용 기간이 종료되었습니다.",
            content = "센서의 사용 기간이 만료되어 더 이상 측정이 불가합니다. 새 센서를 연결해주세요.",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        // 1. 혈당 표시 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(10.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            // 롱클릭 시 실행할 코드
                            Log.d("TEST", "롱클릭됨!")
                            showModeDialog.value = true

                        },
                        onTap = {
                            // 짧은 클릭 (탭) 시 실행할 코드 (선택사항)
//                            Log.d("TEST", "터치됨!")
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF385DAB), // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "현재 혈당",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 20.dp)
                )
//                Image(
//                    modifier = Modifier
//                        .padding(start = 5.dp)
//                        .size(17.dp),
//                    painter = painterResource(R.drawable.glucose_reset),
//                    contentDescription = "glucoseReset"
//                )
            }

            // Box로 수정
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(start = 30.dp, end = 40.dp, top = 5.dp)
            ) {
                Spacer(modifier = Modifier.width(30.dp))
                Text(

                    text = if (glucose == 0) {
                        "_ _ _"
                    } else {
                        "${glucose}"
                    },
                    color = Color.White,
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart),
                )

                Text(
                    text = "mg/dL",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 10.dp, bottom = 25.dp)
                )

                Image(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.TopEnd),
//                    painter = painterResource(R.drawable.level3),
                    painter = painterResource(glucoseTrendImgResource.value),
                    contentDescription = "glucose_trend_img"
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 15.dp),
                    text = glucoseTrend.value,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

            }
        }


        // 2. 그래프 표시 카드
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
            Spacer(modifier = Modifier.height(10.dp))

            val mode = when (selectedChartOption) {
                "혈당" -> "혈당 그래프"
                "WEO1" -> "WEO1 그래프"
                else -> "WEO2 그래프"
            }


            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )

                if (selectedChartOption != "혈당") {

                        Spacer(modifier = Modifier.width(50.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Image(
                                painter = painterResource(R.drawable.zoom_in),
                                contentDescription = "확대 줌",
                                modifier = Modifier.size(20.dp)
                                    .clickable {
                                        if (zoomFactor < 3){
                                            zoomFactor += 1f
                                        }
//                                        zoomFactor = zoomFactor.coerceIn(1f, 10f)
                                    }
                            )

                            Image(
                                painter = painterResource(R.drawable.zoom_out),
                                contentDescription = "축소 줌",
                                modifier = Modifier.size(20.dp)
                                    .clickable {
                                        if (zoomFactor > 1){
                                            zoomFactor -= 1f
                                        }
//                                        zoomFactor = zoomFactor.coerceIn(1f, 10f)
                                    }
                            )

                            Image(
                                painter = painterResource(R.drawable.zoom_reset),
                                contentDescription = "리셋 줌",
                                modifier = Modifier.size(20.dp)
                                    .clickable {
                                        zoomFactor = 1f
                                    }
                            )
                        }
                    }
                }

            // VICO 그래프
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
//                    .pointerInput(Unit) {
//                        detectTransformGestures { _, _, zoom, _ ->
//                            zoomFactor *= zoom
//                            zoomFactor = zoomFactor.coerceIn(1f, 10f)
//                        }
//                    }

//                    .pointerInput(Unit) {
//                        forEachGesture {
//                            awaitPointerEventScope {
//                                val down = awaitFirstDown(requireUnconsumed = false)
//                                var zooming = false
//                                var initialDistance = 0f
//
//                                do {
//                                    val event = awaitPointerEvent()
//                                    val pointers = event.changes
//
//                                    if (pointers.size == 2) {
//                                        val distance = (pointers[0].position - pointers[1].position).getDistance()
//
//                                        if (!zooming) {
//                                            zooming = true
//                                            initialDistance = distance
//                                        } else {
//                                            val zoom = distance / initialDistance
//                                            zoomFactor *= zoom
//                                            zoomFactor = zoomFactor.coerceIn(1f, 10f)
//                                            initialDistance = distance
//                                        }
//
//                                        pointers.forEach { it.consume() }
//                                    }
//                                } while (event.changes.any { it.pressed })
//                            }
//                        }
//                    }
                    .weight(1f),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dataSetForModel.isNotEmpty() && isLoading.value == true) {
                        ProvideChartStyle {
                            val marker = rememberMarker()
                            if (totalEntryCount.value < 5) {
                                Chart(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    chart = lineChart(
                                        lines = dataSetLineSpec,
                                        axisValuesOverrider = AxisValuesOverrider.fixed(
                                            minY = minY,
                                            maxY = maxY
                                        )
                                    ),
                                    chartModelProducer = modelProducer,
                                    chartScrollState = scrollState,
                                    chartScrollSpec = scrollSpec,
                                    // y축
                                    startAxis = rememberStartAxis(
                                        title = "Top values",
                                        tickLength = 0.dp,
                                        valueFormatter = { value, _ ->
                                            value.toInt().toString()
                                        },
                                        label = axisLabelComponent(color = Color.Black),
                                        // y축 레이블 갯수
                                        itemPlacer = AxisItemPlacer.Vertical.default(
                                            maxItemCount = 6,
                                            shiftTopLines = true
                                        )
                                    ),
                                    marker = marker,
                                    isZoomEnabled = true,

                                    // x축
                                    bottomAxis = rememberBottomAxis(
                                        title = "Count of values",
                                        tickLength = 0.dp,
                                        valueFormatter = { value, _ ->
                                            val baseTime = 1743442801000L // 25년 4월 1일 00시 00분 00초
                                            val actualTimeMillis = baseTime + (value * 60 * 1000).toLong()
                                            val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                                            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                            formatter.format(Date(actualTimeMillis))
                                        },
                                        itemPlacer = AxisItemPlacer.Horizontal.default(
                                            spacing = 1,  // x축 라벨 간격을 더 촘촘히 (기본은 자동)
                                            shiftExtremeTicks = true

                                        ),
                                        label = axisLabelComponent(
                                            color = Color.Black,
//                                            background = ShapeComponent(color = R.color.teal_200)
                                        ),
                                        guideline = null,
                                    ),
                                )
                            } else {
                                Chart(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    chart = lineChart(
                                        lines = dataSetLineSpec,
                                        axisValuesOverrider = AxisValuesOverrider.fixed(
                                            minY = minY,
                                            maxY = maxY
                                        )
                                    ),
                                    chartModelProducer = modelProducer,
                                    chartScrollState = scrollState,
                                    chartScrollSpec = scrollSpec,

                                    // y축
                                    startAxis = rememberStartAxis(
                                        title = "Top values",
                                        tickLength = 0.dp,
                                        valueFormatter = { value, _ ->
                                            value.toInt().toString()
                                        },
                                        label = axisLabelComponent(color = Color.Black),
                                        // y축 레이블 갯수
                                        itemPlacer = AxisItemPlacer.Vertical.default(
                                            maxItemCount = 6,
                                            shiftTopLines = true
                                        )
                                    ),
                                    marker = marker,
                                    isZoomEnabled = true,

                                    // x축
                                    bottomAxis = rememberBottomAxis(
                                        title = "Count of values",
                                        tickLength = 0.dp,
                                        itemPlacer = customItemPlacer,
                                        valueFormatter = { value, _ ->
                                            val baseTime = 1743442801000L
                                            val actualTimeMillis = baseTime + (value * 60 * 1000).toLong()
                                            val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                                            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                            formatter.format(Date(actualTimeMillis))
                                        },
                                        label = axisLabelComponent(
                                            color = Color.Black,
//                                            horizontalMargin = 20.dp,
                                            textSize = 10.sp,
                                            background = ShapeComponent(color = R.color.teal_200),
                                            ellipsize = TextUtils.TruncateAt.START,
                                        ),
                                        guideline = null,
                                    )
                                )
                            }

                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "",
                                color = Color(0xFF385DAB),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    RadioButtonSingleSelection(
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it }
                    )
                }
            }
        }

        // 3. 센서 정보 표시 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 10.dp)
                .padding(top = 10.dp, bottom = 20.dp)
            ,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White, // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(top = 10.dp, start = 15.dp, end = 10.dp),
                    .padding(top = 10.dp, start = 20.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "센서 정보",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "더보기 아이콘",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            navController.navigate("SensorInfoScreen")
                        }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 14) {
                    val painter = if (i < 14 - day + 1) {
                        R.drawable.sensor_progress_on
                    } else {
                        R.drawable.sensor_progress_off
                    }
                    Image(
                        painter = painterResource(painter),
                        contentDescription = "센서 진행률",
//                        modifier = Modifier.size(15.dp, 10.dp)
                        modifier = Modifier.size(19.dp, 10.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if ((10 - day + 1) < 11) {
                    Text(
//                        text = "${10 - day + 1}/10일",
                        text = "${14 - day + 1}/14일",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
//                        text = "10/10일",
                        text = "14/14일",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (day - 1 > 0) {
                    Text(
                        text = "${day - 1}일 남았어요",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "마지막 날이에요",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun RadioButtonSingleSelection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val radioOptions = listOf("3시간", "6시간", "12시간")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .height(60.dp)
            .padding(horizontal = 30.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

fun getTrendStatus(glucoseValueList: List<UserGlucose>): String {
    if (glucoseValueList.size < 4) return "유지 중"

    val list = glucoseValueList.takeLast(5)

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
        slope >= 10.0 -> "급상승"
        slope in 2.1..4.9 -> "상승 중"
        slope in -2.0..2.0 -> "유지 중"
        slope in -4.9..-2.1 -> "하강 중"
        slope <= -10.0 -> "급하강"
        else -> "유지 중"
    }
}


