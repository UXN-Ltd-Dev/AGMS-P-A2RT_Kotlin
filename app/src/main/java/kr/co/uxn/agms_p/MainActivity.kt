package kr.co.uxn.agms_p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.uxn.agms_p.ui.components.login.LoginPasswordScreen
import kr.co.uxn.agms_p.ui.theme.AGMSPTheme
import kr.co.uxn.agms_p.ui.components.login.LoginScreen
import kr.co.uxn.agms_p.ui.components.login.SignUpAgreeScreen1
import kr.co.uxn.agms_p.ui.components.login.SignUpCheckScreen2
import kr.co.uxn.agms_p.ui.components.login.SignUpInfoScreen3
import kr.co.uxn.agms_p.ui.components.ready.SettingPermissionScreen
import kr.co.uxn.agms_p.ui.components.ready.SettingScreen
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen1
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen2
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen3
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen4
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen5
import kr.co.uxn.agms_p.ui.components.ready.GuideScreen6
import kr.co.uxn.agms_p.ui.components.ready.RegisterDevice
import kr.co.uxn.agms_p.ui.components.splash.SplashScreen
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel
import kr.co.uxn.agms_p.ui.viewmodel.RegisterViewModel
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val registerViewModel: RegisterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AGMSPTheme {
                Navigation()
            }
        }
    }

    @Composable
    fun Navigation(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController()
    ) {
        // 단계 3: `NavHost`를 만듭니다.
        // `navController`, `Home`, `modifier`를 전달 합시다.
        NavHost(navController, "Splash", modifier = modifier) {
            composable("Splash") {
                SplashScreen(navController, activity = this@MainActivity)
            }

            composable("Login") {
                LoginScreen(loginViewModel, navController)
            }

            composable("SignUpAgreeScreen1") {
                SignUpAgreeScreen1(navController)
            }

            composable("SignUpCheckScreen2") {
                SignUpCheckScreen2(navController)
            }

            composable("SignUpInfoScreen3/{email}/{pwd}") {backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")?.let {
                    URLDecoder.decode(it, "UTF-8")
                } ?: ""
                val pwd = backStackEntry.arguments?.getString("pwd").toString()

                SignUpInfoScreen3(navController, email, pwd)
            }

            composable("LoginPassword/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email")?.let {
                    URLDecoder.decode(it, "UTF-8")
                } ?: ""
                LoginPasswordScreen(email, navController)
            }


            composable("SettingPermissionScreen") { backStackEntry ->
                SettingPermissionScreen(navController, registerViewModel, this@MainActivity)
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

            composable("RegisterDevice") { backStackEntry ->
                RegisterDevice(navController)
            }
            
            
            
        }
    }
}
