package kr.co.uxn.agms_p

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.AlwaysService
import kr.co.uxn.agms_p.ui.components.login.LoginScreen
import kr.co.uxn.agms_p.ui.components.login.PassWordResetScreen
import kr.co.uxn.agms_p.ui.components.login.SignUpAgreeScreen1
import kr.co.uxn.agms_p.ui.components.login.SignUpCheckScreen2
import kr.co.uxn.agms_p.ui.components.login.SignUpInfoScreen3
import kr.co.uxn.agms_p.ui.components.main.MainScreen
import kr.co.uxn.agms_p.ui.components.main.event.ActivityRegisterScreen
import kr.co.uxn.agms_p.ui.components.main.event.GlucoseRegisterScreen
import kr.co.uxn.agms_p.ui.components.main.setting.DeleteAccountScreen
import kr.co.uxn.agms_p.ui.components.main.setting.MyInfoScreen
import kr.co.uxn.agms_p.ui.components.main.setting.NotificationScreen
import kr.co.uxn.agms_p.ui.components.main.setting.PrivacyPolicyScreen
import kr.co.uxn.agms_p.ui.components.main.setting.SensorInfoScreen
import kr.co.uxn.agms_p.ui.components.main.setting.TermsAndConditionsScreen
import kr.co.uxn.agms_p.ui.components.main.setting.VersionInfoScreen
import kr.co.uxn.agms_p.ui.components.ready.EnterFirstGlucose
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen1
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen2
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen3
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen4
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen5
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen6
import kr.co.uxn.agms_p.ui.components.ready.RegisterDeviceScreen
import kr.co.uxn.agms_p.ui.components.ready.ScanDeviceScreen
import kr.co.uxn.agms_p.ui.components.ready.ScanFailScreen
import kr.co.uxn.agms_p.ui.components.ready.SettingPermissionScreen
import kr.co.uxn.agms_p.ui.components.ready.StabilizationCompleteScreen
import kr.co.uxn.agms_p.ui.components.ready.StabilizationScreen
import kr.co.uxn.agms_p.ui.components.splash.SplashScreen
import kr.co.uxn.agms_p.ui.theme.AGMSPTheme
import kr.co.uxn.agms_p.ui.viewmodel.AuthEventNotifier
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel
import kr.co.uxn.agms_p.ui.viewmodel.LoginNavigationEvent
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel
import kr.co.uxn.agms_p.ui.viewmodel.PermissionViewModel
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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

