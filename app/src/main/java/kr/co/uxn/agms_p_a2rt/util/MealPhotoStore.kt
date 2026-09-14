package kr.co.uxn.agms_p_a2rt.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * 식사 기록 사진을 기기 안에 보관한다.
 *
 * 서버에는 보내지 않는다. 기록용 API(RequestEventData)에 사진을 담을 자리가 없고,
 * 앱에서 되짚어 보는 용도라 기기 안에 두는 것으로 충분하다.
 * 앱을 지우거나 기기를 바꾸면 사진도 함께 사라진다.
 *
 * 파일 이름을 기록 시각으로 정해 별도의 매핑 테이블을 두지 않는다.
 * Room 에 테이블을 더하면 스키마가 바뀌는데, 이 앱은 fallbackToDestructiveMigration 이라
 * 그 순간 기존 혈당·캘리브레이션 데이터가 전부 지워진다. 그 위험을 피한다.
 */
object MealPhotoStore {

    private const val TAG = "MealPhoto"
    private const val DIR = "meal_photos"

    private fun dir(context: Context): File =
        File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }

    /** 기록 시각 하나에 사진 하나. 같은 시각에 다시 찍으면 덮어쓴다. */
    fun fileFor(context: Context, recordTimeMillis: Long): File =
        File(dir(context), "meal_$recordTimeMillis.jpg")

    /** 카메라 앱에 넘길 URI. FileProvider 를 거쳐야 다른 앱이 쓸 수 있다. */
    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** 저장된 사진. 없으면 null 이라 호출부가 분기하기 쉽다. */
    fun photoOf(context: Context, recordTimeMillis: Long): File? =
        fileFor(context, recordTimeMillis).takeIf { it.exists() && it.length() > 0 }

    /**
     * 임시로 찍어 둔 사진을 기록 시각 이름으로 옮긴다.
     *
     * 촬영 시점에는 사용자가 시각을 바꿀 수 있어 최종 시각을 알 수 없다. 그래서 임시 파일에
     * 먼저 담고, 저장할 때 옮긴다.
     */
    fun commit(context: Context, temp: File, recordTimeMillis: Long): Boolean = runCatching {
        if (!temp.exists() || temp.length() == 0L) return false
        val target = fileFor(context, recordTimeMillis)
        if (target.exists()) target.delete()
        temp.copyTo(target, overwrite = true)
        temp.delete()
        true
    }.getOrElse {
        Log.w(TAG, "사진 저장 실패", it)
        false
    }

    /** 촬영용 임시 파일. 저장하지 않고 화면을 나가면 그대로 남으므로 [clearTemp] 로 지운다. */
    fun tempFile(context: Context): File = File(dir(context), "temp_capture.jpg")

    fun clearTemp(context: Context) {
        runCatching { tempFile(context).delete() }
    }

    fun remove(context: Context, recordTimeMillis: Long) {
        runCatching { fileFor(context, recordTimeMillis).delete() }
    }
}
