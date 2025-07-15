package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val glucoseDataFromUser = remember { mutableStateOf("") }
    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // 다이얼로그 변수 모음
    var showCaliDialog = bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog = bleViewModel.showBleConnectDialog.collectAsState()
    var showBluetoothOnDialog = bleViewModel.showBluetoothOnDialog.collectAsState()
    var showLowGlucoseDialog = bleViewModel.showLowGlucoseDialog.collectAsState()
    var showHighGlucoseDialog = bleViewModel.showHighGlucoseDialog.collectAsState()
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    // 다이얼로그
    // 1. Dialog : 일일 혈당 입력
    if (showCaliDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showCaliDialog(false) },
            onConfirm = {
                BleBridge.showCaliDialog(false)
                navController.navigate("GlucoseRegisterScreen")
            },
            title = "혈당 입력 시간입니다.",
            content = "정확한 측정을 위해 공복 상태에서 자가 채혈한 혈당값을 입력해주세요.",
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}") {
                                    popUpTo("MainScreen/{startIndex}") { inclusive = true }
                                }
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "개인정보 처리방침",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
        ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider()
                Text(
                    modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 20.dp),
                    text = "유엑스엔 개인정보 처리방침\n" +
                            "\n" +
                            "마지막 개정일: 2025년 1월 1일\n" +
                            " \n" +
                            " 본 개인정보 처리방침(“방침”)은 주식회사 유엑스엔 (본 방침에서 “당사”, “당사의” 또는 “당사를”로 칭함)이 모바일 앱, 웹사이트 또는 기타 제품 또는 서비스를 포함하여 고객이 당사와 상호 작용할 때 수집, 생성 및 처리하는 개인 데이터에 적용되며 이 모든 것은 “플랫폼”의 일부입니다. 또한 개인 데이터의 사용, 공유 및 보호 방법, 개인 데이터와 관련된 선택 사항 및 당사에 연락할 수 있는 방법에 대해서 설명합니다.\n" +
                            "\n" +
                            "주식회사 유엑스엔은  Always앱을 포함하여 플랫폼과 관련된 기타 모든 개인 데이터 처리에 대한 책임이 있습니다. 당사에서는 데이터 및 개인 정보 보호의 중요성을 인지하고 있으며 건강 관련 정보를 비롯한 개인 정보를 보호하기 위해 최선을 다하고 있습니다.\n" +
                            "\n" +
                            " \n" +
                            "1. 처리하는 개인정보의 항목 \n" +
                            "2. 개인정보의 처리 목적\n" +
                            "3. 개인정보의 처리 및 보유기간\n" +
                            "4. 개인정보의 파기절차 및 방법에 관한 사항\n" +
                            "5. 개인정보의 제3자 제공에 관한 사항\n" +
                            "6. 개인정보의 안전성 확보 조치에 관한 사항\n" +
                            "7. 정보 주체와 법정대리인의 권리·의무 및 행사 방법에 관한 사항\n" +
                            "8. 개인정보 자동 수집 장치에 관한 사항\n" +
                            "9. 행태정보의 수집.이용, 제공 및 거부 등에 관한 사항\n" +
                            "10.당사가 수집하는 개인정보 관리하는 도구\n" +
                            "11.개인정보 처리방침의 변경에 관한 사항\n" +
                            "12.개인정보보호책임자에 관한 사항\n" +
                            "13.질문 및 피드백\n" +
                            "14.정보 주체의 권익 침해에 대한 구제 방법\n" +
                            "\n" +
                            "\n" +
                            "1. 처리하는 개인정보의 항목  \n" +
                            " \n" +
                            "\n" +
                            "서비스\n" +
                            "수집하는 항목\n" +
                            "Always에 가입하는 경우\n" +
                            "계정 생성 시:\n" +
                            "[필수항목] 이름, 이메일, 간편로그인 정보, 비밀번호\n" +
                            "[선택항목] \n" +
                            "성별, 연령, 신장, 체중, 건강 정보\n" +
                            "계정 생성 시 자동으로 생성된 정보:\n" +
                            "[필수항목] 고유 사용자 ID\n" +
                            "고객 서비스에 문의하는 경우\n" +
                            "[필수항목(웹페이지에서 문의 서비스를 이용하는 경우)] 이름, 이메일\n" +
                            "\n" +
                            "[선택 항목] 문의사항, 전화번호, 결제 정보 (이름, 결제 유형, 결제 계좌 번호, 결제 계좌 확인 정보 포함) (결제 관련 문의사항의 경우)\n" +
                            "귀하가 플랫폼과 상호작용할 때\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "특정 데이터는 귀하의 기기 또는 웹 브라우저 및 통신에서 자동으로 수집됩니다. 이 데이터에는 IP 주소와 같은 기기 정보, 고유 식별자, 고유 기기 식별자 및 기기 유형, 도메인, 브라우저 유형, 버전 및 언어, 운영 체제 및 시스템 설정, 일반적인 위치 정보 및 시간대, 및 유사한 기기 및 사용 정보, 쿠키 및 클릭한 링크, 페이지 보기, 구매, 검색, 사용된 기능, 조회한 항목, 플랫폼 내에서 소요된 시간, 업로드 된 정보, 귀하가 장바구니에 추가한 항목 및 플랫폼 내 다른 사람과의 상호작용과 같은 유사 기술을 사용하여 수집된 온라인 활동 및 브라우징 정보가 포함됩니다. 자세한 내용은 아래의 “개인정보 자동 수집 장치에 관한 사항”을 참조하십시오.\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "2. 개인정보의 처리 목적\n" +
                            "\n" +
                            "당사는 개인정보를 사용하여 다음을 수행합니다.\n" +
                            "\n" +
                            "귀하가 요청하는 플랫폼 및 서비스의 기능 제공\n" +
                            "의료 기기 안전, 품질 및 개선, 불만 및 이상 반응 보고서와 관련된 법적 의무를 포함한 법적 의무를 준수\n" +
                            "사업, 제품 및 서비스의 운영, 개선 및 유지\n" +
                            "당사 또는 타인의 권리, 재산 또는 안전 보호\n" +
                            "일반 연구 및 분석 목적\n" +
                            "법적 의무 준수\n" +
                            "\n" +
                            "당사는 또한 귀하의 개인정보 다른 방법으로 사용할 수 있으며 수집 시점에 특정 통지를 제공하고 필요한 경우 귀하의 동의를 받을 수 있습니다. \n" +
                            "\n" +
                            "\n" +
                            "3. 개인정보의 처리 및 보유 기간\n" +
                            "\n" +
                            "관련 법률에 따라 달리 요구되지 않는 한, 당사는 귀하의 개인정보를 다음과 같이 보유합니다:\n" +
                            "\n" +
                            "Always 회원 개인정보: 귀하의 계정 탈퇴시까지\n" +
                            "고객 서비스 문의를 위해 수집된 개인정보: 5년\n" +
                            "\n" +
                            "관련 법률에 따라 보존해야 하는 개인 데이터의 경우, 당사는 다음과 같은 법적 의무(세법 및 판매법과 보증 목적 등)를 준수하기 위해 이를 보존합니다.\n" +
                            "\n" +
                            "계약 또는 청약철회 등에 관한 기록: 5년(전자상거래 등에서의 소비자보호에 관한 법률)\n" +
                            "대금결제 및 재화 등의 공급에 관한 기록: 5년(전자상거래 등에서의 소비자보호에 관한 법률)\n" +
                            "소비자의 불만 또는 분쟁처리에 관한 기록: 3년(전자상거래 등에서의 소비자보호에 관한 법률)\n" +
                            "\n" +
                            "Always 계정이 2년 이상 회원 프로필에 로그인하지 않을 경우, 당사는 해당 회원의 개인 정보를 다른 회원의 개인 정보와 별도로 저장 및 관리하며, 관련 법률에 특별한 규정이 있는 경우를 제외하고는 해당 개인 정보를 사용하지 않습니다. 계정이 5년 동안  프로필에 로그인하지 않을 경우, 모든 프로필 정보와 개인 데이터는 모든 플랫폼에서 영구적으로 삭제되며 복구할 수 없습니다. \n" +
                            "\n" +
                            "4. 개인정보의 파기 절차 및 방법에 관한 사항\n" +
                            "\n" +
                            "당사는 보관 기간이 만료되거나 귀하와의 계약에 따라 귀하의 개인 정보를 파기합니다. 개인 정보를 파기할 때 당사의 내부 정책에 따라 모든 디지털 프로필 정보와 개인 데이터를 영구적으로 삭제하고, 모든 물리적 사본을 파기하고, 개인 정보를 복구 불가능하거나 복제 불가능하게 만드는 기술적으로 합리적인 조치를 취할 것입니다.\n" +
                            "\n" +
                            "\n" +
                            "5. 개인정보의 제3자 제공에 관한 사항\n" +
                            "\n" +
                            "당사는 다음과 같이 귀하의 개인정보 제3자에게 제공합니다.\n" +
                            "\n" +
                            "귀하가 동의한 제3자.\n" +
                            "동의하지 않은 다른 제3자에게 다음과 같이 제공합니다.\n" +
                            "- 공공기관이 법령 등에서 정하는 소관 업무의 수행을 위한 경우 및 법률이 특별한 규정이 있거나 법령상의 의무를 준수하기 위한 경우 필요한 범위 내에서\n" +
                            "\n" +
                            "- 개인정보 보호법 또는 기타 관련 법령에 특별한 규정이 있는 경우, 그리고\n" +
                            "\n" +
                            "- 법원의 선례 또는 관계 당국의 해석에 따라 제3자에게 개인정보를 제공하는 것이 허용되는 경우\n" +
                            "당사의 사업 또는 자산의 전부 또는 일부를 매각하거나 이전하는 경우(조직 개편, 분사, 해산 또는 청산 포함) 통지와 함께 다른 제 3자에게.\n" +
                            "\n" +
                            "\n" +
                            "6. 개인정보의 안전성 확보조치에 관한 사항\n" +
                            "\n" +
                            "당사는 귀하의 개인 데이터의 안전을 유지하기 위해 다음을 포함한 다양한 관리, 기술 및 물리적 보안 조치를 사용합니다.\n" +
                            "\n" +
                            "관리적 조치: 개인정보의 분실, 도난, 유출, 변경, 훼손 방지를 위한 내부관리계획 수립 및 도입, 개인정보취급자 최소화 및 교육\n" +
                            "기술적 조치: 액세스 제어의 수립 및 구현, 암호화 및 인증 도구의 구현 및 해킹에 대한 대응책\n" +
                            "물리적 조치: 물리적 저장 구역 분리 및 컴퓨터실 및 데이터 저장실 등에 대한 접근 통제, 접근 통제 절차 수립 및 이행\n" +
                            "\n" +
                            "7. 정보 주체와 법정대리인의 권리·의무 및 행사 방법에 관한 사항\n" +
                            "\n" +
                            " 귀하는 (i) 귀하의 개인 데이터에 대한 접근, (ii) 귀하의 개인 데이터 전자 사본(휴대성) 및 이 정보를 다른 회사로 전송, (iii) 불완전하거나 부정확한 경우 귀하의 개인 데이터 수정, 또는 (iv) 해당 법률이 제공하는 특정 상황에서 귀하의 개인 데이터의 삭제 또는 제한을 요청할 권리가 있습니다. 이러한 권리는 절대적이지 않습니다. 당사가 귀하의 개인정보 처리에 대한 귀하의 동의를 받은 경우, 귀하는 언제든지 동의를 철회할 권리가 있습니다.\n" +
                            "\n" +
                            "계정이 있는 경우 플랫폼의 설정 섹션에서 개인 정보에 액세스하거나 편집하거나 계정을 취소하거나 직접 문의할 수 있습니다. 개인 정보 사본을 요청하거나 다른 권리를 행사하려면 당사에 문의하십시오.당사는 요청을 처리하기 전에 요청을 하는 사람이 데이터 주체인지 또는 그 합법적인 대리인인지 확인합니다.\n" +
                            "\n" +
                            "8.개인정보 자동 수집 장치에 관한 사항\n" +
                            "\n" +
                            "당사, 당사의 서비스 제공업체 및 제3자는 귀하가 쿠키, 픽셀 태그 및 기타 유사한 기술과 같은 다양한 방법을 사용하여 당사 플랫폼을 사용할 때 귀하의 브라우저, 기기 또는 앱에서 개인 정보를 포함할 수 있는 정보를 수집합니다. 이러한 쿠키 및 유사 기술에 의해 수집되는 정보에는 다음이 포함될 수 있습니다. IP 주소, 쿠키를 통해 얻은 고유 쿠키 식별자 및 정보, 고유 기기 식별자 및 기기 유형, 도메인, 브라우저 유형 및 언어, 운영 체제 및 시스템 설정, 국가 및 시간대, 이전에 방문한 웹사이트, 클릭 동작 및 표시된 기본 설정과 같은 당사 플랫폼과의 상호작용에 대한 정보, 액세스 시간 및 참조 URL.\n" +
                            "\n" +
                            " 당사는 쿠키 및 유사한 기술을 사용하여 귀하가 당사 플랫폼에 접근, 사용 및 상호 작용하는 방법과 소비자의 선호 사항(국가 및 언어 선택 등)을 분석하고 이해하며, 당사 플랫폼의 성능을 평가, 보안, 보호, 최적화 및 개선합니다. 이를 통해 소비자에게 서비스를 제공하고 온라인 경험을 개선할 수 있습니다. 당사는 또한 쿠키 및 픽셀 태그를 사용하여 플랫폼 트래픽과 상호 작용에 대한 집계 데이터를 수집하고, 추세를 식별하며, 당사 플랫폼을 개선할 수 있도록 통계를 얻습니다. \n" +
                            " \n" +
                            "제3자 기술 및 서비스\n" +
                            " \n" +
                            "당사가 사용하는 일부 쿠키 및 유사 기술은 제3자 회사에서 운영합니다. 당사는 당사 플랫폼의 사용을 평가하고, 성능 및 사용자 경험을 개선하며, 귀하에게 관련 광고를 제공하는 데 도움이 되는 보고서 및 메트릭을 제공하기 위해 당사 플랫폼에 대한 사용 데이터(쿠키, 픽셀 태그 및 유사한 도구 사용)를 수집할 수 있는 Google Analytics 및 광고 제품과 같은 제3자 서비스를 사용합니다. 이 데이터의 처리에는 이러한 제3자의 개인정보 처리방침이 적용됩니다(예: Google Analytics의 경우 Google의 개인정보 처리방침 및 약관 참조). 당사는 제3자 광고 서비스를 사용하기 때문에 귀하는 당사 파트너의 웹사이트와 같은 다른 웹사이트에서 당사의 광고를 볼 수 있습니다. 이러한 광고 서비스를 통해 당사는 귀하의 개별 관심사에 맞춤화된 광고를 보여드릴 수 있습니다. 이러한 광고 서비스는 또한 자동화된 수단을 통해 정보를 수집함으로써 시간이 지남에 따라 여러 웹사이트 및 앱에서 귀하의 온라인 활동을 추적할 수 있습니다. 이 데이터 수집은 당사 플랫폼과 이러한 광고 서비스에 참여하는 제3자 웹사이트 및 앱에서 모두 이루어집니다.\n" +
                            "\n" +
                            "쿠키 기본 설정 관리\n" +
                            "\n" +
                            "당사는 귀하가 당사 플랫폼 내에서 쿠키를 관리할 수 있는 여러 가지 방법을 제공합니다. 이 중 다수는 브라우저 및 기기에 따라 다르기 때문에 귀하가 당사의 플랫폼에 접속하기 위해 사용하는 각 브라우저 및 기기에 대해 기본 설정을 지정해야 합니다. 또한 쿠키를 삭제하거나 차단하는 경우, 귀하는 이러한 기본 설정을 다시 적용해야 할 수 있습니다. 귀하는 쿠키 설치에 대한 선택권을 가지고 있습니다. 따라서 웹 브라우저에서 옵션을 설정함으로써 모든 쿠키를 허용하거나 , 쿠키가 저장될 때마다 확인을 거치거나, 아니면 모든 쿠키의 저장을 거부할 수 있습니다. 단 이용자께서 쿠키 설치를 거부하였을 경우 서비스 제공에 어려움이 있을 수 있습니다.\n" +
                            "\n" +
                            "9. 행태정보의 수집.이용, 제공 및 거부 등에 관한 사항\n" +
                            "\n" +
                            "당사는 귀하가 당사의 플랫폼을 이용하거나 플랫폼과 상호작용을 하는 경우 다음과 같이 행태정보를 수집.이용합니다: \n" +
                            "\n" +
                            " 수집하는 행태정보의 항목: 당사 플랫폼과의 상호작용에 대한 정보 (이용자의 당사 플랫폼 방문이력, 이용이력, 활동이력)\n" +
                            "행태정보의 수집 방법: 쿠키, 픽셀 태그 및 기타 유사한 기술과 같은 다양한 방법을 사용하여 당사 웹사이트 및 앱을 방문/열 때 자동으로 수집됩니다. 이 정보에 개인 데이터가 포함되는 한, 당사는 사용자 동의에 따라 이 데이터를 처리합니다.\n" +
                            "\n" +
                            "행태정보 수집 목적\n" +
                            "\n" +
                            "이용자가 당사 플랫폼에 접근, 사용 및 상호 작용하는 방법 분석 및 이해\n" +
                            "당사 플랫폼의 성능 평가, 보호, 최적화 및 개선 및 서비스 제공,이용자 온라인 경험 개선\n" +
                            "\n" +
                            "행태정보의 보유.이용기간 및 이후 정보처리 방법: 회원 탈퇴 시 파기\n" +
                            "\n" +
                            "10. 당사가 수집하는 개인정보를 관리하는 도구\n" +
                            "\n" +
                            "당사의 플랫폼을 이용할 때에도 당사는 특정 관행에 대하여 제때에 통지하거나 동의를 받습니다. 예를 들어, 당사는 귀하의 권한 사용에 대한 동의를 받습니다. 또는 푸시알림을 전송합니다.  당사는 플랫폼을 통하여 또는기기에서 사용 가능한 표준허가를 사용하여 이러한 동의를 받을 수 있습니다.\n" +
                            "\n" +
                            "대부분의 경우 웹브라우저 또는 모바일 장치 플랫폼은 장치가 특정 범주의 개인정보를 수집 또는 공유할 때 귀하가 통제할 수 있도록 해주는 추가 도구를 제공합니다.  예를 들어,모바일 장치 또는 웹브라우저는 쿠키 사용이나 위치 공유를 관리할 수 있는 도구를 제공할 수 있습니다. 귀하의 장치에서 사용할 수 있는 도구를 숙지하고 사용할 것을 권장합니다.\n" +
                            "\n" +
                            "11. 개인정보 처리방침의 변경에 관한 사항\n" +
                            "\n" +
                            "시간의 경과에 따라 관련 법률과 당사의 현황은 변경될 수 있습니다. 당사의 개인정보 처리방침이 변경되는 경우, 당사는 당사 플랫폼에 변경사항을 게시할 것입니다. 당사가 귀하의 개인정보를 처리하는 방식을 중대하게 변경하는 경우, 당사는 그러한 변경을 시행하기 전에 귀하에게 사전에 통지하거나 법적으로 요구되는 경우 귀하의 동의를 요청할 것입니다. 당사의 개인정보 처리방침을 읽고 당사의 현황에 대해 숙지하시기를 적극 권장 드립니다. 본 개인정보 처리방침은 2025년 1월 1일에 마지막으로 개정되었습니다.  \n" +
                            "\n" +
                            "\n" +
                            "12. 개인정보 보호책임자에 관한 사항\n" +
                            "\n" +
                            "\n" +
                            "당사는 귀하의 개인 정보에 대한 액세스를 정식으로 권한이 부여된 직원으로 엄격히 제한하고, 귀하의 개인 정보를 공유하는 사람을 엄격하게 제한합니다. 당사는 절대로 상업적 이익을 위해 제3자에게 정보를 판매하지 않습니다. 당사는 귀하의 개인 정보의 기밀 및 보안을 유지해야 하는 제3자 클라우드 서비스 제공 업체를 비롯한 제3자 공급 업체와 개인 정보를 공유합니다. 당사가 제3자 공급자에게 귀하의 개인 정보를 제공하는 경우, 이러한 공급자들은 본 문서에 명시된 조건을 준수하고 귀하의 개인 정보를 기밀로 안전하게 유지해야 하며 귀하의 개인 정보를 필요한 최소한의 수준으로 사용해야 합니다.\n" +
                            "\n" +
                            "당사에서 처리하는 경우 담당자는 다음과 같습니다.\n" +
                            " \n" +
                            "이메일: contact@uxn.co.kr\n" +
                            "\n" +
                            "\n" +
                            "13. 질문 및 피드백\n" +
                            "\n" +
                            " 당사의 개인정보 처리방침 및 개인정보보호 관행에 대한 문의, 의견 및 불만사항 등을 편히 말씀 주시기 바랍니다. 귀하의 개인정보와 관련하여 피드백을 제공하거나, 질문이나 우려 사항이 있거나, 권리를 행사하고자 하는 경우, 개인정보 보호 책임자에게 이메일을 보내 주시기 바랍니다.\n" +
                            "\n" +
                            "　개인정보보호에 대해 불만사항에 관하여 당사에 연락하여 주시는 경우, 당사는 그러한 불만 사항의 신속하고 효과적인 해결을 목표로 하겠습니다.\n" +
                            "\n" +
                            "\n" +
                            "14. 정보주체의 권익 침해에 대한 구제 방법\n" +
                            "\n" +
                            " 귀하의 개인 데이터 침해 또는 손해 배상과 관련하여 다른 방식으로 상담하거나 보고해야 하는 경우, 다음 기관에 문의할 수 있습니다.\n" +
                            "\n" +
                            "개인정보 분쟁조정위원회:\n" +
                            "https://www.kopico.go.kr\n" +
                            "\n" +
                            "전화: 1833-6972\n" +
                            "\n" +
                            "개인정보 침해신고센터\n" +
                            "https://privacy.kisa.or.kr\n" +
                            "\n" +
                            "전화: 118\n" +
                            "\n" +
                            "대검찰청 사이버범죄조사팀\n" +
                            "https://www.spo.go.kr\n" +
                            "\n" +
                            "전화: 1301\n" +
                            "\n" +
                            "경찰청\n" +
                            "https://ecrm.cyber.go.kr\n" +
                            "\n" +
                            "전화: 182\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\n"
                )
            }
        }
    }
}

