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
import kr.co.uxn.agms_p.ui.theme.AGMSPTheme
import kr.co.uxn.agms_p.ui.components.login.LoginScreen
import kr.co.uxn.agms_p.ui.components.splash.SplashScreen
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
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
                SplashScreen(navController)
            }

            composable("Login") {
                LoginScreen(loginViewModel, navController)
            }

//            navController.navigate("Office") {
//                popUpTo("Home") {
//                    inclusive = true
//                }
//            }

//            composable("Argument/{userId}") { backStackEntry ->
//                val userId = backStackEntry.arguments?.get("userId")
//                Text("userID는 $userId")
//            }
        }
    }
}
