package kr.co.uxn.agms_p_a2rt.ui.components.main.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.room.GlucoseAlert
import kr.co.uxn.agms_p_a2rt.room.GlucoseAlertType
import kr.co.uxn.agms_p_a2rt.ui.components.main.home.DemoGlucoseConfig
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AlertScreenBackground = Color(0xFFF7F4EE)
private val HighAlertColor = Color(0xFFEF5350)
private val HighAlertBackground = Color(0xFFFFF0EE)
private val LowAlertColor = Color(0xFF5C8FEF)
private val LowAlertBackground = Color(0xFFF1F5FF)

@Composable
fun AlertHistoryScreen(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userId by produceState(initialValue = -1) {
        value = DataStoreManager.getUserId().first() ?: -1
    }
    val alertFlow = remember(userId) {
        if (userId < 0) {
            flowOf(emptyList())
        } else {
            AppDatabase.getInstance(context)
                ?.dataDao()
                ?.observeGlucoseAlerts(userId)
                ?: flowOf(emptyList())
        }
    }
    val storedAlerts by alertFlow.collectAsState(initial = emptyList())
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    val alerts = if (DemoGlucoseConfig.ENABLED) {
        remember(userId) {
            createDemoAlerts(userId, System.currentTimeMillis())
        }
    } else {
        storedAlerts
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background_grey))
    ) {
        Text(
            text = "알림",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF241F1B),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)
        )

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "고혈당·저혈당 알림이 없습니다.",
                    color = Color(0xFF8B8178),
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    GlucoseAlertCard(alert = alert, currentTime = currentTime)
                }
            }
        }
    }
}

@Composable
private fun GlucoseAlertCard(alert: GlucoseAlert, currentTime: Long) {
    val isHigh = alert.alertType == GlucoseAlertType.HIGH
    val accentColor = if (isHigh) HighAlertColor else LowAlertColor
    val backgroundColor = if (isHigh) HighAlertBackground else LowAlertBackground
    val title = if (isHigh) {
        "혈당 높음: ${alert.glucose} mg/dL"
    } else {
        "혈당 낮음: ${alert.glucose} mg/dL"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.38f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.notification_selected),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column {
                Text(
                    text = title,
                    color = Color(0xFF2E2925),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = formatAlertTime(alert.createdAtLong, currentTime),
                    color = Color(0xFF958A80),
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun formatAlertTime(createdAtLong: Long, currentTime: Long): String {
    val elapsedMillis = (currentTime - createdAtLong).coerceAtLeast(0L)
    val elapsedMinutes = elapsedMillis / 60_000L

    return when {
        elapsedMinutes == 0L -> "방금 전"
        elapsedMinutes < 60L -> "${elapsedMinutes}분 전"
        elapsedMinutes < 24L * 60L -> "${elapsedMinutes / 60L}시간 전"
        else -> Instant.ofEpochMilli(createdAtLong)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d일 HH:mm", Locale.getDefault()))
    }
}

private fun createDemoAlerts(userId: Int, currentTime: Long): List<GlucoseAlert> {
    return listOf(
        GlucoseAlert(-1, userId, GlucoseAlertType.HIGH, 215, currentTime - 4 * 60_000L),
        GlucoseAlert(-2, userId, GlucoseAlertType.LOW, 68, currentTime - 19 * 60_000L),
        GlucoseAlert(-3, userId, GlucoseAlertType.HIGH, 192, currentTime - 67 * 60_000L),
        GlucoseAlert(-4, userId, GlucoseAlertType.LOW, 64, currentTime - 143 * 60_000L),
        GlucoseAlert(-5, userId, GlucoseAlertType.LOW, 55, currentTime - 318 * 60_000L)
    )
}
