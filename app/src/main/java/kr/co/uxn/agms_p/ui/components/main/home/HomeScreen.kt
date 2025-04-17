package kr.co.uxn.agms_p.ui.components.main.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.rememberMarker
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.VivoItem
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val weo1 by bleViewModel.weo1.collectAsState()
    val temperature by bleViewModel.temperature.collectAsState()
    val glucose by bleViewModel.glucose.collectAsState()
    val chartTrigger by bleViewModel.chartTrigger.collectAsState()

    // Vico Chart
    val modelProducer = remember { ChartEntryModelProducer() }
    val dataSetForModel = remember { mutableStateListOf(listOf<FloatEntry>()) }
    val dataSetLineSpec = remember { arrayListOf<LineChart.LineSpec>() }
    val scrollState = rememberChartScrollState()

    val isLoading = remember { mutableStateOf(false) }

    // RadioButton
    var selectedOption by remember { mutableStateOf("3시간") }

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    // VICO : 기존 더미데이터 차트
//    LaunchedEffect(Unit) {
//        dataSetForModel.clear()
//        dataSetLineSpec.clear()
//        var xPos = 1f
//        val dataPoints = arrayListOf<FloatEntry>()
//
//        // 차트 디자인 옵션
//        dataSetLineSpec.add(
//            LineChart.LineSpec(
//                lineColor = Color(0xFF6FB0E5).toArgb(),
//                lineBackgroundShader = DynamicShaders.fromBrush(
//                    brush = Brush.verticalGradient(
//                        listOf(
//                            Color(0xFF6FB0E5).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END),
//                            Color(0xFF6FB0E5).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START)
//                        )
//                    )
//                )
//            )
//        )
//
//        // 데이터
////        for (i in 1..100) { // 데이터 갯수
////            val randomYFloat = (50..180).random().toFloat()
////            dataPoints.add(FloatEntry(x = xPos, y = randomYFloat))
////            xPos += 1f
////        }
//
//        for(i in 0 .. 20) {
//            dataPoints.add(FloatEntry(x = xPos, y = 0f))
//            xPos += 1
//        }
//
//        dataSetForModel.add(dataPoints)
//        modelProducer.setEntries(dataSetForModel)
//
//    }



    LaunchedEffect(chartTrigger) {

        dataSetForModel.clear()
        dataSetLineSpec.clear()
        val dataPoints = arrayListOf<FloatEntry>()


        // 차트 디자인 옵션
        dataSetLineSpec.add(
            LineChart.LineSpec(
                lineColor = Color(0xFF6FB0E5).toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF6FB0E5).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                            Color(0xFF6FB0E5).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START)
                        )
                    )
                )
            )
        )

        val userId = DataStoreManager.getUserId().first() ?: -1


        val localDBDataList = withContext(Dispatchers.IO) {
            localDbRepository?.dataDao()?.getGlucoseList(userId = userId)
        }
        for ( i in 0 until localDBDataList!!.size) {


            dataPoints.add(FloatEntry(x = localDBDataList[i].createdAtLong.toFloat(), y = localDBDataList[i].glucose.toFloat()))
        }

        dataSetForModel.add(dataPoints)
        modelProducer.setEntries(dataSetForModel)
//        scrollState.lastScrolledForward
//        dataPoints.lastOrNull()?.let { lastEntry ->
//            scrollState.scroll(scrollPriority = )
//        }

        isLoading.value = true
    }

    // 시연용 타이머
    LaunchedEffect(Unit) {
        homeViewModel.startTimerForTest()
        Log.e("TEST", "홈 화면에서 타이머 실행")
    }

    // 실제 타이머
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                // onResume 시점에만 실행!
//                homeViewModel.startTimer()
//                Log.e("TEST", "홈 화면에서 타이머 실행")
//            }
//        }
//
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }



