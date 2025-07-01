package kr.co.uxn.agms_p.ui.components.main.home

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
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
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.system.exitProcess
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.core.app.NotificationManagerCompat
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import kr.co.uxn.agms_p.GuestList
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog

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
    val day by homeViewModel.day.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val totalEntryCount = remember { mutableStateOf(0) }

    val glucose by bleViewModel.glucose.collectAsState()
    val chartTrigger by bleViewModel.chartTrigger.collectAsState()

    var showCaliDialog = bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog = bleViewModel.showBleConnectDialog.collectAsState()
    var showBluetoothOnDialog = bleViewModel.showBluetoothOnDialog.collectAsState()
    var showLowGlucoseDialog = bleViewModel.showLowGlucoseDialog.collectAsState()
    var showHighGlucoseDialog = bleViewModel.showHighGlucoseDialog.collectAsState()
    var showModeDialog = remember { mutableStateOf(false) }
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()
    var selectedChartOption by remember { mutableStateOf("혈당") }

    val glucoseTrend = remember { mutableStateOf("유지 중") }
    val glucoseTrendImgResource = remember { mutableStateOf(R.drawable.level3) }

    var email = ""

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
    val rangeProvider = remember (yMax) {
        CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = yMax)
    }

    val x = remember { mutableListOf<Number>() }
    val y = remember { mutableListOf<Number>() }

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

    // RadioButton
    var selectedTimeOption by remember { mutableStateOf("6시간") }

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
        email = DataStoreManager.getEmail().first()?: ""
    }

    LaunchedEffect(Unit) {
        val verifiedDSLandscapeMode = DataStoreManager.getLandScapeMode().first() ?: false
        checkedForLandscapeMode.value = verifiedDSLandscapeMode
    }

    // 시간 옵션, 차트 옵션 변경 시 줌 리셋
    LaunchedEffect(selectedTimeOption) {
        forceRecompose++
    }

    LaunchedEffect(selectedChartOption) {
        when (selectedChartOption) {
            "혈당" -> setYMax(250.0)
            "WEO1", "WEO2" -> setYMax(50.0) // 시작은 50.0, 필요시 5.0/10.0 등으로 조정
        }
        forceRecompose++
    }

    LaunchedEffect(chartTrigger, selectedTimeOption, selectedChartOption) {
        withContext(Dispatchers.IO) {
            // delay는 추후에 ANR이 발생하면 다시 활성화할 것!!
            delay(500)
            x.clear()
            y.clear()
            val userId = DataStoreManager.getUserId().first() ?: -1

            Log.e("TEST", "selectedOption : ${selectedTimeOption}")
            val lastTime = when (selectedTimeOption) {
                "6시간" -> System.currentTimeMillis() - (6 * 60 * 60 * 1000L)
                "12시간" -> System.currentTimeMillis() - (12 * 60 * 60 * 1000L)
                else -> System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            }
            Log.e("DB", "lastTime : ${lastTime}")
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val convertedLastTime = formatter.format(Date(lastTime))

            Log.e("DB", "converted : ${convertedLastTime}")

            // db로부터 불러오기
            val localDBDataListAfterLastTime =
                localDbRepository?.dataDao()
                    ?.getGlucoseListAfterLastTime(userId = userId, lastTime = lastTime)?.toMutableList()

            if (localDBDataListAfterLastTime != null) {
                totalEntryCount.value = localDBDataListAfterLastTime.size
            }

            when (selectedTimeOption) {
                "6시간" -> {
                    if (totalEntryCount.value < 360 && totalEntryCount.value > 0) {
                        val str = localDBDataListAfterLastTime?.first()?.createdAt
                        val lastTimeLong = localDBDataListAfterLastTime?.first()?.createdAtLong!!
                        Log.e("TEST", "first str : ${str}, last str : ${localDBDataListAfterLastTime.last().createdAt}")

                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                        val lastCount = (360 + 0) - totalEntryCount.value
//                        Log.e("TEST", "추출한 minute : ${minute}")
                        if (!localDBDataListAfterLastTime.isNullOrEmpty()) {
                            for( i in 1 .. lastCount) {
                                val time = lastTimeLong - (1000L * 60 * i)
                                val convertedTime = formatter.format(Date(time))
                                localDBDataListAfterLastTime.add(UserGlucose(userId = userId, glucose = 0.0, weo1 = 0.0, weo2 = 0.0, createdAt = convertedTime, createdAtLong = time)
                                )
                            }
                        }
                        localDBDataListAfterLastTime.sortedBy { it.createdAtLong }
//                        localDBDataListAfterLastTime.sortedByDescending { it.createdAtLong }
                        Log.d("TEST", "localDBDataListAfterLastTime size: ${localDBDataListAfterLastTime.size}")
                        Log.d("TEST", "localDBDataListAfterLastTime first : ${localDBDataListAfterLastTime.first()}, localDBDataListAfterLastTime last : ${localDBDataListAfterLastTime.last()}")
                    }
                }
                "12시간" -> {
                    if (totalEntryCount.value < 720 && totalEntryCount.value > 0) {
                        val str = localDBDataListAfterLastTime?.first()?.createdAt
                        val lastTimeLong = localDBDataListAfterLastTime?.first()?.createdAtLong!!
                        Log.e("TEST", "first str : ${str}, last str : ${localDBDataListAfterLastTime.last().createdAt}")


                        val lastCount = 720 - totalEntryCount.value
                        if (!localDBDataListAfterLastTime.isNullOrEmpty()) {
                            for( i in 1 .. lastCount) {
                                val time = lastTimeLong - (1000L * 60 * i)
                                localDBDataListAfterLastTime.add(
                                    UserGlucose(userId = userId, glucose = 0.0, weo1 = 0.0, weo2 = 0.0, createdAt = "I'm dummy!", createdAtLong = time)
                                )
                            }
                        }

                        localDBDataListAfterLastTime.sortedBy { it.createdAtLong }
                        Log.d("TEST", "localDBDataListAfterLastTime : ${localDBDataListAfterLastTime}")
                    }
                }
                else -> {
                    if (totalEntryCount.value < 1440 && totalEntryCount.value > 0) {
                        val str = localDBDataListAfterLastTime?.first()?.createdAt
                        val lastTimeLong = localDBDataListAfterLastTime?.first()?.createdAtLong!!
                        Log.d("TEST", "first str : ${str}, last str : ${localDBDataListAfterLastTime.last().createdAt}")

                        val lastCount = 1440 - totalEntryCount.value
                        if (!localDBDataListAfterLastTime.isNullOrEmpty()) {
                            for( i in 1 .. lastCount) {
                                val time = lastTimeLong - (1000L * 60 * i)
                                localDBDataListAfterLastTime.add(
                                    UserGlucose(userId = userId, glucose = 0.0, weo1 = 0.0, weo2 = 0.0, createdAt = "I'm dummy!", createdAtLong = time)
                                )
                            }
                        }

                        localDBDataListAfterLastTime.sortedBy { it.createdAtLong }
                        Log.d("TEST", "localDBDataListAfterLastTime : ${localDBDataListAfterLastTime}")

                    }
                }
            }

            val baseTime = 1743442800000L // 25년 4월 1일 00시 00분 00초
            for (i in 0 until localDBDataListAfterLastTime!!.size) {
                val timeDiffMillis = localDBDataListAfterLastTime[i].createdAtLong - baseTime
                val timeDiffMinutes =
                    (timeDiffMillis / 1000 / 60).toDouble() // millis → seconds → minutes
                when (selectedChartOption) {
                    "혈당" -> {
                        x.add(
                            timeDiffMinutes
                        )
                        y.add(
                            localDBDataListAfterLastTime[i].glucose.toFloat()
                        )
                    }

                    "WEO1" -> {
                        x.add(
                            timeDiffMinutes
                        )
                        y.add(
                            localDBDataListAfterLastTime[i].weo1.toFloat()
                        )
                    }

                    else -> {
                        x.add(
                            timeDiffMinutes
                        )
                        y.add(
                            localDBDataListAfterLastTime[i].weo2.toFloat()
                        )
                    }
                }
            }

            Log.d("DB", "localDBDataListAfterLastTime : ${localDBDataListAfterLastTime}")
            Log.d("TEST", "x : ${x}  y : ${y.size}")

            withContext(Dispatchers.Main) {
                delay(100)
//                modelProducer.setEntries(dataSetForModel)

                if (x.isNotEmpty() && y.isNotEmpty()) {
                    modelProducer.runTransaction {
                        lineSeries { series(x, y) }
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
            title = "혈당 입력 시간입니다.",
            content = "정확한 측정을 위해 공복 상태에서 자가 채혈한 혈당을 입력해주세요.",
        )
    }

    // 2. Dialog : BLE 끊김
    if (showBleConnectDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
            },
            title = "블루투스 연결이 끊어졌습니다.",
            content = "센서와의 연결이 일시적으로 끊어졌어요.\n스마트폰을 가까이 두고 연결 상태를 확인하세요.",
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
            title = "블루투스가 꺼져있습니다.",
            content = "블루투스를 켜고 연결 상태를 확인하세요.",
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
                                    DataStoreManager.setLandScapeMode(false)
                                    DataStoreManager.deleteTargetLowGlucose()
                                    DataStoreManager.deleteTargetHighGlucose()
                                    DataStoreManager.deleteEmail()
                                    withContext(Dispatchers.Main) {
                                        // 1. 서비스 종료
                                        bleViewModel.emit("STOP_SERVICE")
                                        // 앱 강제종료
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
                            Toast.makeText(context, "네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            title = "센서의 사용 기간이 종료되었습니다.",
            content = "센서의 사용 기간이 만료되어 더 이상 측정이 불가합니다. 새 센서를 연결해주세요.",
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
            title = "혈당수치가 낮습니다.",
            content = "저혈당 위험이 있어요. 필요시 조치를 취하고, 안정 후 수치를 다시 확인하세요.",
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
            title = "혈당수치가 높습니다.",
            content = "현재 혈당이 혈당 범위를 초과했어요. 식사나 활동 내용을 확인하세요.",
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
//                .weight(1f)
                .pointerInput(Unit) {
                    if (GuestList.getGuestList().contains(email)) {
                        detectTapGestures(
                            onLongPress = {
                                Log.d("TEST", "I'm guest : ${email}")
                            }
                        )
                    } else {
                        detectTapGestures(
                            onLongPress = {
                                // 롱클릭 시 실행할 코드
                                Log.d("TEST", "롱 클릭됨!")
                                showModeDialog.value = true
                            }
                        )
                    }
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

        // 가로 모드
        if (checkedForLandscapeMode.value) {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val screenHeight = configuration.screenHeightDp.dp
                // 2. 그래프 표시 카드 90도
            Box(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        if (GuestList.getGuestList().contains(email)) {
                            detectTapGestures(
                                onLongPress = {
                                    Log.d("TEST", "I'm guest : ${email}")
                                }
                            )
                        } else {
                            detectTapGestures(
                                onDoubleTap = {
                                    val newMax = if(selectedChartOption == "혈당" ) {
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
                                    Log.d("TEST", "더블탭! old: $currentYMax -> $newMax")
                                    setYMax(newMax)
                                    forceRecompose++
                                }
                            )
                        }
                    },
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 10.dp)
                    ,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White, // 카드 배경색 설정
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    // VICO 그래프
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 30.dp, bottom = 100.dp)
                            .rotate(90f)
                            .background(Color.Transparent)
                    ) {
                        Column() {
                            val lineColor = Color(0xFF6FB0E5)
                            val markerDecimalFormat =
                                when (selectedChartOption) {
                                    "혈당" -> DecimalFormat("# mg/dL")
                                    else -> DecimalFormat("##.## nA")
                                }
                            val yDecimalFormat = DecimalFormat("#")
                            val startAxisValueFormatter =
                                CartesianValueFormatter.decimal(yDecimalFormat)
                            val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
                                val baseTime = 1743442801000L
                                val timeMillis = baseTime + (value * 60 * 1000).toLong()
                                val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                                formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                formatter.format(Date(timeMillis))
                            }
                            val MarkerValueFormatter =
                                DefaultCartesianMarker.ValueFormatter.default(markerDecimalFormat)


                            if (isLoading.value == true && x.isNotEmpty() && y.isNotEmpty()) {
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
                                            itemPlacer = VerticalAxis.ItemPlacer.count({ 6 })
                                        ),
                                        bottomAxis = HorizontalAxis.rememberBottom(
                                            valueFormatter = bottomAxisFormatter,
                                            itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                                                spacing = { 20 }, // 5개의 xStep마다 하나의 라벨
                                                offset = { 0 },
                                                shiftExtremeLines = true,
                                                addExtremeLabelPadding = true
                                            )
//                                            itemPlacer = customItemPlacer,
//                                            labelRotationDegrees = 90f
                                        ),
                                        marker = rememberMarker(MarkerValueFormatter),
                                        fadingEdges = FadingEdges( // 또는 FadingEdges.horizontal() 도 가능
                                            startWidthDp = 0f,
                                            endWidthDp = 0f,
                                            visibilityThresholdDp = 15f
                                        )
                                    ),
                                    modelProducer = modelProducer,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    scrollState = chartScrollSpec,
                                    zoomState = rememberVicoZoomState(
                                        zoomEnabled = true,
                                        initialZoom = Zoom.Content
                                    )
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "잠시만 기다려주세요...",
                                        color = Color(0xFF385DAB),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                        } // column


                    } // surface

                } // Card (Column)
                RadioButtonSingleSelection(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    selectedOption = selectedTimeOption,
                    onOptionSelected = { selectedTimeOption = it }
                )
            } // Box
        } else { // 세로 모드
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mode,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize
                    )
                }

                // VICO 그래프
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    val newMax = if(selectedChartOption == "혈당" ) {
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
                                    Log.d("TEST", "더블탭! old: $currentYMax -> $newMax")
                                    setYMax(newMax)
                                    forceRecompose++
                                },
                                onTap = {
                                },
                                onLongPress = {
                                }
                            )
                        },
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val lineColor = Color(0xFF6FB0E5)
                        val markerDecimalFormat =
                            when (selectedChartOption) {
                                "혈당" -> DecimalFormat("# mg/dL")
                                else -> DecimalFormat("##.## nA")
                            }
                        val yDecimalFormat = DecimalFormat("#")
                        val startAxisValueFormatter = CartesianValueFormatter.decimal(yDecimalFormat)
                        val bottomAxisFormatter = CartesianValueFormatter { _, value, _ ->
                            val baseTime = 1743442801000L
                            val timeMillis = baseTime + (value * 60 * 1000).toLong()
                            val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
                            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            formatter.format(Date(timeMillis))
                        }
                        val MarkerValueFormatter =
                            DefaultCartesianMarker.ValueFormatter.default(markerDecimalFormat)

                        if (isLoading.value == true && x.isNotEmpty() && y.isNotEmpty()) {
                            key(yMax, forceRecompose) {
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
                                            itemPlacer = if (yMax == 500.0) {
                                            VerticalAxis.ItemPlacer.count({ 6 })
                                            } else {
                                            VerticalAxis.ItemPlacer.count({ 6 })
                                            },
//                                            guideline = null
                                        ),
                                        bottomAxis = HorizontalAxis.rememberBottom(
                                            valueFormatter = bottomAxisFormatter,
//                                            itemPlacer = HorizontalAxis.ItemPlacer.aligned(
//                                                spacing = { 20 }, // 5개의 xStep마다 하나의 라벨
//                                                offset = { 2 },
//                                                shiftExtremeLines = true,
//                                                addExtremeLabelPadding = true
//                                            ),
//                                            itemPlacer = HorizontalAxis.ItemPlacer.segmented(false),
                                            itemPlacer = customItemPlacer,
//                                            guideline = null
                                            labelRotationDegrees = 90f
                                        ),
                                        marker = rememberMarker(MarkerValueFormatter),
                                        fadingEdges = FadingEdges( // 또는 FadingEdges.horizontal() 도 가능
                                            startWidthDp = 0f,
                                            endWidthDp = 0f,
                                            visibilityThresholdDp = 15f
                                        ),
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
                                    zoomState = rememberVicoZoomState(
                                        zoomEnabled = true,
                                        initialZoom = Zoom.Content
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
                                    text = "잠시만 기다려주세요...",
                                    color = Color(0xFF385DAB),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        RadioButtonSingleSelection(
                            selectedOption = selectedTimeOption,
                            onOptionSelected = { selectedTimeOption = it }
                        )
                    } // column
                } // surface
            }

            // 3. 센서 정보 표시 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 10.dp)
                    .padding(top = 10.dp, bottom = 20.dp),
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
                        text = "남은 사용 기간",
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

                    Spacer(modifier = Modifier.weight(1f))
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
}

@Composable
fun RadioButtonSingleSelection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val radioOptions = listOf("6시간", "12시간", "24시간")

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
        slope >= 10.0 -> "급상승"
        slope in 2.1..4.9 -> "상승 중"
        slope in -2.0..2.0 -> "유지 중"
        slope in -4.9..-2.1 -> "하강 중"
        slope <= -10.0 -> "급하강"
        else -> "유지 중"
    }
}



