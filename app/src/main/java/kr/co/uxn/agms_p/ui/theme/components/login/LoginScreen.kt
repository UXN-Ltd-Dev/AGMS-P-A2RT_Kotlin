package kr.co.uxn.agms_p.ui.theme.components.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kr.co.uxn.agms_p.KakaoAuthViewModel
import kr.co.uxn.agms_p.R

@Composable
fun LoginScreen(viewModel: KakaoAuthViewModel, navController: NavController) {

    val isLoggedIn = viewModel.isLoggedIn.collectAsState()

    val userInfo = viewModel.userInfo.collectAsState()
    val context = LocalContext.current
//    val isLoggedIn by remember { viewModel.isLoggedIn.collectAsState() }

    val loginStatusInfoTitle = if (isLoggedIn.value) "로그인 상태" else "로그아웃 상태"
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.kakao_login_large_wide),
            contentDescription = "카카오 로그인 로고",
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clickable {
                    viewModel.kakaoLogin(context)
                }
        )
        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = {
            viewModel.kakaoLogout()
        }) {
            Text("로그아웃")
        }


        Spacer(modifier = Modifier.height(40.dp))
        Text(text = loginStatusInfoTitle)

        Button(onClick = {
            viewModel.getUserInfo()
        }) {
            Text("유저 정보 불러오기")
        }
        AsyncImage(
            model = userInfo.value.profileImageUrl,
            contentDescription = "프로필 이미지"
        )
        Text(text = "ID: ${userInfo.value.id}\n" +
                "Email: ${userInfo.value.email}\n" +
                "Nickname: ${userInfo.value.nickname}\n")
    }
}