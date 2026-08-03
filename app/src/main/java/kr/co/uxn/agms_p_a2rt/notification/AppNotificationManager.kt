package kr.co.uxn.agms_p_a2rt.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.first
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager

enum class AlertChannel(
    val channelId: String,
    val channelName: String,
    val description: String
) {
    GLUCOSE(
        channelId = "glucose_alert_channel",
        channelName = "Glucose Alerts",
        description = "Alerts for high or low glucose levels"
    ),
    BLE_DISCONNECTED(
        channelId = "ble_disconnect_alert_channel",
        channelName = "BLE disconnect alerts",
        description = "Alerts when the BLE connection is lost"
    ),
    BLE_STATUS(
        channelId = "ble_connect_channel",
        channelName = "BLE status alerts",
        description = "Alerts about Bluetooth status"
    ),
    STABILIZATION(
        channelId = "stabilization_channel",
        channelName = "Stabilization alerts",
        description = "Alerts when sensor stabilization is complete"
    )
}

object AppNotificationManager {
    private const val SILENT_CHANNEL_SUFFIX = "_silent_v1"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun notify(
        context: Context,
        channel: AlertChannel,
        title: String,
        message: String,
        notificationId: Int,
        autoCancel: Boolean = false
    ) {
        val isSilent = DataStoreManager.getNotiSilentMode().first()
        val channelId = if (isSilent) {
            channel.channelId + SILENT_CHANNEL_SUFFIX
        } else {
            channel.channelId
        }

        createChannel(context, channel, channelId, isSilent)

        val builder = NotificationCompat.Builder(context, channelId)
            .setOngoing(false)
            .setAutoCancel(autoCancel)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher_round)

        if (isSilent && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder
                .setSound(null)
                .setVibrate(longArrayOf(0L))
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun createChannel(
        context: Context,
        channel: AlertChannel,
        channelId: String,
        isSilent: Boolean
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationChannel = NotificationChannel(
            channelId,
            if (isSilent) "${channel.channelName} - Silent" else channel.channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = channel.description
            if (isSilent) {
                setSound(null, null)
                enableVibration(false)
            }
        }

        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(notificationChannel)
    }
}
