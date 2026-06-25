package kr.co.uxn.agms_p_a2rt.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["user_id", "times"])
data class SensorParameter(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "version")
    val version: Int,
    @ColumnInfo(name = "times")
    val times: Double,
    @ColumnInfo(name = "sensitivities")
    val sensitivities: Double,
    @ColumnInfo(name = "base_currents")
    val baseCurrents: Double,
    @ColumnInfo(name = "slopes")
    val slopes: Double,
    @ColumnInfo(name = "alphas")
    val alphas: Double
)
