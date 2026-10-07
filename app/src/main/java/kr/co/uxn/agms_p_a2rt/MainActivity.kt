package kr.co.uxn.agms_p_a2rt

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ble.AlwaysA2RTService
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.components.login.EmailLoginScreen
import kr.co.uxn.agms_p_a2rt.ui.components.login.LoginScreen
import kr.co.uxn.agms_p_a2rt.ui.components.login.PassWordResetScreen
import kr.co.uxn.agms_p_a2rt.ui.components.login.SignUpAgreeScreen1
import kr.co.uxn.agms_p_a2rt.ui.components.login.SignUpCheckScreen2
import kr.co.uxn.agms_p_a2rt.ui.components.login.SignUpInfoScreen3
import kr.co.uxn.agms_p_a2rt.ui.components.main.MainScreen
import kr.co.uxn.agms_p_a2rt.ui.components.main.NotiDialog
import kr.co.uxn.agms_p_a2rt.ui.components.main.setting.PrivacyPolicyScreen
import kr.co.uxn.agms_p_a2rt.ui.components.main.setting.TermsAndConditionsScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.EnterFirstGlucose
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen1
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen2
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen3
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen4
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen5
import kr.co.uxn.agms_p_a2rt.ui.components.ready.GuideScreen6
import kr.co.uxn.agms_p_a2rt.ui.components.ready.RegisterDeviceScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.RegisterDeviceScreenQR
import kr.co.uxn.agms_p_a2rt.ui.components.ready.ScanDeviceScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.ScanFailScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.PermissionScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.StabilizationCompleteScreen
import kr.co.uxn.agms_p_a2rt.ui.components.ready.StabilizationScreen
import kr.co.uxn.agms_p_a2rt.ui.components.splash.SplashScreen
import kr.co.uxn.agms_p_a2rt.ui.theme.AGMSPTheme
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.EventScreenViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.HomeViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.LoginNavigationEvent
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.LoginViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.PermissionViewModel
import java.net.URLDecoder
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val bleViewModel: BleViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val eventScreenViewModel: EventScreenViewModel by viewModels()

    val appUpdateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            // 사용자가 '아니요'를 누르거나 뒤로 가기를 한 경우
            Log.w("InAppUpdate", "Flexible update flow cancelled by user.")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // ✅ 시스템 바 영역 침범 허용
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ 상태바와 내비게이션 바 투명하게
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE

        // ✅ 상태바 아이콘 색 조정 (배경이 밝으면 true, 어두우면 false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

        enableEdgeToEdge()

