package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ui.components.isKorea

@Composable
fun GuideScreen1(
    navController: NavController,
    activity: Activity
) {

    val context = LocalContext.current
    val activityContext = context as Activity

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.background_white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.background_white))
                    .height(70.dp)
                    .align(Alignment.Start)
                    .padding(start = 30.dp, top = 30.dp),
            ) {
                // 백 버튼
                Image(
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "백 버튼",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )
            }

            // 사용 설명
            Text(
                text = stringResource(R.string.guide_title),
                fontSize = 33.sp,
                color = colorResource(R.color.black)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 이미지
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 10.dp),
                color = colorResource(R.color.background_white)
            ) {
                if (isKorea()) {
                    Image(
                        painter = painterResource(R.drawable.guide_illustration1),
                        contentDescription = "가이드 일러스트1",
                        modifier = Modifier.size(300.dp),
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.guide_eng_illustration1),
                        contentDescription = "가이드 일러스트1",
                        modifier = Modifier.size(300.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.size(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 30.dp),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.guide_description_1),
                fontSize = 20.sp,
            )

            Spacer(modifier = Modifier.weight(1f))


            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(bottom = 15.dp)
            ) {
                Image(
                    painter = painterResource(id = if(isKorea()) R.drawable.btn_next else R.drawable.btn_eng_next),
                    contentDescription = "다음 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            navController.navigate("GuideScreen2")
                        }
                )
            }
        }
    }
}