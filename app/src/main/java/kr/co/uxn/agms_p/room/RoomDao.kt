package kr.co.uxn.agms_p.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RoomDao {
    // DB에 데이터 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserValue(userValeList: List<UserValue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucose(glucoseList: List<UserGlucose>)

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

    // 캘리 불러오기
    @Query("SELECT * FROM UserCalibration WHERE user_id = :userId")
    suspend fun getCalibrationList(userId: Int): List<UserCalibration>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDummyList(dataList: List<DummyValue>)

}
