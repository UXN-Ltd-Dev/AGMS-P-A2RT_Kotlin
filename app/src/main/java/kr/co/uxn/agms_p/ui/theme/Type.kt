package kr.co.uxn.agms_p.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kr.co.uxn.agms_p.R

private val spoqaHansSansNeoRegular = Font(R.font.spoqahansansneo_regular, FontWeight.Normal)
private val spoqaHansSansNeoBold = Font(R.font.spoqahansansneo_bold, FontWeight.Bold)
private val spoqaHansSansNeoThin = Font(R.font.spoqahansansneo_thin, FontWeight.Thin)

// 2. 폰트 '가족'을 코드로 정의합니다.
val spoqaHansSansNeoFamily = FontFamily(
    spoqaHansSansNeoRegular,
    spoqaHansSansNeoBold,
    spoqaHansSansNeoThin
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = spoqaHansSansNeoFamily,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = spoqaHansSansNeoFamily,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontFamily = spoqaHansSansNeoFamily,
        fontWeight = FontWeight.Thin,
//        fontSize = 11.sp,
//        lineHeight = 16.sp,
//        letterSpacing = 0.5.sp
    )
)