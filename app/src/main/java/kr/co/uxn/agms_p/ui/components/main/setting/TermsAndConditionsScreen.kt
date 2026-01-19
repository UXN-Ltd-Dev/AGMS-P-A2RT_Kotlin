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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
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
fun TermsAndConditionsScreen(navController: NavController, bleViewModel: BleViewModel) {
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

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    // 다이얼로그
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
                            Toast.makeText(context, context.getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show()
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
                        text = stringResource(R.string.settings_terms_of_service),
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
                if(isKorean) {
                    Text(
                        modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 20.dp),
                        text = "Always 이용약관\n" +
                                "\n" +
                                "마지막 개정일: 2025년 1월 1일\n" +
                                "\n" +
                                "Always 플랫폼을 사용하기 전에 이용 약관(“약관”)을 자세히 읽으시기 바랍니다.\n" +
                                "\n" +
                                "본 약관을 동의하고  UXN 소유의 웹사이트, 모바일 애플리케이션, 연속혈당측정 기술 또는 서비스(“플랫폼”)를 이용함으로써 귀하는, 귀하가 본 계약을 체결할 수 있는 법적 연령에 해당하며, 귀하 본인 또는 귀하가 본 계약의 법적 구속력을 행사할 수 있는 실제적인 권한을 가진 대상인 다른 개인을 대표하여 계약을 수락한다는 점을 단언합니다.\n" +
                                "\n" +
                                "본 약관은 귀하와 UXN(이하 \"당사가\", \"당사를\" 또는 \"당사의\")간에 귀하의 플랫폼 사용에 관한 법적으로 구속력이 있는 계약을 형성합니다. 이 약관은 다음 사항에 적용됩니다.\n" +
                                "\n" +
                                "승인된 디바이스에서 Always 앱(모든 업데이트, 업그레이드, 버그 수정 사항 또는 관련된 수정 버전 포함) 설치 및 이용,\n" +
                                "이 Always 앱 이용을 설명하거나 이와 관련하여 당사가 제공한 인쇄본 또는 전자 형식의 모든 매뉴얼, 지침, 설명서, 사양 또는 기타 자료(“문서”).\n" +
                                "\n" +
                                "중요한 몇가지 요점은 다음과 같습니다.\n" +
                                "\n" +
                                "당사 약관은 변경될 수 있습니다. 당사는 약관을 수시로 업데이트할 수 있습니다. 중대한 변경이 있을 경우 해당 변경 사항이 발효되기 적어도 7일 전에 플랫폼에 고지 사항을 게시하거나 귀하에게 개별적으로 분명한 알림을 전송할 것입니다. 변경이 고객에게 실질적으로 불리하게 적용되는 효과를 가진다면 당사는 허용 가능한 경우 최소 30일 이전 통지를 제공하도록 최선을 다할 것입니다. 변경 사항을 모두 읽고 동의하지 않을 경우 거부 의사를 알려주시고 플랫폼의 사용을 중단하시기 바랍니다. 해당 변경 사항이 발효된 이후 귀하가 새로운 약관 수락을 거부한다고 당사에 알리지 않은 채 계속 플랫폼을 사용하는 경우 해당 법률에서 금지하지 않는 한, 귀하는 업데이트된 약관을 수락하는 것으로 간주됩니다. 귀하가 수정된 약관에 동의하지 않는 경우, UXN는 변경된 약관에 의한 서비스 제공이 어려울 수 있으며 본 계약을 취소 또는 종료할 권리를 보유합니다.\n" +
                                "\n" +
                                "개인정보 처리방침. 당사의 개인정보 처리방침은 플랫폼에서 개인 정보의 수집 및 사용을 설명하며 귀하의 플랫폼 사용에 적용됩니다.\n" +
                                "\n" +
                                "의료적조언 아님에 대한 중요 고지 사항. 플랫폼에는 당뇨병의 진단이나 검진을 목적으로 하지 않습니다. 사용자의 혈당 데이터 분석을 지원하는 정보 관리 서비스일 뿐이며 의료전문가의 조원을 대체하여 사용될 수 없다는 점을 인지해야 합니다. 개인은 당뇨 관리에 대한 의문이나 우려 사항을 비롯하여 의료적 상태에 대해 가질 수 있는 문의 사항이 있는 경우 항상 담당 의사나 자격 있는 기타 의료전문가와 상담해야 합니다.\n" +
                                "\n" +
                                "당사는 플랫폼을 이용하는 동안 발생한 귀하의 피해에 관하여 관계 법령에 따라 보상할 것이지만, 당사의 고의 또는 중과실이 없는 한 책임을 지지 아니합니다. 단, 그러한 피해가 당사 서비스의 본질적인 영역에서의 문제에 기인하는 경우, 신의성실의 원칙에 비추어 당사의 귀책사유가 인정되는 때에는 면책되지 아니합니다\n" +
                                "\n" +
                                "1. Always 플랫폼\n" +
                                "\n" +
                                "A. Always 서비스. Always 플랫폼은 다음 서비스 중 하나 이상으로 구성됩니다.\n" +
                                "\n" +
                                "정보 제공\n" +
                                "\n" +
                                "Always App을 포함한 모바일 애플리케이션의 운영\n" +
                                "\n" +
                                "기타 사용자에게 유용한 추가 서비스.\n" +
                                "\n" +
                                "B. 플랫폼에 대한 변경. 당사는 당사가 제공할 수 있는 최상의 것을 드리고자 플랫폼, 제품 및 서비스를 지속적으로 검토하고 있습니다. 따라서 당사는 통지 없이 언제든지 모든 플랫폼, 제품 또는 서비스에 추가하거나 또는 이를 수정, 중단할 권리를 보유합니다.\n" +
                                "\n" +
                                "2. 기본 원칙\n" +
                                "\n" +
                                "A. 자격. 귀하는 귀하의 국가에서 법적 연령에 도달하였거나 부모 또는 보호자의 동의를 얻은 경우에만 구매를 하거나 플랫폼을 사용할 자격이 있습니다. \n" +
                                "\n" +
                                "B. 등록 규칙. 당사의 계정에 등록할 때 다음의 규칙이 적용됩니다.\n" +
                                "\n" +
                                "진실할 것: 유효하고 정확한 최신 등록 정보를 제공합니다.\n" +
                                "\n" +
                                "본인일 것: 등록은 개인적이어야 합니다. 하나 이상의 계정에 등록하거나, 다른 사람을 대신하여 계정에 등록하거나 계정을 이전하지 마십시오.\n" +
                                "\n" +
                                "보안을 유지할 것: 사용자 이름, 암호 및 기타 로그인 자격 증명의 보안을 유지하고 다른 사람이 계정을 사용하지 않도록 해야 합니다.\n" +
                                "\n" +
                                "책임을 질 것: 계정의 무단 사용이 있을 경우 즉시 알려주십시오. 귀하의 동의 여부를 불문하고 자신의 계정을 통해 발생한 모든 것에 대해 귀하는 책임을 져야 합니다. 해당 법률이 허용하는 최대 수준까지 당사는 귀하의 계정에 대한 무단 사용이 초래한 손실 또는 활동에 대해 책임을 지지 않습니다.\n" +
                                "\n" +
                                "\n" +
                                "3.  콘텐츠의 소유권. 플랫폼의 모든 콘텐츠(텍스트, 소프트웨어, 스크립트, 코드, 디자인, 그래픽, 사진, 애플리케이션, 글, 스케치, 애니메이션, 일반 아트워크 및 기타 콘텐츠 포함)(“콘텐츠”)는 UXN 및 콘텐츠의 라이선스를 허여한 기타 주체의 소유이며, 저작권, 상표, 특허 및 기타 법률의 보호를 받습니다. UXN는 본 약관에 명백하게 명시하지 않은 모든 권리를 보유합니다.\n" +
                                "\n" +
                                "모든 상표, 서비스 마크 및 상호는UXN에 의해 소유되고 등록되고/또는 라이선스가 허여됩니다. 귀하는 플랫폼 또는 콘텐츠에 액세스하거나 사용함을 통해 상표, 서비스 마크 또는 상호에 대한 라이선스나 소유권을 획득하지 않습니다.\n" +
                                "귀하는 플랫폼에서 다운로드하거나 인쇄한 자료에서 소유권 고지를 변경하거나 삭제하지 않는다는 데 동의합니다.\n" +
                                "귀하는 플랫폼과 관련하여 당사가 귀하에게 부여한 특정 사용 권한의 범위를 벗어난 모든 콘텐츠(사용자 콘텐츠 포함)를 당사의 사전 서면 동의 없이 사용, 복사, 편집, 번역, 표시, 배포, 다운로드, 전송, 판매, 그에 대한 파생 저작물 생성 또는 어떠한 방식으로든 이용하는 행위를 하지 않는 데 동의합니다(단, 귀하가 플랫폼에 합법적으로 게시한 자신의 사용자 콘텐츠는 예외입니다). 콘텐츠의 무단 사용은 저작권, 상표 또는 기타 지식재산권 법률 위반을 구성할 수 있으며 형사 또는 민사 고발 및 벌금형에 처해질 수 있습니다.\n" +
                                "\n" +
                                "4. 중요한 법적 고지 \n" +
                                "\n" +
                                "A. 의료적조언 아님. 플랫폼에는 당뇨병의 진단이나 검진을 목적으로 하지 않습니다. 사용자의 혈당 데이터 분석을 지원하는 정보 관리 서비스일 뿐이며 의료전문가의 조원을 대체하여 사용될 수 없다는 점을 인지해야 합니다. 개인은 당뇨 관리에 대한 의문이나 우려 사항을 비롯하여 의료적 상태에 대해 가질 수 있는 문의 사항이 있는 경우 항상 담당 의사나 자격 있는 기타 의료전문가와 상담해야 합니다.\n" +
                                "\n" +
                                "Always를 착용하기 전에 수반된 위험을 고려하고 의료 전문가와 상담하십시오.\n" +
                                "플랫폼의 포함된 정보때문에 전문적 의료 조언을 무시하거나 조언을 구하는 것을 지체해서는 안 됩니다.\n" +
                                "당사는 어떠한 특정 검사, 제품, 절차 또는 의견도 권장하거나 보증하지 않습니다.\n" +
                                "당사는 플랫폼을 이용하는 동안 발생한 귀하의 피해에 관하여 관계 법령에 따라 보상할 것이지만, 당사의 고의 또는 중과실이 없는 한 책임을 지지 아니합니다. 단, 그러한 피해가 당사 서비스의 본질적인 영역에서의 문제에 기인하는 경우, 신의성실의 원칙에 비추어 당사의 귀책사유가 인정되는 때에는 면책되지 아니합니다.\n" +
                                "\n" +
                                "B. 보증의 책임 부인. 이 예외 및 제한은 귀하에게 적용되지 않을 수 있습니다.\n" +
                                "\n" +
                                "고객이 플랫폼을 이용하는 중 인터넷 서비스 과부하, 연결 지연, 연결 실패 또는 중단, 바이러스 등의 서비스 이용장애가 발생한 경우 당사는 고의 중과실이 없는 한 이로 인한 손해에 대해 책임지지 않습니다. 다만 관련 법령에 의해 책임이 인정되는 경우에는 그러하지 않습니다. 또한 구체적 사안에 따라 신의성실의 원칙에 비추어 당사에게 귀책사유가 있어 책임을 부담해야 한다고 판단되는 경우 당사는 면책되지 않습니다.\n" +
                                "\n" +
                                "5. 이용자 계정의 정지, 제한 종료.\n" +
                                "\n" +
                                "관계 법령상 기준 및 산업 기준에 비추어 당사가 합리적으로 판단하기에 귀하의 플랫폼 사용이 당사의 비즈니스 운영을 중대하게 방해하는 경우, 당사는 귀하의 구체적 플랫폼 이용과 세부 활동 내역을 검토할 수 있습니다. 구체적인 예는 다음과 같습니다.\n" +
                                "\n" +
                                "회원 계정 생성 시 허위 정보를 제공하는 경우\n" +
                                "\n" +
                                "하드웨어, 소프트웨어 또는 장비에 있어 작동에 유해하거나, 침입하려 하거나, 그런 영향을 미칠 가능성이 있거나, 작동을 손상시키거나 갈취할 의도가 있거나, 그 이용을 모니터링하려는 의도가 있는 바이러스, 웜, 트로이 목마, 이스터 에그, 시한폭탄, 스파이웨어 또는 기타 컴퓨터 코드, 파일이나 프로그램을 게시, 전송하는 행위.\n" +
                                "\n" +
                                "Always 앱의 일부를 해킹하거나 손상시키는 등, 서버 또는 네트워크의 작동을 방해 또는 중단하거나 이러한 서버 또는 네트워크의 요건, 절차 또는 정책을 위반하는 행위,\n" +
                                "\n" +
                                "불공정하게 다른 이용자의 당사의 플랫폼 또는 제품에 대한 접근을 제한하는 등의 방법으로 전자상거래 질서를 위협하거나, 방해하는 경우\n" +
                                "\n" +
                                "관계 법령 및 본 약관또는 개인정보 취급방침을 위반하는 경우\n" +
                                "\n" +
                                "이러한 경우, 당사는 조사에 관한 이유와 함께 귀하에게 조치를 취하기 전 정보 제공 및 소명을 위한 기회에 대하여 최선을 다하여 고지할 것입니다. 그럼에도 불구하고, 귀하는 당사의 경영 또는 이용자의 보호를 위하여 정지 조치 직후에 고지를 할 수도 있음을 이해합니다. 당사의 검토에 따라, 당사는 귀하의 소명이 비합리적이거나 귀하가 응답하지 아니한 경우 계정의 정지, 제한, 종료 조치를 취할 수 있습니다.\n" +
                                "\n" +
                                "이러한 약관은 계정이 종료되거나 귀하가 플랫폼 사용을 중단한 후에도 유효합니다.\n" +
                                "\n" +
                                "\n" +
                                "6.  분쟁/배상/책임의 제한. \n" +
                                "\n" +
                                "A. 분쟁.\n" +
                                "\n" +
                                "플랫폼과 관련한 귀하의 사용  또는 구매로 인해 발생한 당사와 귀하 간에 논쟁이나 분쟁이 있을 경우, 당사자들은 즉시 선의로 해당 분쟁을 해결하도록 노력해야 합니다.\n" +
                                "\n" +
                                "해당 분쟁을 합리적인 시간 내에 해결하지 못한 경우, 귀하와 당사는 플랫폼을 사용하여 발생한 모든 분쟁, 논쟁 또는 클레임에 관한 소송 등의 제기는 대한민국 민사소송법에 의한 관할 법원에서 이루어집니다.\n" +
                                "\n" +
                                "B. 면책. \n" +
                                "\n" +
                                "귀하는 귀하의 플랫폼 사용, 플랫폼 사용자와 관련된 귀하의 행위 또는 본 약관, 법률 또는 제3자의 권리 위반으로 인하거나 어떤 방식으로든 연관되어 발생하는 모든 청구, 손실, 책임, 경비, 손해 및 비용(변호사 비용 포함을 포함하며 이에 국한되지 않음)으로부터 당사, 임원, 이사, 직원, 대리인 및 공급업체(이하 UXN당사자들”)를 면책하고 방어하며 무해하도록 한다는데 동의합니다. 다만, UXN당사자들의 고의 또는 과실에 기인하는 부분이 있음이 인정된 경우는 제외합니다. 귀하는 자신과 자신의 상속자, 유산, 보험회사, 승계자 및 양수인을 대신하여 귀하의 플랫폼 사용과 어떤 방식으로든 관련된 피해로 생길 수 있는 모든 청구 또는 소송 사유로부터 UXN당사자들을 완전하고 영구적으로 면제합니다. 다만, 당사의 고의 또는 과실에 기인하여 그러한 피해가 발생한 경우에는 해당하지 아니합니다.\n" +
                                "\n" +
                                "천재지변 또는 이에 준하는 불가항력으로 서비스를 제공할 수 없는 경우\n" +
                                "이용자의 귀책사유로 인하여 서비스 이용에 장애가 발생할 경우\n" +
                                "서비스의 이용 불가로 인해 발생한 제반 손실 또는 피해 \n" +
                                "\n" +
                                "C.  책임의 제한.\n" +
                                "\n" +
                                "UXN당사자들은 고의적 불법행위나 중과실의 경우에만 책임을 집니다. 원칙적으로 귀하는 귀하의 적극적인 행위에 기초한 플랫폼 사용에 대해 전적인 책임을 집니다. 그러나 귀하의 플랫폼 이용에 관련한 손실 또는 손해에 대하여 당사의 책임이 인정되는 경우 당사는 이에 대한 책임을 집니다. 다만 그 책임의 범위는 원칙적으로 통상손해에 국한되며, 당사가 알거나 알 수 있었을 때가 아닌 한 특별손해에 대해서는 책임지지 않습니다.\n" +
                                "다만 다음의 경우 당사가 특별손해의 존재에 대해 알았거나 알 수 있었을 경우로 간주되지 않습니다. (a) 고객의 일방적인 요청 또는 진술 (b) 본 약관의 내용에 반하는 요청 (c) 당사가 명시적으로 동의하지 않은 요청이나 진술 (d) 기타 신의성실 및 사회상규에 벗어난 요구\n" +
                                "\n" +
                                "7. 기타 조건. \n" +
                                "\n" +
                                "A. 전자 커뮤니케이션 \n" +
                                "\n" +
                                "플랫폼을 사용함으로써 해당 법률에 따라 귀하는 당사로부터 특정 전자 커뮤니케이션을 받는 데 동의하게 됩니다.\n" +
                                "\n" +
                                "귀하에게 제공되는 모든 통지는 이메일 주소 또는 문자 메시지, 또는 귀하의 계정 설정에 미리 귀하가 지정한 기타 커뮤니케이션 방법으로 전송되어야 합니다. 귀하는 당사가 귀하에게 전자적으로 전송한 모든 통지, 계약, 공개 또는 기타 커뮤니케이션이 모든 법적 커뮤니케이션 요구 사항(해당 커뮤니케이션이 서면이어야 한다는 요구 사항 포함)을 충족한다는 데 동의합니다.\n" +
                                "\n" +
                                "B. 앱 마켓플레이스\n" +
                                "\n" +
                                "귀하는 본 계약이 귀하와 당사 간에만 이루어진 것이며, Always 앱을 다운로드했던 앱 마켓플레이스(예: iOS 사용자의 경우 Apple Inc.가 운영하는 App Store 또는 Android 사용자의 경우 Google Inc.가 운영하는Google Play 스토어)와의 계약은 아님을 인정합니다.\n" +
                                "\n" +
                                "\n" +
                                "본 계약의 어떤 조항이 불법이거나, 효력이 없거나, 시행할 수 없는 이유가 발견되면, 해당 조항은 본 계약에서 분리될 수 있으며 나머지 조항의 유효성 및 집행 가능성에는 영향을 미치지 않습니다.\n" +
                                "귀하는 명시적 사전 서면 동의 없이, 본 계약에 따른 귀하의 권리 또는 의무 전부 또는 일부를 양도, 이전 또는 라이센스를 재부여할 수 없습니다.\n" +
                                "\n" +
                                "읽어주셔서 감사합니다.\n" +
                                "\n" +
                                "\n"
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(top = 30.dp, start = 20.dp, end = 20.dp),
                        text = "Always Terms of Service\n" +
                                "\n" +
                                "Last Revised Date: January 1, 2025\n" +
                                "\n" +
                                "Please read these Terms of Service (“Terms”) carefully before using the Always platform.\n" +
                                "\n" +
                                "By agreeing to these Terms and using the UXN-owned website, mobile application, continuous glucose monitoring technology, or services (“Platform”), you affirm that you are of legal age to enter into this agreement and that you accept this agreement on behalf of yourself or another individual for whom you have the actual authority to legally bind.\n" +
                                "\n" +
                                "These Terms form a legally binding agreement between you and UXN (“we,” “us,” or “our”) regarding your use of the Platform. These Terms apply to the following:\n" +
                                "\n" +
                                "The installation and use of the Always app on approved devices (including all updates, upgrades, bug fixes, or related modified versions),\n" +
                                "All manuals, instructions, guides, specifications, or other materials provided by us in print or electronic form that describe or relate to the use of the Always app (“Documentation”).\n" +
                                "\n" +
                                "Some important points are as follows:\n" +
                                "\n" +
                                "Our Terms may change. We may update these Terms from time to time. If there are material changes, we will post a notice on the Platform or send you a clear individual notification at least seven (7) days before such changes take effect. If a change has a materially adverse effect on customers, we will make reasonable efforts to provide at least thirty (30) days’ prior notice where permitted. If you do not agree to all changes, please notify us of your refusal and discontinue use of the Platform. If you continue to use the Platform after the changes take effect without notifying us that you reject the new Terms, you will be deemed to have accepted the updated Terms to the extent not prohibited by applicable law. If you do not agree to the amended Terms, UXN may be unable to provide services under the changed Terms and reserves the right to cancel or terminate this agreement.\n" +
                                "\n" +
                                "Privacy Policy. Our Privacy Policy explains the collection and use of personal information on the Platform and applies to your use of the Platform.\n" +
                                "\n" +
                                "Important Notice Regarding No Medical Advice. The Platform is not intended for the diagnosis or screening of diabetes. It is solely an information management service that supports the analysis of users’ blood glucose data and must not be used as a substitute for assistance from medical professionals. Individuals should always consult their physician or another qualified healthcare professional regarding any questions or concerns about medical conditions, including diabetes management.\n" +
                                "\n" +
                                "We will compensate you for damages incurred while using the Platform in accordance with applicable laws; however, we shall not be liable unless such damages are caused by our willful misconduct or gross negligence. Notwithstanding the foregoing, if such damages arise from issues within the essential scope of our services and our fault is recognized in light of the principle of good faith, we shall not be exempt from liability.\n" +
                                "\n" +
                                "1. Always Platform\n" +
                                "\n" +
                                "A. Always Services. The Always Platform consists of one or more of the following services:\n" +
                                "\n" +
                                "Provision of information\n" +
                                "\n" +
                                "Operation of mobile applications, including the Always App\n" +
                                "\n" +
                                "Other additional services useful to users.\n" +
                                "\n" +
                                "B. Changes to the Platform. We continuously review our Platform, products, and services to provide the best possible offerings. Accordingly, we reserve the right to add to, modify, or discontinue any Platform, product, or service at any time without notice.\n" +
                                "\n" +
                                "2. Basic Principles\n" +
                                "\n" +
                                "A. Eligibility. You are eligible to make purchases or use the Platform only if you have reached the legal age in your country or have obtained the consent of a parent or legal guardian.\n" +
                                "\n" +
                                "B. Registration Rules. The following rules apply when registering for an account with us:\n" +
                                "\n" +
                                "Be truthful: Provide valid, accurate, and up-to-date registration information.\n" +
                                "\n" +
                                "Be yourself: Registration must be personal. Do not register for more than one account, register on behalf of another person, or transfer your account.\n" +
                                "\n" +
                                "Maintain security: You must maintain the security of your username, password, and other login credentials and prevent others from using your account.\n" +
                                "\n" +
                                "Be responsible: Notify us immediately of any unauthorized use of your account. You are responsible for all activities that occur through your account, whether or not authorized by you. To the maximum extent permitted by applicable law, we are not responsible for losses or activities resulting from unauthorized use of your account.\n" +
                                "\n" +
                                "\n" +
                                "3. Ownership of Content. All content on the Platform (including text, software, scripts, code, designs, graphics, photos, applications, writings, sketches, animations, general artwork, and other content) (“Content”) is owned by UXN or other parties that have licensed such content and is protected by copyright, trademark, patent, and other laws. UXN reserves all rights not expressly granted in these Terms.\n" +
                                "\n" +
                                "All trademarks, service marks, and trade names are owned, registered, and/or licensed by UXN. By accessing or using the Platform or Content, you do not acquire any license or ownership rights in any trademarks, service marks, or trade names.\n" +
                                "You agree not to alter or remove any proprietary notices from materials downloaded or printed from the Platform.\n" +
                                "You agree not to use, copy, edit, translate, display, distribute, download, transmit, sell, create derivative works of, or otherwise exploit any Content (including user content) beyond the scope of specific usage rights granted to you by us in connection with the Platform without our prior written consent (except for your own user content lawfully posted on the Platform). Unauthorized use of Content may constitute a violation of copyright, trademark, or other intellectual property laws and may result in criminal or civil liability and penalties.\n" +
                                "\n" +
                                "4. Important Legal Notices\n" +
                                "\n" +
                                "A. No Medical Advice. The Platform is not intended for the diagnosis or screening of diabetes. It is solely an information management service that supports the analysis of users’ blood glucose data and must not be used as a substitute for assistance from medical professionals. Individuals should always consult their physician or another qualified healthcare professional regarding any questions or concerns about medical conditions, including diabetes management.\n" +
                                "\n" +
                                "Before wearing Always, consider the associated risks and consult a medical professional.\n" +
                                "You should not disregard professional medical advice or delay seeking such advice because of information contained on the Platform.\n" +
                                "We do not recommend or endorse any specific tests, products, procedures, or opinions.\n" +
                                "We will compensate you for damages incurred while using the Platform in accordance with applicable laws; however, we shall not be liable unless such damages are caused by our willful misconduct or gross negligence. Notwithstanding the foregoing, if such damages arise from issues within the essential scope of our services and our fault is recognized in light of the principle of good faith, we shall not be exempt from liability.\n" +
                                "\n" +
                                "B. Disclaimer of Warranties. These exclusions and limitations may not apply to you.\n" +
                                "\n" +
                                "If service disruptions such as internet service overload, connection delays, connection failures or interruptions, or viruses occur while a customer is using the Platform, we shall not be liable for resulting damages unless caused by our willful misconduct or gross negligence. However, this does not apply where liability is recognized under applicable laws. In addition, where it is determined that we bear responsibility in light of the principle of good faith depending on the specific circumstances, we shall not be exempt from liability.\n" +
                                "\n" +
                                "5. Suspension, Restriction, or Termination of User Accounts.\n" +
                                "\n" +
                                "If, based on applicable laws and industry standards, we reasonably determine that your use of the Platform materially interferes with our business operations, we may review your specific use of the Platform and detailed activity. Specific examples include, but are not limited to:\n" +
                                "\n" +
                                "Providing false information when creating a member account\n" +
                                "\n" +
                                "Posting or transmitting viruses, worms, Trojan horses, Easter eggs, time bombs, spyware, or other computer code, files, or programs that are harmful to, attempt to intrude upon, may affect, impair, exploit, or monitor the operation of hardware, software, or equipment\n" +
                                "\n" +
                                "Hacking or damaging any part of the Always app, or interfering with or disrupting the operation of servers or networks, or violating the requirements, procedures, or policies of such servers or networks\n" +
                                "\n" +
                                "Threatening or disrupting electronic commerce order by unfairly restricting other users’ access to our Platform or products\n" +
                                "\n" +
                                "Violating applicable laws, these Terms, or the Privacy Policy\n" +
                                "\n" +
                                "In such cases, we will make reasonable efforts to notify you, along with the reasons for the investigation, and provide you with an opportunity to receive information and present an explanation before taking action. Nevertheless, you understand that we may provide notice immediately after taking suspension measures to protect our business or users. Based on our review, if your explanation is deemed unreasonable or you fail to respond, we may suspend, restrict, or terminate your account.\n" +
                                "\n" +
                                "These Terms remain effective even after your account is terminated or you discontinue use of the Platform.\n" +
                                "\n" +
                                "\n" +
                                "6. Disputes / Indemnification / Limitation of Liability.\n" +
                                "\n" +
                                "A. Disputes.\n" +
                                "\n" +
                                "In the event of any controversy or dispute between you and us arising out of your use of or purchase related to the Platform, the parties shall immediately endeavor to resolve the dispute in good faith.\n" +
                                "\n" +
                                "If such dispute cannot be resolved within a reasonable period of time, any lawsuit or legal action relating to all disputes, controversies, or claims arising from use of the Platform shall be brought before a court of competent jurisdiction in accordance with the Civil Procedure Act of the Republic of Korea.\n" +
                                "\n" +
                                "B. Indemnification.\n" +
                                "\n" +
                                "You agree to indemnify, defend, and hold harmless us, our officers, directors, employees, agents, and suppliers (“UXN Parties”) from and against any and all claims, losses, liabilities, expenses, damages, and costs (including, without limitation, attorneys’ fees) arising out of or in any way connected with your use of the Platform, your conduct related to Platform users, or your violation of these Terms, applicable laws, or the rights of third parties, except to the extent caused by the willful misconduct or negligence of the UXN Parties. On behalf of yourself and your heirs, estates, insurers, successors, and assigns, you fully and permanently release the UXN Parties from any and all claims or causes of action arising from damages related in any way to your use of the Platform, except where such damages are caused by our willful misconduct or negligence.\n" +
                                "\n" +
                                "In cases where services cannot be provided due to natural disasters or force majeure events\n" +
                                "In cases where service disruptions occur due to reasons attributable to the user\n" +
                                "Any losses or damages arising from the inability to use the service\n" +
                                "\n" +
                                "C. Limitation of Liability.\n" +
                                "\n" +
                                "The UXN Parties shall be liable only in cases of willful misconduct or gross negligence. In principle, you bear full responsibility for your use of the Platform based on your affirmative actions. However, where liability on our part is recognized for losses or damages related to your use of the Platform, we shall bear such liability. The scope of such liability is, in principle, limited to ordinary damages, and we shall not be liable for special damages unless we knew or could have known of such damages.\n" +
                                "The following cases shall not be deemed situations in which we knew or could have known of the existence of special damages: (a) unilateral requests or statements by the customer; (b) requests contrary to the content of these Terms; (c) requests or statements to which we did not expressly agree; (d) other demands that violate the principle of good faith or social norms.\n" +
                                "\n" +
                                "7. Miscellaneous.\n" +
                                "\n" +
                                "A. Electronic Communications\n" +
                                "\n" +
                                "By using the Platform, you consent to receiving certain electronic communications from us in accordance with applicable laws.\n" +
                                "\n" +
                                "All notices provided to you must be sent to your email address, via text message, or through other communication methods you have previously designated in your account settings. You agree that all notices, agreements, disclosures, or other communications that we send to you electronically satisfy any legal communication requirements, including requirements that such communications be in writing.\n" +
                                "\n" +
                                "B. App Marketplace\n" +
                                "\n" +
                                "You acknowledge that this agreement is solely between you and us, and not with the app marketplace from which you downloaded the Always app (e.g., the App Store operated by Apple Inc. for iOS users or the Google Play Store operated by Google Inc. for Android users).\n" +
                                "\n" +
                                "\n" +
                                "If any provision of this agreement is found to be illegal, invalid, or unenforceable, such provision shall be severable from this agreement and shall not affect the validity or enforceability of the remaining provisions.\n" +
                                "You may not assign, transfer, or sublicense all or any part of your rights or obligations under this agreement without our express prior written consent.\n" +
                                "\n" +
                                "Thank you for reading.\n" +
                                "\n" +
                                "\n"
                    )
                }
            }
        }
    }
}

