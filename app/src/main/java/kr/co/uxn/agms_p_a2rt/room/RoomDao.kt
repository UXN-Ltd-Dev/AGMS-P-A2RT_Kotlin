package kr.co.uxn.agms_p_a2rt.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    // DB에 데이터 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserValue(userValeList: List<UserValue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucose(glucoseList: List<UserGlucose>)

    @Insert
    suspend fun insertGlucoseAlert(alert: GlucoseAlert)

    @Query(
        "SELECT * FROM GlucoseAlert " +
            "WHERE uxn_user_id = :userId " +
            "ORDER BY created_at_long DESC LIMIT :limit"
    )
    fun observeGlucoseAlerts(userId: Int, limit: Int = 100): Flow<List<GlucoseAlert>>

    @Query("DELETE FROM GlucoseAlert WHERE uxn_user_id = :userId")
    suspend fun deleteUserGlucoseAlertTable(userId: Int)

    @Transaction
    suspend fun replaceUserGlucoseTable(userId: Int, glucoseList: List<UserGlucose>) {
        deleteUserGlucoseTable(userId)
        insertGlucose(glucoseList)
    }
    // 캘리 저장하기
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalibration(calibrationData: UserCalibration)

    // userId의 lastTime 이후의 데이터 가져오기
    @Query("SELECT * FROM UserValue WHERE uxn_user_id = :userId AND created_at_long > :lastTime")
    suspend fun getListAfterLastTime(userId: Int, lastTime: Long): List<UserValue>

    @Query("DELETE FROM UserGlucose WHERE uxn_user_id = :userId")
    suspend fun deleteUserGlucoseTable(userId: Int)

    @Query("DELETE FROM UserValue WHERE uxn_user_id = :userId")
    suspend fun deleteUserValueTable(userId: Int)

    @Query("DELETE FROM UserCalibration WHERE user_id = :userId")
    suspend fun deleteUserCalibrationTable(userId: Int)

    // userId의 glucoseList 가져오기
    @Query("SELECT * FROM UserGlucose WHERE uxn_user_id = :userId")
    suspend fun getGlucoseList(userId: Int): List<UserGlucose>

    @Query("SELECT * FROM UserGlucose WHERE uxn_user_id = :userId AND created_at_long > :lastTime")
    suspend fun getGlucoseListAfterLastTime(userId: Int, lastTime: Long): List<UserGlucose>

    @Query(
        "SELECT * FROM UserGlucose " +
            "WHERE uxn_user_id = :userId " +
            "AND created_at_long >= :startTime " +
            "AND created_at_long < :endTime " +
            "ORDER BY created_at_long ASC"
    )
    suspend fun getGlucoseListBetween(
        userId: Int,
        startTime: Long,
        endTime: Long
    ): List<UserGlucose>

    // 캘리 불러오기
    @Query("SELECT * FROM UserCalibration WHERE user_id = :userId")
    suspend fun getCalibrationList(userId: Int): List<UserCalibration>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDummyList(dataList: List<DummyValue>)

    // 센서파라미터
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorParameters(sensorParameters: MutableList<SensorParameter>)

    @Query("DELETE FROM SensorParameter WHERE user_id = :userId")
    suspend fun deleteSensorParameters(userId: Int)

    @Query("SELECT * FROM SensorParameter WHERE user_id = :userId ORDER BY times ASC")
    suspend fun getSensorParameters(userId: Int): MutableList<SensorParameter>

    // A2RT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertA2RTData(data: A2RTData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertA2RTDataList(data: List<A2RTData>)

    @Query("SELECT * FROM A2RTData WHERE user_device_id = :userDeviceId AND created_at > :targetTime ORDER BY created_at ASC")
    suspend fun getA2RTDataList(userDeviceId: Int, targetTime: Long): List<A2RTData>

}
