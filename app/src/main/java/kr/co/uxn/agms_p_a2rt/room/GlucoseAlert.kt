package kr.co.uxn.agms_p_a2rt.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "GlucoseAlert",
    indices = [Index(value = ["uxn_user_id", "created_at_long"])]
)
data class GlucoseAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "uxn_user_id")
    val userId: Int,
    @ColumnInfo(name = "alert_type")
    val alertType: String,
    @ColumnInfo(name = "glucose")
    val glucose: Int,
    @ColumnInfo(name = "created_at_long")
    val createdAtLong: Long
)

object GlucoseAlertType {
    const val HIGH = "HIGH"
    const val LOW = "LOW"
}
