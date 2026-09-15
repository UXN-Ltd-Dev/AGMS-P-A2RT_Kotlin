package kr.co.uxn.agms_p_a2rt.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
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

    /**
     * 저장할 사진의 긴 변 최대 길이(px).
     *
     * 요즘 폰 카메라는 4000px 을 넘게 찍는데 그대로 두면 한 장이 3~5MB 다.
     * 화면에서 확인하는 용도라 1600px 이면 확대해 봐도 음식이 또렷하다.
     */
    private const val MAX_EDGE = 1600

    /** JPEG 품질. 85 부터는 눈으로 차이를 알기 어렵고 용량은 크게 준다. */
    private const val JPEG_QUALITY = 85

    private fun dir(context: Context): File =
        File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }

    /**
     * 기록 시각 하나에 사진 하나. 같은 시각에 다시 찍으면 덮어쓴다.
     *
     * 초 단위로 잘라서 이름을 짓는다. 서버로 보내는 기록 시각이 "yyyy-MM-dd HH:mm:ss" 라
     * 되읽을 때 밀리초가 사라진다. 저장할 때 밀리초를 남겨 두면 이름이 어긋나 사진을 못 찾는다.
     */
    fun fileFor(context: Context, recordTimeMillis: Long): File =
        File(dir(context), "meal_${recordTimeMillis / 1000}.jpg")

    /** 카메라 앱에 넘길 URI. FileProvider 를 거쳐야 다른 앱이 쓸 수 있다. */
    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** 저장된 사진. 없으면 null 이라 호출부가 분기하기 쉽다. */
    fun photoOf(context: Context, recordTimeMillis: Long): File? {
        fileFor(context, recordTimeMillis)
            .takeIf { it.exists() && it.length() > 0 }
            ?.let { return it }

        // 초 단위로 자르기 전에 저장한 사진은 이름에 밀리초가 붙어 있다. 같은 초에 찍힌
        // 파일이면 그것으로 본다. 이 규칙이 없으면 예전 사진이 영영 안 보인다.
        val second = recordTimeMillis / 1000
        return dir(context).listFiles()
            ?.firstOrNull { file ->
                val stamp = file.name.removePrefix("meal_").removeSuffix(".jpg").toLongOrNull()
                stamp != null && stamp / 1000 == second && file.length() > 0
            }
    }

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

        val before = temp.length()
        if (!compressInto(temp, target)) {
            // 줄이지 못했으면 원본이라도 남긴다. 사진이 사라지는 것보다 낫다.
            temp.copyTo(target, overwrite = true)
        }
        temp.delete()
        Log.d(TAG, "사진 저장 ${before / 1024}KB -> ${target.length() / 1024}KB")
        true
    }.getOrElse {
        Log.w(TAG, "사진 저장 실패", it)
        false
    }

    /**
     * 긴 변을 [MAX_EDGE] 로 줄이고 JPEG 로 다시 압축한다.
     *
     * 두 단계로 줄인다. 먼저 inSampleSize 로 메모리에 올릴 때부터 작게 읽는다.
     * 4000px 짜리를 통째로 올리면 그것만으로 60MB 라 기기에 따라 OOM 이 난다.
     * 그다음 정확한 크기로 맞추고 압축한다.
     *
     * 카메라가 남긴 회전 정보(EXIF)는 다시 쓰는 과정에서 사라지므로 미리 적용해 둔다.
     * 그러지 않으면 저장한 사진이 옆으로 눕는다.
     */
    private fun compressInto(source: File, target: File): Boolean = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

        val longEdge = maxOf(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (longEdge / sample > MAX_EDGE * 2) sample *= 2

        val decoded = BitmapFactory.decodeFile(
            source.path,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: return false

        val scaled = scaleToMaxEdge(decoded)
        val rotated = applyExifRotation(source, scaled)

        target.outputStream().use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        if (rotated !== decoded) rotated.recycle()
        decoded.recycle()
        target.length() > 0
    }.getOrElse {
        Log.w(TAG, "사진 압축 실패. 원본을 그대로 쓴다.", it)
        false
    }

    private fun scaleToMaxEdge(bitmap: Bitmap): Bitmap {
        val longEdge = maxOf(bitmap.width, bitmap.height)
        if (longEdge <= MAX_EDGE) return bitmap
        val ratio = MAX_EDGE.toFloat() / longEdge
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun applyExifRotation(source: File, bitmap: Bitmap): Bitmap = runCatching {
        val degrees = when (
            ExifInterface(source.path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }.getOrElse { bitmap }

    /**
     * 앨범에서 고른 사진을 촬영본과 같은 임시 파일에 담는다.
     *
     * 여기서는 바이트만 옮긴다. 크기를 줄이고 회전을 바로잡는 일은 [commit] 이 어차피
     * 다시 하므로, 두 경로가 같은 자리에서 만나게 두는 편이 갈래가 적다.
     */
    fun copyInto(context: Context, source: Uri, target: File): Boolean = runCatching {
        val copied = context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        copied != null && target.length() > 0
    }.getOrElse {
        Log.w(TAG, "앨범 사진 복사 실패", it)
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