//        PermissionManagerConfig.setCustomRationaleUI { permission, onDismiss, onConfirm ->
//            CustomRationaleDialog(
//                description = permission.description,
//                onDismiss = onDismiss,
//                onConfirm = onConfirm
//            )
//        }
//
//        PermissionManagerConfig.setCustomSettingsUI { permission, onDismiss, onConfirm ->
//            CustomSettingsDialog(
//                description = permission.description,
//                onDismiss = onDismiss,
//                onConfirm = onConfirm
//            )
//        }

        setContent {
            AGMSPTheme {
                Navigation()
            }
        }



        // 서비스 실행 이벤트 처리
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bleViewModel.events.collect { event ->
                    // 안전하게 수집됨! onStop 되면 자동 중단
                    val serviceIntent = Intent(this@MainActivity, AlwaysA2RTService::class.java)
                    if (event == "START_SERVICE") {
                        ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                        Log.e("SERVICE", "메인액티비티 startForegroundService call!")
                    } else if (event == "STOP_SERVICE") {
                        val stopIntent = Intent(this@MainActivity, AlwaysA2RTService::class.java).apply {
                            action = "ACTION_STOP_SERVICE"
                        }
                        ContextCompat.startForegroundService(this@MainActivity, stopIntent)
                    }
                }
            }
        }

        // 앱 업데이트 확인 후 서비스 실행
        lifecycleScope.launch(Dispatchers.IO) {
            val isUpdate = DataStoreManager.getIsUpdateCompleted().firstOrNull()
            if (isUpdate != null) {
                if (isUpdate == true) {
                    DataStoreManager.saveIsUpdateCompleted(false)
                    withContext(Dispatchers.Main) {
                        val serviceIntent = Intent(this@MainActivity, AlwaysA2RTService::class.java)
                        ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                    }
                }
            }
        }

        // 무결성 검사
        //
        // VALID_SIGNATURE_HASH 는 Google Play 앱 서명 키의 SHA-256 인증서 지문이다.
        //
        // 비밀값이 아니다. Play Console > 설정 > 앱 서명 에 평문으로 공개되며,
        // 배포된 APK 의 META-INF 안에 인증서가 동봉되므로 누구나 추출할 수 있다.
        // 이 키의 개인키는 Google 이 보관하며 우리에게 사본이 없다.
        // (우리가 지켜야 하는 비밀은 업로드 키 .jks 와 그 비밀번호다.)
        //
        // 용도: 재패키징 탐지를 위한 자기 서명 대조. 외부로 전송하지 않는다.
        // 갱신: Play Console 에서 앱 서명 키를 교체(key upgrade)한 경우에만
        //      새 지문으로 바꾼다. 평소에는 바뀌지 않는다.
        //      업로드 키 재설정·버전 배포는 이 값을 바꾸지 않는다.
        //
        // **직접 해시해서 비교하지 않는다.** 전에는 getPackageInfo(GET_SIGNATURES) 로
        // 인증서를 꺼내 SHA-256 을 떠서 문자열로 맞대 봤는데, 그 API 는 API 28 에서
        // 폐기됐고 서명 키를 교체하면 회전 **이전**의 인증서를 돌려준다. 그 상태로
        // Play Console 의 새 지문을 넣으면 절차대로 갱신했는데 검증이 깨진다.
        // hasSigningCertificate() 는 회전 이력까지 Android 가 대신 따져 준다.
        lifecycleScope.launch(Dispatchers.IO) {
            val VALID_SIGNATURE_HASH =
                "5FB974AD26663D6CECA631E88F6CBA7243192828FE1062E4DB25F4E803EC164E" // 서명 키

            val expected = hexToBytes(VALID_SIGNATURE_HASH)
            if (expected == null) {
                // 지문이 아니라 상수가 깨진 경우다. 서명을 의심할 일이 아니다.
                Log.e("TEST", "무결성 검증 건너뜀 - 지문 상수가 16진수 64자가 아니다")
                return@launch
            }

            val matched = this@MainActivity.packageManager.hasSigningCertificate(
                this@MainActivity.packageName,
                expected,
                PackageManager.CERT_INPUT_SHA256
            )

            if (matched) {
                Log.d("TEST", "무결성 검증 통과")
                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, R.string.toast_integrity_check_pass, Toast.LENGTH_SHORT).show();
                }
            } else {
                // **아직 집행하지 않는다.** 로그만 남긴다.
                // 디버그 빌드는 debug.keystore 로 서명되므로 늘 여기로 온다 -
                // 개발 중에 이 줄이 보이는 것은 정상이다.
                Log.w("TEST", "무결성 검증 불일치 - 서명 지문이 다르다")
                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, R.string.toast_integrity_check_fail, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 앱 업데이트 알림
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        val installStateUpdatedListener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // 4단계로 점프!
                popupSnackbarForCompleteUpdate(appUpdateManager)
            } else if (state.installStatus() == InstallStatus.FAILED) {
                Log.e("InAppUpdate", "Flexible update download failed.")
            }
        }

        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) { //&& it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                Toast.makeText(this, R.string.toast_request_app_update,Toast.LENGTH_SHORT).show()

                appUpdateManager.startUpdateFlowForResult(
                    it,
                    appUpdateResultLauncher, // 1단계에서 만든 런처
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

//    override fun onPause() {
//        super.onPause()
//        appUpdateManager.unregisterListener(installStateUpdatedListener)
//    }

    /**
     * 16진수 문자열을 바이트 배열로. SHA-256 지문이 아니면 null 이다.
     *
     * 길이를 먼저 보는 까닭이 있다. 상수를 갱신하다 한 글자를 빠뜨리면 그냥 "불일치"
     * 로만 보여서, 서명이 틀린 것인지 상수가 깨진 것인지 구분할 수 없다. 둘을 갈라
     * 놓아야 갱신 절차가 제 구실을 한다.
     */
    private fun hexToBytes(hex: String): ByteArray? {
        if (hex.length != 64) return null
        return try {
            ByteArray(32) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun popupSnackbarForCompleteUpdate(appUpdateManager: AppUpdateManager) {
        Snackbar.make(
            findViewById(android.R.id.content), // Activity의 루트 뷰
            R.string.snakbar_complete_update,
            Snackbar.LENGTH_INDEFINITE // 사용자가 직접 닫거나 액션을 취해야 함
        ).apply {
            setAction("설치") {
                lifecycleScope.launch(Dispatchers.IO) {
                    DataStoreManager.getIsUpdateCompleted().first()?: true
                    DataStoreManager.saveIsUpdateCompleted(true)
                }
                appUpdateManager.completeUpdate()
            }
            setActionTextColor(resources.getColor(R.color.blue_splash)) // 색상 지정
            show()
        }
    }



    @Composable
    fun Navigation(
//        modifier: Modifier = Modifier.safeDrawingPadding(),
        modifier: Modifier = Modifier.fillMaxSize(),
        navController: NavHostController = rememberNavController(),
    ) {

        val destination = remember { mutableStateOf<String?>(null) }
        val lifecycleOwner = LocalLifecycleOwner.current
        val showDuplicateLoginSessionOffDialog = remember { mutableStateOf (false) }

        val context = LocalContext.current
        val localDbRepository by lazy {
            AppDatabase.getInstance(context)
        }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
//            val isMain = DataStoreManager.getIsMain().first() ?: false
//            Log.e("TEST", "isMain From DS : $isMain")
//            destination.value = if (isMain) "MainScreen/0" else "Splash"

            val startRoute = DataStoreManager.getRoute().first() ?: "Splash"

            Log.e("TEST", "Route From DS : $startRoute")
            destination.value = startRoute
//            destination.value = if (isMain) "MainScreen/0" else "Splash"
        }

        LaunchedEffect(Unit) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthEventNotifier.eventFlow.collect { event ->
                    when (event) {
                        AuthEvent.DUPLICATE_LOGIN -> {
                            showDuplicateLoginSessionOffDialog.value = true
                        }
                    }
                }
            }
        }

        if (showDuplicateLoginSessionOffDialog.value) {
            NotiDialog(
                onDismiss = {
                    showDuplicateLoginSessionOffDialog.value = false
                },
                onConfirm = {
                    showDuplicateLoginSessionOffDialog.value = false

                    coroutineScope.launch(Dispatchers.IO) {
                        val userId = DataStoreManager.getUserId().first() ?: -1
                        try {
                            // userId의 db삭제
                            localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                            localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                            localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                            DataStoreManager.saveIsMain(false)
                            DataStoreManager.deleteRoute()
                            DataStoreManager.saveRoute("Splash")
                            Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                            DataStoreManager.deleteAccessToken()
                            DataStoreManager.deleteRefreshToken()
                            DataStoreManager.deleteUserId()
                            DataStoreManager.deleteDeviceMac()
//                                    DataStoreManager.deleteDeviceMac()
                            DataStoreManager.setNotiHighGlucose(false)
                            DataStoreManager.setNotiLowGlucose(false)
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
                        } catch (e: Exception) {
                            Log.e("TEST", "중복로그인 다이얼로그 confirm 에러 : ${e.message}")
                        }
                    }
                },
                title = context.getString(R.string.dialog_detect_other_login_title),
                content = context.getString(R.string.dialog_detect_other_login_content)
            )
        }

        LaunchedEffect(Unit) {
            loginViewModel.navigationEvent.collect { event ->
                when (event) {
                    is LoginNavigationEvent.NavigateToSettingPermission -> {
                        navController.navigate("SettingPermissionScreen/${event.type}")
                        loginViewModel.clearNavigationEvent()
                    }

                    is LoginNavigationEvent.NavigateToSignUp -> {
                        navController.navigate("SignUpAgreeScreen1/${event.type}/${event.oAuthEmail}")
                        Log.e("TAG", "event로 받은 userId: ${event.userId}")
                        Log.e("TAG", "event로 받은 type: ${event.type}")
                        // 민감정보 로그 - 주석 처리 (CWE-200)
//                        Log.e("TAG", "event로 받은 oAuthEmail: ${event.oAuthEmail}")
                        loginViewModel.updateUserId(event.userId)
                        loginViewModel.clearNavigationEvent()
                    }
                    null -> {

                    }
                }
            }
        }

        // 현재 화면의 Route를 실시간으로 감지
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var isSplash = currentRoute == "Splash" || currentRoute == null

        var modifier = Modifier.fillMaxSize()
        if (isSplash) {
            modifier = Modifier.fillMaxSize()
        } else {
            modifier = Modifier.navigationBarsPadding()
                .statusBarsPadding()
        }


        destination.value?.let {
            startDestination ->
            NavHost(navController, startDestination, modifier = modifier) {
//            NavHost(navController, "SettingPermissionScreen/1801", modifier = modifier) {
//            NavHost(navController, "EnterFirstGlucose", modifier = modifier) {
//            NavHost(navController, "StabilizationScreen/mac", modifier = modifier) {
//            NavHost(navController, "SignUpInfoScreen3/choco/choco/1803", modifier = modifier) {
//            NavHost(navController, "RegisterDeviceScreen", modifier = modifier) {
//            NavHost(navController, "RegisterDeviceQRScreen", modifier = modifier) {
//            NavHost(navController, "MainScreen/0", modifier = modifier) {
//            NavHost(navController, "RegisterDeviceQRScreen", modifier = modifier) {
//            NavHost(navController, "GuideScreen1", modifier = modifier) {
                composable("Splash") {
                    SplashScreen(navController, activity = this@MainActivity)
                }

                composable("Login") {
                    LoginScreen(loginViewModel, navController)
                }

                composable("EmailLogin") {
                    EmailLoginScreen(loginViewModel, navController)
                }

                composable("PasswordResetScreen") {
                    PassWordResetScreen(navController)
                }

                composable("SignUpAgreeScreen1/{type}/{email}") { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")?.toIntOrNull() ?: -1
                    val oAuthEmail = backStackEntry.arguments?.getString("email").toString()
                    SignUpAgreeScreen1(navController, type, oAuthEmail)
                }

                composable("SignUpCheckScreen2/{type}/{email}") { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")?.toIntOrNull() ?: -1
                    val oAuthEmail = backStackEntry.arguments?.getString("email").toString()
                    SignUpCheckScreen2(navController, type, oAuthEmail)
                }

                composable("SignUpInfoScreen3/{email}/{pwd}/{type}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email")?.let {
                        URLDecoder.decode(it, "UTF-8")
                    } ?: ""
                    val pwd = backStackEntry.arguments?.getString("pwd").toString()
                    val type = backStackEntry.arguments?.getString("type")?.toInt() ?: -1
                    SignUpInfoScreen3(navController, email, pwd, loginViewModel, type)
                }

                composable("SettingPermissionScreen/{type}") { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")?.toInt() ?: -1
                    PermissionScreen(navController, permissionViewModel, this@MainActivity, type)
                }

                composable("GuideScreen") { backStackEntry ->
                    GuideScreen(navController, this@MainActivity)
                }


                composable("GuideScreen1") { backStackEntry ->
                    GuideScreen1(navController, this@MainActivity)
                }

                composable("GuideScreen2") { backStackEntry ->
                    GuideScreen2(navController)
                }

                composable("GuideScreen3") { backStackEntry ->
                    GuideScreen3(navController)
                }

                composable("GuideScreen4") { backStackEntry ->
                    GuideScreen4(navController)
                }

                composable("GuideScreen5") { backStackEntry ->
                    GuideScreen5(navController)
                }

                composable("GuideScreen6") { backStackEntry ->
                    GuideScreen6(navController)
                }

                composable("RegisterDeviceScreen") { backStackEntry ->
                    RegisterDeviceScreen(navController)
                }

                composable("RegisterDeviceQRScreen") {
                    RegisterDeviceScreenQR(navController)
                }

                composable("ScanDeviceScreen/{mac}/{serialNumber}") { backStackEntry ->
                    val mac = backStackEntry.arguments?.getString("mac").toString()
                    val serialNumber = backStackEntry.arguments?.getString("serialNumber").toString()
                    ScanDeviceScreen(navController, bleViewModel, mac, serialNumber)
                }

                composable("ScanFailScreen") {
                    ScanFailScreen(navController)
                }

                composable("StabilizationScreen/{mac}") { backStackEntry ->
                    val mac = backStackEntry.arguments?.getString("mac")?.toString() ?: ""
                    StabilizationScreen(navController, mac, bleViewModel)
                }

                composable("StabilizationCompleteScreen") { backStackEntry ->
                    StabilizationCompleteScreen(navController)
                }

                composable("EnterFirstGlucose") { backStackEntry ->
                    EnterFirstGlucose(navController)
                }

                composable("MainScreen/{startIndex}") { backStackEntry ->
                    val startIndex = backStackEntry.arguments?.getString("startIndex")?.toInt() ?: 0
                    MainScreen(navController, homeViewModel, bleViewModel, startIndex, eventScreenViewModel)
                }

                composable("MainScreen/{startIndex}/{eventStartCategory}") { backStackEntry ->
                    val startIndex = backStackEntry.arguments?.getString("startIndex")?.toInt() ?: 0
                    val eventStartCategory = backStackEntry.arguments?.getString("eventStartCategory")
                    MainScreen(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        bleViewModel = bleViewModel,
                        startIndex = startIndex,
                        eventScreenViewModel = eventScreenViewModel,
                        eventStartCategory = eventStartCategory
                    )
                }

                composable("MainScreen/{startIndex}/{eventStartCategory}/{recordTimeMillis}") { backStackEntry ->
                    val startIndex = backStackEntry.arguments?.getString("startIndex")?.toInt() ?: 0
                    val eventStartCategory = backStackEntry.arguments?.getString("eventStartCategory")
                    val recordTimeMillis = backStackEntry.arguments
                        ?.getString("recordTimeMillis")
                        ?.toLongOrNull()
                    MainScreen(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        bleViewModel = bleViewModel,
                        startIndex = startIndex,
                        eventScreenViewModel = eventScreenViewModel,
                        eventStartCategory = eventStartCategory,
                        eventRecordTimeMillis = recordTimeMillis
                    )
                }



                composable("PrivacyPolicyScreen") { backStackEntry ->
                    PrivacyPolicyScreen(navController, bleViewModel)
                }

                composable("TermsAndConditionsScreen") { backStackEntry ->
                    TermsAndConditionsScreen(navController, bleViewModel)
                }

            }
        }

    }
}
