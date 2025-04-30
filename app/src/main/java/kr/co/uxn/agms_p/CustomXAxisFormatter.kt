package kr.co.uxn.agms_p

import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CustomXAxisFormatter(
    private val baseTime: Long,
    private val totalEntryCount: Int,
    private val interval: Int,
) : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {

    private val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }


//    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
//        val isLatest = value.toInt() == chartValues.maxX.toInt()
//
//        return if (isLatest) {
//            val millis = baseTime + value.toInt() * 60000L
//            formatter.format(Date(millis))
//        } else {
//            "" // 나머지는 표시 안 함
//        }
//    }

    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        val xValue = value.toInt()
        val maxX = chartValues.maxX.toInt()

        // 최신값은 무조건 표시
        if (xValue == maxX) {
            val millis = baseTime + xValue * 60_000L
            return formatter.format(Date(millis))
        } else {
            return ""
        }


        // 나머지 값 중 3개만 간격으로 표시
//        val spacing = totalEntryCount / 4 // 3개 정도 간격 만들기
//        return if (spacing > 0 && xValue % spacing == 0) {
//            val millis = baseTime + xValue * 60_000L
//            formatter.format(Date(millis))
//        } else {
//            ""
//        }
    }

}