//        enableEdgeToEdge()

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
                    val serviceIntent = Intent(this@MainActivity, AlwaysService::class.java)
                    if (event == "START_SERVICE") {
                        ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                        Log.e("SERVICE", "메인액티비티 startForegroundService call!")
                    } else if (event == "STOP_SERVICE") {
//                        this@MainActivity.stopService(serviceIntent)
                        val stopIntent = Intent(this@MainActivity, AlwaysService::class.java).apply {
                            action = "ACTION_STOP_SERVICE"
                        }
                        ContextCompat.startForegroundService(this@MainActivity, stopIntent)
                    }
                }
            }
        }



    }

    override fun onResume() {
        super.onResume()

        // 앱 업데이트 알림
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) { //&& it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                Toast.makeText(this, "앱을 최신버전으로 업데이트 해주세요",Toast.LENGTH_SHORT).show()

                appUpdateManager.startUpdateFlowForResult(
                    it,
                    appUpdateResultLauncher, // 1단계에서 만든 런처
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )

            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)



        // 무결성 검사
        val VALID_SIGNATURE_HASH =
            "9E9233211A9157E699D49D8C0E7A4289B180A1B3DC4B9EE7AC3C96A6AA86FAB1" // 서명 키
        try {
            val packageInfo = this.packageManager.getPackageInfo(
                this.packageName,
                PackageManager.GET_SIGNATURES
            )

            for (signature in packageInfo.signatures!!) {
                Log.d("TEST", "서명 정보 : " + signature.toCharsString())
                var md: MessageDigest? = null
                try {
                    md = MessageDigest.getInstance("SHA-256")
                    md.update(signature.toByteArray())
                    val currentHash: String = toHex(md.digest())

                    if (currentHash.equals(VALID_SIGNATURE_HASH, ignoreCase = true)) {
                        Log.e("TEST", "무결성 검증 통과!")
                        //                        Toast.makeText(this, "무결성 검증 통과!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "앱 실행에 문제가 감지되었습니다. 안전한 사용을 위해 앱을 다시 설치해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NoSuchAlgorithmException) {
                    Toast.makeText(this, "앱 실행에 문제가 감지되었습니다. 안전한 사용을 위해 앱을 다시 설치해 주세요.", Toast.LENGTH_SHORT).show()
                    Log.e("TEST", "무결성 검증 실패: " + e.message)
                    throw RuntimeException(e)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TEST", "무결성 검증 실패: " + e.message)
            Toast.makeText(this, "앱 실행에 문제가 감지되었습니다. 안전한 사용을 위해 앱을 다시 설치해 주세요.", Toast.LENGTH_SHORT).show()
            throw RuntimeException(e)
        }
    }

    override fun onPause() {
        super.onPause()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun toHex(bytes: ByteArray): String {
        // "%02X" : %X(대문자 16진수), 02(2자리로, 비면 0으로 채움)
        return bytes.joinToString("") { "%02X".format(it) }
    }

    fun popupSnackbarForCompleteUpdate(appUpdateManager: AppUpdateManager) {
        Snackbar.make(
            findViewById(android.R.id.content), // Activity의 루트 뷰
            "새 버전 다운로드가 완료되었습니다.",
            Snackbar.LENGTH_INDEFINITE // 사용자가 직접 닫거나 액션을 취해야 함
        ).apply {
            setAction("설치") {
                // ★★★★★ 이게 진짜 설치(재시작)를 실행하는 코드 ★★★★★
                appUpdateManager.completeUpdate()
            }
            setActionTextColor(resources.getColor(R.color.blue_splash)) // 색상 지정
            show()
        }
    }



    @Composable
    fun Navigation(
//        modifier: Modifier = Modifier.safeDrawingPadding(),
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController()
    ) {

        val destination = remember { mutableStateOf<String?>(null) }

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
            AuthEventNotifier.refreshTokenExpired.collect {
                Log.e("토큰", "토큰 만료됨.")
                navController.navigate("Login")
            }
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
                        Log.e("TAG", "event로 받은 oAuthEmail: ${event.oAuthEmail}")
                        loginViewModel.updateUserId(event.userId)
                        loginViewModel.clearNavigationEvent()
                    }
                    null -> {

                    }
                }
            }
        }


        destination.value?.let {
            startDestination ->
            NavHost(navController, startDestination, modifier = modifier) {
                composable("Splash") {
                    SplashScreen(navController, activity = this@MainActivity)
                }

                composable("Login") {
                    LoginScreen(loginViewModel, navController)
                }

                composable("PasswordResetScreen") {
                    PassWordResetScreen(navController)
                }

                composable("SignUpAgreeScreen1/{type}/{email}") {backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")?.toIntOrNull() ?: -1
                    val oAuthEmail = backStackEntry.arguments?.getString("email").toString()
                    SignUpAgreeScreen1(navController, type, oAuthEmail)
                }

                composable("SignUpCheckScreen2/{type}/{email}") {backStackEntry ->
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
                    SettingPermissionScreen(navController, permissionViewModel, this@MainActivity, type)
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

                composable("GlucoseRegisterScreen") { backStackEntry ->
                    GlucoseRegisterScreen(navController)
                }

                composable("ActivityRegisterScreen") { backStackEntry ->
                    ActivityRegisterScreen(navController)
                }

                composable("MyInfoScreen") { backStackEntry ->
                    MyInfoScreen(navController, bleViewModel)
                }

                composable("NotificationScreen") { backStackEntry ->
                    NotificationScreen(navController, bleViewModel)
                }

                composable("SensorInfoScreen") { backStackEntry ->
                    SensorInfoScreen(navController, bleViewModel)
                }

                composable("VersionInfoScreen") { backStackEntry ->
                    VersionInfoScreen(navController, bleViewModel)
                }

                composable("PrivacyPolicyScreen") { backStackEntry ->
                    PrivacyPolicyScreen(navController, bleViewModel)
                }

                composable("TermsAndConditionsScreen") { backStackEntry ->
                    TermsAndConditionsScreen(navController, bleViewModel)
                }

                composable("DeleteAccountScreen") { backStackEntry ->
                    DeleteAccountScreen(navController, bleViewModel)
                }
            }
        }

    }
}
