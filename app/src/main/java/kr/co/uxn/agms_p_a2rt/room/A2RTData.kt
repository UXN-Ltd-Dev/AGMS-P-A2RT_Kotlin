package kr.co.uxn.agms_p_a2rt.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnull

@Entity(primaryKeys = ["user_device_id", "frequency_type", "created_at"])
data class A2RTData(
    @Nonnull
    @ColumnInfo(name = "user_device_id")
    val userDeviceId: Int,

    @Nonnull
    @ColumnInfo(name = "frequency_type")
    val frequencyType: String,

    @Nonnull
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "temperature")
    val temperature: Double,

    @ColumnInfo(name = "real")
    val real: Long,

    @ColumnInfo(name = "imaginary")
    val imaginary: Long,

    @ColumnInfo(name = "magnitude")
    val magnitude: Long,

    @ColumnInfo(name = "phase")
    val phase: Long
)
