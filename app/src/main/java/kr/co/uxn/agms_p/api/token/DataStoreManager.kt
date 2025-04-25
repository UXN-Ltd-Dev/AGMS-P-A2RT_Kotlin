package kr.co.uxn.agms_p.api.token

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.uxn.agms_p.ble.Device

object DataStoreManager {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var notiStore: DataStore<Preferences>
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val EMAIL = stringPreferencesKey("email")
    private val USER_ID = intPreferencesKey("user_id")
    private val START_TIME = longPreferencesKey("start_time")
    private val MEASUREMENT_TIME = longPreferencesKey("measurement_time")
    private val END_TIME = longPreferencesKey("end_time")
    private val DEVICE_MAC = stringPreferencesKey("device_mac")
    private val IS_MAIN = booleanPreferencesKey("is_main")
    private val ROUTE = stringPreferencesKey("route")
    private val TYPE = intPreferencesKey("type")

    // noti
    private val NOTI_HIGH_GLUCOSE = booleanPreferencesKey("noti_high_glucose")
    private val NOTI_LOW_GLUCOSE = booleanPreferencesKey("noti_low_glucose")
    private val NOTI_LOST_SIGNAL = booleanPreferencesKey("noti_lost_signal")
    private val NOTI_EXPIRED_SENSOR = booleanPreferencesKey("noti_expired_sensor")
    private val NOTI_STABILIZATION = booleanPreferencesKey("noti_stabilization")
    private val NOTI_CALIBRATION = booleanPreferencesKey("noti_calibration")
    private val TARGET_HIGH_GLUCOSE = intPreferencesKey("target_high_glucose")
    private val TARGET_LOW_GLUCOSE = intPreferencesKey("target_low_glucose")
    private val DAILY_CALIBRATION_TIME = stringPreferencesKey("daily_calibration_time")

    fun init(context: Context) {
        dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("token_prefs")
        }

        notiStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("noti_prefs")
        }
    }

    fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN_KEY]
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }
    }

    fun getEmail(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[EMAIL]
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data.map { prefs ->
            prefs[USER_ID]
        }
    }

    fun getDeviceMac(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[DEVICE_MAC]
        }
    }

    fun getStartTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            prefs[START_TIME]
        }
    }

    fun getMeasurementTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            prefs[MEASUREMENT_TIME]
        }
    }

    fun getEndTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            prefs[END_TIME]
        }
    }

    fun getIsMain(): Flow<Boolean?> {
        return dataStore.data.map { prefs ->
            prefs[IS_MAIN]
        }
    }

    fun getRoute(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[ROUTE]
        }
    }

    fun getType(): Flow<Int?> {
        return dataStore.data.map { prefs ->
            prefs[TYPE]
        }
    }


    suspend fun saveEmail(email: String) {
        dataStore.edit { prefs ->
            prefs[EMAIL] = email
        }
    }

    suspend fun saveIsMain(isMain: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_MAIN] = isMain
        }
    }

    suspend fun saveUserId(userId: Int) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    suspend fun saveDeviceMac(deviceMac: String) {
        dataStore.edit { prefs ->
            prefs[DEVICE_MAC] = deviceMac
        }
    }

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveStartTime(StartTime: Long) {
        dataStore.edit { prefs ->
            prefs[START_TIME] = StartTime
        }
    }

    suspend fun saveMeasurementTime(MeasurementTime: Long) {
        dataStore.edit { prefs ->
            prefs[MEASUREMENT_TIME] = MeasurementTime
        }
    }

    suspend fun saveEndTime(EndTime: Long) {
        dataStore.edit { prefs ->
            prefs[END_TIME] = EndTime
        }
    }

    suspend fun saveRoute(route: String) {
        dataStore.edit { prefs ->
            prefs[ROUTE] = route
        }
    }

    suspend fun saveType(type: Int) {
        dataStore.edit { prefs ->
            prefs[TYPE] = type
        }
    }

    suspend fun deleteAccessToken() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
        }
    }

    suspend fun deleteRefreshToken() {
        dataStore.edit { prefs ->
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }

    suspend fun deleteUserId() {
        dataStore.edit { prefs ->
            prefs.remove(USER_ID)
        }
    }

    suspend fun deleteDeviceMac() {
        dataStore.edit { prefs ->
            prefs.remove(DEVICE_MAC)
        }
    }

    suspend fun deleteStartTime() {
        dataStore.edit { prefs ->
            prefs.remove(START_TIME)
        }
    }

    suspend fun deleteMeasurementTime() {
        dataStore.edit { prefs ->
            prefs.remove(MEASUREMENT_TIME)
        }
    }

    suspend fun deleteEndTime() {
        dataStore.edit { prefs ->
            prefs.remove(END_TIME)
        }
    }

    suspend fun deleteRoute() {
        dataStore.edit { prefs ->
            prefs.remove(ROUTE)
        }
    }

    suspend fun deleteType() {
        dataStore.edit { prefs ->
            prefs.remove(TYPE)
        }
    }

    suspend fun deleteDailyCalibrationTime() {
        dataStore.edit { prefs ->
            prefs.remove(DAILY_CALIBRATION_TIME)
        }
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = token
        }
    }

    // noti
    fun getNotiHighGlucose(): Flow<Boolean?> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_HIGH_GLUCOSE]
        }
    }
    fun getNotiLowGlucose(): Flow<Boolean?> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_LOW_GLUCOSE]
        }
    }

    fun getNotiLostSignal(): Flow<Boolean?> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_LOST_SIGNAL]
        }
    }

    fun getNotiExpiredSensor(): Flow<Boolean?> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_EXPIRED_SENSOR]
        }
    }

    fun getNotiStabilization(): Flow<Boolean?> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_STABILIZATION]
        }
    }

    fun getNotiCalibration(): Flow<Boolean?> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_CALIBRATION]
        }
    }

    fun getTargetHighGlucose(): Flow<Int?> {
        return notiStore.data.map { prefs ->
            prefs[TARGET_HIGH_GLUCOSE]
        }
    }

    fun getTargetLowGlucose(): Flow<Int?> {
        return notiStore.data.map { prefs ->
            prefs[TARGET_LOW_GLUCOSE]
        }
    }

    fun getDailyCalibrationTime(): Flow<String?> {
        return notiStore.data.map { prefs ->
            prefs[DAILY_CALIBRATION_TIME]
        }
    }

    suspend fun setNotiHighGlucose(isCheck: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_HIGH_GLUCOSE] = isCheck
        }
    }

    suspend fun setNotiLowGlucose(isCheck: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_LOW_GLUCOSE] = isCheck
        }
    }
    suspend fun setNotiLostSignal(isCheck: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_LOST_SIGNAL] = isCheck
        }
    }

    suspend fun setNotiExpiredSensor(isCheck: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_EXPIRED_SENSOR] = isCheck
        }
    }

    suspend fun setNotiStabilization(isCheck: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_STABILIZATION] = isCheck
        }
    }

    suspend fun setNotiCalibration(isCheck: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_CALIBRATION] = isCheck
        }
    }

    suspend fun setTargetHighGlucose(glucose: Int) {
        notiStore.edit { prefs ->
            prefs[TARGET_HIGH_GLUCOSE] = glucose
        }
    }

    suspend fun setTargetLowGlucose(glucose: Int) {
        notiStore.edit { prefs ->
            prefs[TARGET_LOW_GLUCOSE] = glucose
        }
    }

    suspend fun setDailyCalibrationTime(time: String) {
        notiStore.edit { prefs ->
            prefs[DAILY_CALIBRATION_TIME] = time
        }
    }
}