package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p_a2rt.R

@Composable
fun GuideScreen(
    navController: NavController,
    activity: Activity
) {

    val context = LocalContext.current
    val activityContext = context as Activity

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    fun checkIgnoringBatteryOptimizations(activity: Activity): Boolean {
        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(activity.packageName)
    }


    LaunchedEffect(Unit) {
        if(!checkIgnoringBatteryOptimizations(activityContext)) {
            val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
            activityContext.startActivity(intent)
        }
    }

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
            )

            // 사용 설명
            Text(
                text = stringResource(R.string.guide_notice_title),
                fontSize = 33.sp,
                color = colorResource(R.color.black)
            )

            Spacer(modifier = Modifier.weight(0.3f))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    textAlign = TextAlign.Start,
                    text = stringResource(R.string.guide_notice_description),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(bottom = 15.dp)
            ) {
                Image(
//                    painter = painterResource(id = if(isKorean) R.drawable.btn_next else R.drawable.btn_eng_continue),
                    painter = painterResource(id = R.drawable.btn_next),
                    contentDescription = "다음 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            navController.navigate("GuideScreen1")
                        }
                )
            }
        }
    }
}