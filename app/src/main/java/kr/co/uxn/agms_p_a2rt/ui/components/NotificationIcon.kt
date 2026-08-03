package kr.co.uxn.agms_p_a2rt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.uxn.agms_p_a2rt.R

@Composable
fun NotificationIcon(
    modifier: Modifier = Modifier,
    size: Dp = 25.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(colorResource(R.color.main), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "!",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
