package kr.co.uxn.agms_p.ui.components.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {

    val isLoggedIn = viewModel.isLoggedIn.collectAsState()

    val userInfo = viewModel.userInfo.collectAsState()
    val context = LocalContext.current
//    val isLoggedIn by remember { viewModel.isLoggedIn.collectAsState() }

    val loginStatusInfoTitle = if (isLoggedIn.value) "로그인 상태" else "로그아웃 상태"

    val email = remember {mutableStateOf("")}

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(R.drawable.uxn_logo),
                contentDescription = "로고"
            )
            Text(text = "로그인을 위해 이메일을 입력하세요.")
            TextField(
                value = email.value,
                onValueChange = { email.value = it },
                placeholder = { Text("이메일 주소") }
            )
            Text(
                text = "계속 진행하려면 유엑스엔의 개인정보 처리방침 및 \n이용약관에 동의하게 됩니다.",
                fontSize = 12.sp
            )
            Text("간편 로그인")
            // 카카오 로그인 버튼
            Image(
                painter = painterResource(id = R.drawable.kakao_login_large_wide),
                contentDescription = "카카오 로그인",
                modifier = Modifier
                    .size(width = 300.dp, height = 50.dp)
                    .clickable {
                        viewModel.kakaoLogin(context)
                    }
            )
            Spacer(modifier = Modifier.height(10.dp))
            // 구글 로그인 버튼
            Button(
                onClick = {
                    viewModel.googleLogin(context)
                },
                modifier = Modifier
                    .size(width = 300.dp, height = 50.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(7.dp)),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(0.1.dp, Color.Gray),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, // 세로 중앙 정렬
                    horizontalArrangement = Arrangement.Start, // 왼쪽 정렬
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.android_light_rd_na),
                        contentDescription = "구글 로그인",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.size(80.dp))
                    Text(
                        text = "구글 로그인",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                    )
                }
            }
        }
    }
}