//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_CREATE) {
//                // onResume 시점에만 실행!
//                // 서버로부터 이벤트 목록 받아와서 화면 갱신해주기
//                Log.e("TEST", "이벤트 화면에서 onResume일때 DisposableEffect 실행")
//
//                coroutineScope.launch(Dispatchers.IO) {
//                    try {
//                        val userId = DataStoreManager.getUserId().first() ?: -1
//                        val glucoseList = tokenRetrofit.getGlucoseList(userId)
//                        if (glucoseList.isSuccessful) {
//                            val glucoseListBody = glucoseList.body()
//                            if (glucoseListBody != null) {
//                                Log.e("TEST", "불러온 glucoseListBody : ${glucoseListBody}")
//
//
//                                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
//
//                                val glucoseDataSetList = glucoseListBody.map {
//                                    val timeMillis = formatter.parse(it.createdAt)?.time?.toFloat() ?: 0f
//                                    VivoItem(xAxisTime = timeMillis, yAxisValue = it.glucose.toFloat())
//                                }
//                                Log.e("TEST", "glucoseDataSetList : ${glucoseDataSetList}")
//
//
//                                val currentDataSetList = glucoseListBody.map {
//                                    val timeMillis = formatter.parse(it.createdAt)?.time?.toFloat() ?: 0f
//                                    VivoItem(xAxisTime = timeMillis, yAxisValue = it.current.toFloat())
//                                }
//
//                                // VIVO
//                                withContext(Dispatchers.Main) {
//                                    dataSetForModel.clear()
//                                    dataSetLineSpec.clear()
////                                    var xPos = 1f
//                                    val dataPoints = arrayListOf<FloatEntry>()
//
//                                    // 차트 디자인 옵션
//                                    dataSetLineSpec.add(
//                                        LineChart.LineSpec(
//                                            lineColor = Color(0xFF6FB0E5).toArgb(),
//                                            lineBackgroundShader = DynamicShaders.fromBrush(
//                                                brush = Brush.verticalGradient(
//                                                    listOf(
//                                                        Color(0xFF6FB0E5).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END),
//                                                        Color(0xFF6FB0E5).copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START)
//                                                    )
//                                                )
//                                            )
//                                        )
//                                    )
//
//                                    for ( i in 0 until glucoseDataSetList.size) {
//                                        dataPoints.add(FloatEntry(x = glucoseDataSetList[i].xAxisTime, y = glucoseDataSetList[i].yAxisValue))
//                                    }
//
//                                    dataSetForModel.add(dataPoints)
//                                    modelProducer.setEntries(dataSetForModel)
//                                    isLoading.value = true
//                                }
//                            }
//                        } else {
//                            Log.e("TEST", "API 에러 : ${glucoseList.errorBody()?.string()}")
//                        }
//                    } catch (e: Exception) {
//                        Log.e("TEST", "네트워크 에러 : ${e.message}")
//                    }
//                }
//            }
//        }
//
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }



    // 혈당 표시 카드
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(10.dp),
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
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 20.dp)
                )

                Image(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(17.dp),
                    painter = painterResource(R.drawable.glucose_reset),
                    contentDescription = "glucoseReset"
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.width(35.dp))
                Text(
                    text = "${glucose}",
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "mg/dL",
                    color = Color.White,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.width(70.dp))
                Column(
                    modifier = Modifier.align(Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .size(32.dp),
                        painter = painterResource(R.drawable.level3),
                        contentDescription = "glucose_lv3"
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "유지 중",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

            }
        }

        // 그래프 표시 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White, // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "혈당 그래프",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(start = 20.dp)
            )

            // TODO VICO CHART
            if (dataSetForModel.isNotEmpty() && isLoading.value == true) {
                ProvideChartStyle {
                    val marker = rememberMarker()
                    Chart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp, end = 10.dp),
                        chart = lineChart(
                            lines = dataSetLineSpec,
                            axisValuesOverrider = AxisValuesOverrider.fixed(minY = 0f, maxY = 250f)
                        ),
                        chartModelProducer = modelProducer,
                        chartScrollState = scrollState,
                        chartScrollSpec = rememberChartScrollSpec(initialScroll = InitialScroll.End), // 우측부터 최신값추가
                        // y축
                        startAxis = rememberStartAxis(
                            title = "Top values",
                            tickLength = 0.dp,
                            valueFormatter = { value, _ ->
                                value.toInt().toString()
                            },
                            // y축 레이블 갯수
                            itemPlacer = AxisItemPlacer.Vertical.default(
                                maxItemCount = 6,
                                shiftTopLines = true
                            )
                        ),

                        // x축
                        bottomAxis = rememberBottomAxis(
                            title = "Count of values",
                            tickLength = 0.dp,
                            valueFormatter = { value, _ ->
                                val date = Date(value.toLong())
                                val displayFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
                                displayFormat.format(date)
                            },
                            guideline = null,
                            itemPlacer = AxisItemPlacer.Horizontal.default(
                                spacing = 1  // x축 라벨 간격을 더 촘촘히 (기본은 자동)
                            )
//
                        ),
                        marker = marker,
                        isZoomEnabled = true
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "그래프를 불러오는 중 입니다...",
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

        // 센서 정보 표시 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(10.dp),
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
                    .padding(top = 10.dp, start = 15.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "센서 정보",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
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
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 10) {
                    val painter = if (i < 10 - day + 1) {
                        R.drawable.sensor_progress_on
                    } else {
                        R.drawable.sensor_progress_off
                    }
                    Image(
                        painter = painterResource(painter),
                        contentDescription = "센서 진행률",
                        modifier = Modifier.size(25.dp, 10.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if ((10 - day + 1) < 11) {
                    Text(
                        text = "${10 - day + 1}/10일",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }  else {
                    Text(
                        text = "10/10일",
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
