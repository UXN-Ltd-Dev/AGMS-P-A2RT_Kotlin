package kr.co.uxn.agms_p_a2rt.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import java.time.LocalDateTime
import java.time.ZoneId

class HomeViewModel : ViewModel() {
    //측정 종료 상태
    var hour: Int = 0

    val DAY_MILLISECONDS: Long = 1000 * 60 * 60 * 24 // 1일
    val MINUTE_MILLISECONDS: Long = 1000 * 60
//    val HOUR_MILLISECONDS: Long = 1000 * 60 * 60     // 1시간
    val HOUR_MILLISECONDS: Long = 1000 * 60 * 30     // 30분

    private val _day = MutableStateFlow(1)
    val day: StateFlow<Int> = _day.asStateFlow()

    private val _remainingTimeText = MutableStateFlow("")
    val remainingTimeText: StateFlow<String> = _remainingTimeText.asStateFlow()

    private var daySchedulerJob: Job? = null

    private val _isEndMeasurement = MutableStateFlow(false)
    val isEndMeasurement: StateFlow<Boolean> = _isEndMeasurement.asStateFlow()

    var timeEnd: Long = 0
    val zoneId = ZoneId.of("Asia/Seoul")

    fun startTimer(isKorean: Boolean) {
        daySchedulerJob?.cancel()

        daySchedulerJob = viewModelScope.launch {

            val timeStart = DataStoreManager.getStartTime().first() ?: 0L
            timeEnd = DataStoreManager.getEndTime().first() ?: 0L

            if (timeStart <= 0L || timeEnd <= 0L || timeEnd <= timeStart) {
                _day.value = 1
                _remainingTimeText.value = ""
                Log.w("TEST", "타이머 시간 정보 없음 startTime: $timeStart, endTime: $timeEnd")
                return@launch
            }

            while (!isEndMeasurement.value) {
                Log.e("TEST", "타이머 실행 중 인 뷰모델")
                val now = System.currentTimeMillis()
                val elapsedTime = now - timeStart
                val remainingTime = timeEnd - now

                val currentDay = (elapsedTime / DAY_MILLISECONDS).toInt() + 1
                _day.value = currentDay.coerceIn(1, 14)
                _remainingTimeText.value = formatRemainingTime(remainingTime, isKorean)

                Log.e("TEST", "현재 day : ${day.value}, 남은 시간 : ${remainingTimeText.value}")

                if (remainingTime <= 0L) {
                    updateIsEndMeasurement(true)
                    Log.e("TEST", "타이머 종료")
                }

                delay(1000 * 60) // 1분
//                delay(1000 * 5) // 5초 (테스트)

            }
        }
    }

    private fun formatRemainingTime(remainingTime: Long, isKorean: Boolean): String {
        if (remainingTime <= 0L) return if (isKorean) "0분" else "0m"

        return if (remainingTime < 1000 * 60 * 60) {
            val minutes = ((remainingTime + MINUTE_MILLISECONDS - 1) / MINUTE_MILLISECONDS)
                .coerceAtLeast(1)
            if (isKorean) {
                "${minutes}분"
            } else {
                "${minutes}m"
            }
        } else {
            val days = remainingTime / DAY_MILLISECONDS
            val hours = (remainingTime % DAY_MILLISECONDS) / (1000 * 60 * 60)
            if (isKorean) {
                "${days}일 ${hours}시간"
            } else {
                "${days}d ${hours}h"
            }
        }
    }

    fun startTimerForTest() {
        viewModelScope.launch {

            while (!isEndMeasurement.value) {
                Log.e("TEST", "타이머 실행 중 인 뷰모델")


                Log.e("TEST", "남은day : ${day.value}")
//                delay(1000 * 60 * 1) // 1분


                if (_day.value == 1) {
                    updateIsEndMeasurement(true)
                    Log.e("TEST", "타이머 종료")
                } else {
                    delay(1000 * 2) // 2초
                    _day.value = _day.value - 1
                }

            }
        }
    }

    fun updateIsEndMeasurement(boolean: Boolean) {
        _isEndMeasurement.value = boolean
    }
}
