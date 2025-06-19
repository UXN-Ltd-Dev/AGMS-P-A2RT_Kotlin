package kr.co.uxn.agms_p.ui.viewmodel

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
import kr.co.uxn.agms_p.api.token.DataStoreManager
import java.time.LocalDateTime
import java.time.ZoneId

class HomeViewModel : ViewModel() {
    //측정 종료 상태
    var hour: Int = 0

    val DAY_MILLISECONDS: Long = 1000 * 60 * 60 * 24 // 1일
//    val HOUR_MILLISECONDS: Long = 1000 * 60 * 60     // 1시간
    val HOUR_MILLISECONDS: Long = 1000 * 60 * 30     // 30분

    private val _day = MutableStateFlow(14)  // 시연용
    //    private val _day = MutableStateFlow(0) // 실제
    val day: StateFlow<Int> = _day.asStateFlow()

    private var daySchedulerJob: Job? = null

    private val _isEndMeasurement = MutableStateFlow(false)
    val isEndMeasurement: StateFlow<Boolean> = _isEndMeasurement.asStateFlow()

    var timeEnd: Long = 0
    val zoneId = ZoneId.of("Asia/Seoul")

    fun startTimer() {
        daySchedulerJob?.cancel()

        daySchedulerJob = viewModelScope.launch {

            timeEnd = DataStoreManager.getEndTime().first() ?: 0

            while (!isEndMeasurement.value) {
                Log.e("TEST", "타이머 실행 중 인 뷰모델")
                val diffTime: Long =
                    timeEnd - LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli()

                _day.value = (diffTime / DAY_MILLISECONDS).toInt() + 1

                Log.e("TEST", "남은day : ${day.value}")

                if (_day.value == 1) {
                    updateIsEndMeasurement(true)
                    Log.e("TEST", "타이머 종료")
                }

                delay(1000 * 60 * 60 * 1 ) // 1시간
//                delay(1000 * 5) // 5초 (테스트)

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