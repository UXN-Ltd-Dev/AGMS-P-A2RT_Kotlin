package kr.co.uxn.agms_p

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.ble.AlwaysService
import kr.co.uxn.agms_p.ui.theme.AGMSPTheme
import kr.co.uxn.agms_p.ui.components.login.LoginScreen
import kr.co.uxn.agms_p.ui.components.login.SignUpAgreeScreen1
import kr.co.uxn.agms_p.ui.components.login.SignUpCheckScreen2
import kr.co.uxn.agms_p.ui.components.login.SignUpInfoScreen3
import kr.co.uxn.agms_p.ui.components.main.event.ActivityRegisterScreen
import kr.co.uxn.agms_p.ui.components.main.event.GlucoseRegisterScreen
import kr.co.uxn.agms_p.ui.components.main.MainScreen
import kr.co.uxn.agms_p.ui.components.main.setting.DeleteAccountScreen
import kr.co.uxn.agms_p.ui.components.main.setting.MyInfoScreen
import kr.co.uxn.agms_p.ui.components.main.setting.NotificationScreen
import kr.co.uxn.agms_p.ui.components.main.setting.PrivacyPolicyScreen
import kr.co.uxn.agms_p.ui.components.main.setting.SensorInfoScreen
import kr.co.uxn.agms_p.ui.components.main.setting.TermsAndConditionsScreen
import kr.co.uxn.agms_p.ui.components.ready.StabilizationCompleteScreen
import kr.co.uxn.agms_p.ui.components.ready.EnterFirstGlucose
import kr.co.uxn.agms_p.ui.components.ready.SettingPermissionScreen
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen1
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen2
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen3
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen4
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen5
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen6
import kr.co.uxn.agms_p.ui.components.ready.RegisterDeviceScreen
import kr.co.uxn.agms_p.ui.components.ready.ScanDeviceScreen
import kr.co.uxn.agms_p.ui.components.ready.ScanFailScreen
import kr.co.uxn.agms_p.ui.components.ready.StabilizationScreen
import kr.co.uxn.agms_p.ui.components.splash.SplashScreen
import kr.co.uxn.agms_p.ui.viewmodel.AuthEventNotifier
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel
import kr.co.uxn.agms_p.ui.viewmodel.LoginNavigationEvent
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel
import kr.co.uxn.agms_p.ui.viewmodel.PermissionViewModel
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val bleViewModel: BleViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val eventScreenViewModel: EventScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    @Composable
    fun Navigation(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController()
    ) {
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
                        navController.navigate("SettingPermissionScreen")
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



        NavHost(navController, "Splash", modifier = modifier) {
            composable("Splash") {
                SplashScreen(navController, activity = this@MainActivity)
            }

            composable("Login") {
                LoginScreen(loginViewModel, navController)
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

            composable("SignUpInfoScreen3/{email}/{pwd}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")?.let {
                    URLDecoder.decode(it, "UTF-8")
                } ?: ""
                val pwd = backStackEntry.arguments?.getString("pwd").toString()
                SignUpInfoScreen3(navController, email, pwd, loginViewModel)
            }

            composable("SettingPermissionScreen") { backStackEntry ->
                SettingPermissionScreen(navController, permissionViewModel, this@MainActivity)
            }

            composable("GuideScreen1") { backStackEntry ->
                GuideScreen1(navController)
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

            composable("StabilizationScreen") { backStackEntry ->
                StabilizationScreen(navController, bleViewModel)
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
                GlucoseRegisterScreen(navController, eventScreenViewModel)
            }

            composable("ActivityRegisterScreen") { backStackEntry ->
                ActivityRegisterScreen(navController, eventScreenViewModel)
            }

            composable("MyInfoScreen") { backStackEntry ->
                MyInfoScreen(navController, eventScreenViewModel)
            }

            composable("NotificationScreen") { backStackEntry ->
                NotificationScreen(navController, eventScreenViewModel)
            }

            composable("SensorInfoScreen") { backStackEntry ->
                SensorInfoScreen(navController, eventScreenViewModel)
            }

            composable("PrivacyPolicyScreen") { backStackEntry ->
                PrivacyPolicyScreen(navController, eventScreenViewModel)
            }

            composable("TermsAndConditionsScreen") { backStackEntry ->
                TermsAndConditionsScreen(navController, eventScreenViewModel)
            }

            composable("DeleteAccountScreen") { backStackEntry ->
                DeleteAccountScreen(navController, eventScreenViewModel)
            }

        }
    }
}
