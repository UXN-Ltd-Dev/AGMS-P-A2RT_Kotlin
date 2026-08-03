package kr.co.uxn.agms_p_a2rt.api.token

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kr.co.uxn.agms_p_a2rt.ble.Device

data class CachedUserInfo(
    val userId: Int,
    val email: String,
    val name: String,
    val sex: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    val diabetesType: String,
    val targetGlucoseMin: Int,
    val targetGlucoseMax: Int
)

object DataStoreManager {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var notiStore: DataStore<Preferences>

    // normal
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val PRE_TOKEN_KEY = stringPreferencesKey("pre_token")
    private val EMAIL = stringPreferencesKey("email")
    private val SAVED_LOGIN_EMAIL = stringPreferencesKey("saved_login_email")
    private val REMEMBER_LOGIN_EMAIL = booleanPreferencesKey("remember_login_email")
    private val USER_ID = intPreferencesKey("user_id")
    private val USER_DEVICE_ID = intPreferencesKey("user_device_id")
    private val START_TIME = longPreferencesKey("start_time")
    private val MEASUREMENT_TIME = longPreferencesKey("measurement_time")
    private val END_TIME = longPreferencesKey("end_time")
    private val DEVICE_MAC = stringPreferencesKey("device_mac")
    private val DEVICE_TYPE = intPreferencesKey("device_type")
    private val IS_MAIN = booleanPreferencesKey("is_main")
    private val IS_SENSOR_ENDED = booleanPreferencesKey("is_sensor_ended")
    private val ROUTE = stringPreferencesKey("route")
    private val TYPE = intPreferencesKey("type")
    private val SERIAL_NUMBER = stringPreferencesKey("serial_number")
    private val STABLE_FIRST_ACTIVATE = booleanPreferencesKey("stable_first_activate")
    private val STABILIZATION_COMPLETED = booleanPreferencesKey("stabilization_completed")
    private val IS_UPDATE_COMPLETED = booleanPreferencesKey("is_update_completed")
    private val CACHED_PROFILE_USER_ID = intPreferencesKey("cached_profile_user_id")
    private val CACHED_PROFILE_EMAIL = stringPreferencesKey("cached_profile_email")
    private val CACHED_PROFILE_NAME = stringPreferencesKey("cached_profile_name")
    private val CACHED_PROFILE_SEX = stringPreferencesKey("cached_profile_sex")
    private val CACHED_PROFILE_AGE = intPreferencesKey("cached_profile_age")
    private val CACHED_PROFILE_HEIGHT = intPreferencesKey("cached_profile_height")
    private val CACHED_PROFILE_WEIGHT = intPreferencesKey("cached_profile_weight")
    private val CACHED_PROFILE_DIABETES_TYPE =
        stringPreferencesKey("cached_profile_diabetes_type")
    private val CACHED_PROFILE_TARGET_MIN = intPreferencesKey("cached_profile_target_min")
    private val CACHED_PROFILE_TARGET_MAX = intPreferencesKey("cached_profile_target_max")


    // noti
    private val NOTI_HIGH_GLUCOSE = booleanPreferencesKey("noti_high_glucose")
    private val NOTI_LOW_GLUCOSE = booleanPreferencesKey("noti_low_glucose")
    private val NOTI_LOST_SIGNAL = booleanPreferencesKey("noti_lost_signal")
    private val NOTI_EXPIRED_SENSOR = booleanPreferencesKey("noti_expired_sensor")
    private val NOTI_STABILIZATION = booleanPreferencesKey("noti_stabilization")
    private val NOTI_CALIBRATION = booleanPreferencesKey("noti_calibration")
    private val NOTI_SILENT_MODE = booleanPreferencesKey("noti_silent_mode")
    private val TARGET_HIGH_GLUCOSE = intPreferencesKey("target_high_glucose")
    private val TARGET_LOW_GLUCOSE = intPreferencesKey("target_low_glucose")
    private val LANDSCAPE_MODE = booleanPreferencesKey("landscape_mode")
    private val DAILY_CALIBRATION_TIME = stringPreferencesKey("daily_calibration_time")
    private val DAILY_LAST_CALIBRATION_TIME = stringPreferencesKey("daily_last_calibration_time")

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

    fun getPreToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[PRE_TOKEN_KEY]
        }
    }

    fun getEmail(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[EMAIL]
        }
    }

    fun getSavedLoginEmail(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[SAVED_LOGIN_EMAIL]
        }
    }

    fun getRememberLoginEmail(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[REMEMBER_LOGIN_EMAIL] ?: false
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data.map { prefs ->
            prefs[USER_ID]
        }
    }

    fun getUserDeviceId(): Flow<Int?> {
        return dataStore.data.map { prefs ->
            prefs[USER_DEVICE_ID]
        }
    }

    fun getCachedUserInfo(userId: Int): Flow<CachedUserInfo?> {
        return dataStore.data.map { prefs ->
            if (prefs[CACHED_PROFILE_USER_ID] != userId) return@map null

            CachedUserInfo(
                userId = userId,
                email = prefs[CACHED_PROFILE_EMAIL] ?: return@map null,
                name = prefs[CACHED_PROFILE_NAME] ?: return@map null,
                sex = prefs[CACHED_PROFILE_SEX] ?: return@map null,
                age = prefs[CACHED_PROFILE_AGE] ?: return@map null,
                height = prefs[CACHED_PROFILE_HEIGHT] ?: return@map null,
                weight = prefs[CACHED_PROFILE_WEIGHT] ?: return@map null,
                diabetesType = prefs[CACHED_PROFILE_DIABETES_TYPE] ?: return@map null,
                targetGlucoseMin = prefs[CACHED_PROFILE_TARGET_MIN] ?: return@map null,
                targetGlucoseMax = prefs[CACHED_PROFILE_TARGET_MAX] ?: return@map null
            )
        }
    }

    fun getDeviceMac(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[DEVICE_MAC]
        }
    }

    fun getDeviceType(): Flow<Int?> {
        return dataStore.data.map { prefs ->
            prefs[DEVICE_TYPE]
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

    fun getIsSensorEnded(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[IS_SENSOR_ENDED] ?: false
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

    fun getSerialNumber(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[SERIAL_NUMBER]
        }
    }

    fun getFirstActivate(): Flow<Boolean?> {
        return dataStore.data.map { prefs ->
            prefs[STABLE_FIRST_ACTIVATE]
        }
    }

    fun getStabilizationCompleted(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[STABILIZATION_COMPLETED] ?: false
        }
    }

    fun getIsUpdateCompleted(): Flow<Boolean?> {
        return dataStore.data.map { prefs ->
            prefs[IS_UPDATE_COMPLETED]
        }
    }


    suspend fun saveEmail(email: String) {
        dataStore.edit { prefs ->
            prefs[EMAIL] = email
            Log.d("TEST", "email saved!  : $email")
        }
    }

    suspend fun saveLoginEmailPreference(email: String, rememberEmail: Boolean) {
        dataStore.edit { prefs ->
            prefs[REMEMBER_LOGIN_EMAIL] = rememberEmail
            if (rememberEmail) {
                prefs[SAVED_LOGIN_EMAIL] = email
            } else {
                prefs.remove(SAVED_LOGIN_EMAIL)
            }
        }
    }

    suspend fun saveIsMain(isMain: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_MAIN] = isMain
        }
    }

    suspend fun saveIsSensorEnded(isSensorEnded: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_SENSOR_ENDED] = isSensorEnded
        }
    }

    suspend fun saveUserId(userId: Int) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    suspend fun saveUserDeviceId(userDeviceId: Int) {
        dataStore.edit { prefs ->
            prefs[USER_DEVICE_ID] = userDeviceId
        }
    }

    suspend fun saveCachedUserInfo(userInfo: CachedUserInfo) {
        dataStore.edit { prefs ->
            prefs[CACHED_PROFILE_USER_ID] = userInfo.userId
            prefs[CACHED_PROFILE_EMAIL] = userInfo.email
            prefs[CACHED_PROFILE_NAME] = userInfo.name
            prefs[CACHED_PROFILE_SEX] = userInfo.sex
            prefs[CACHED_PROFILE_AGE] = userInfo.age
            prefs[CACHED_PROFILE_HEIGHT] = userInfo.height
            prefs[CACHED_PROFILE_WEIGHT] = userInfo.weight
            prefs[CACHED_PROFILE_DIABETES_TYPE] = userInfo.diabetesType
            prefs[CACHED_PROFILE_TARGET_MIN] = userInfo.targetGlucoseMin
            prefs[CACHED_PROFILE_TARGET_MAX] = userInfo.targetGlucoseMax
        }
    }

    suspend fun saveDeviceMac(deviceMac: String) {
        dataStore.edit { prefs ->
            prefs[DEVICE_MAC] = deviceMac
        }
    }

    suspend fun saveDeviceType(deviceType: Int) {
        dataStore.edit { prefs ->
            prefs[DEVICE_TYPE] = deviceType
        }
    }

    suspend fun replaceDeviceType(deviceType: Int) {
        dataStore.edit { prefs ->
            prefs.remove(DEVICE_TYPE)
            prefs[DEVICE_TYPE] = deviceType
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

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = token
        }
    }

    suspend fun saveSerialNumber(serialNumber: String) {
        dataStore.edit { prefs ->
            prefs[SERIAL_NUMBER] = serialNumber
        }
    }

    suspend fun saveIsUpdateCompleted(isUpdated: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_UPDATE_COMPLETED] = isUpdated
        }
    }

    suspend fun savePreToken(preToken: String) {
        dataStore.edit { prefs ->
            prefs[PRE_TOKEN_KEY] = preToken
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

    suspend fun deletePreToken() {
        dataStore.edit { prefs ->
            prefs.remove(PRE_TOKEN_KEY)
        }
    }

    suspend fun deleteUserId() {
        dataStore.edit { prefs ->
            prefs.remove(USER_ID)
            removeCachedUserInfo(prefs)
        }
    }

    suspend fun deleteCachedUserInfo() {
        dataStore.edit(::removeCachedUserInfo)
    }

    private fun removeCachedUserInfo(prefs: androidx.datastore.preferences.core.MutablePreferences) {
        prefs.remove(CACHED_PROFILE_USER_ID)
        prefs.remove(CACHED_PROFILE_EMAIL)
        prefs.remove(CACHED_PROFILE_NAME)
        prefs.remove(CACHED_PROFILE_SEX)
        prefs.remove(CACHED_PROFILE_AGE)
        prefs.remove(CACHED_PROFILE_HEIGHT)
        prefs.remove(CACHED_PROFILE_WEIGHT)
        prefs.remove(CACHED_PROFILE_DIABETES_TYPE)
        prefs.remove(CACHED_PROFILE_TARGET_MIN)
        prefs.remove(CACHED_PROFILE_TARGET_MAX)
    }

    suspend fun deleteUserDeviceId() {
        dataStore.edit { prefs ->
            prefs.remove(USER_DEVICE_ID)
        }
    }

    suspend fun deleteEmail() {
        dataStore.edit { prefs ->
            prefs.remove(EMAIL)
        }
    }

    suspend fun deleteDeviceMac() {
        dataStore.edit { prefs ->
            prefs.remove(DEVICE_MAC)
        }
    }

    suspend fun deleteDeviceType() {
        dataStore.edit { prefs ->
            prefs.remove(DEVICE_TYPE)
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
        notiStore.edit { prefs ->
            prefs.remove(DAILY_CALIBRATION_TIME)
        }
    }


    suspend fun deleteSerialNumber() {
        dataStore.edit { prefs ->
            prefs.remove(SERIAL_NUMBER)
        }
    }

    suspend fun deleteTargetLowGlucose() {
        notiStore.edit { prefs ->
            prefs.remove(TARGET_LOW_GLUCOSE)
        }
    }

    suspend fun deleteTargetHighGlucose() {
        notiStore.edit { prefs ->
            prefs.remove(TARGET_HIGH_GLUCOSE)
        }
    }

    suspend fun deleteDailyCalibrationLastTime() {
        notiStore.edit { prefs ->
            prefs.remove(DAILY_LAST_CALIBRATION_TIME)
        }
    }

    suspend fun deleteActivateFirst() {
        dataStore.edit { prefs ->
            prefs.remove(STABLE_FIRST_ACTIVATE)
        }
    }

    suspend fun deleteStabilizationCompleted() {
        dataStore.edit { prefs ->
            prefs.remove(STABILIZATION_COMPLETED)
        }
    }

    suspend fun deleteIsUpdateCompleted() {
        dataStore.edit { prefs ->
            prefs.remove(IS_UPDATE_COMPLETED)
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

    fun getNotiSilentMode(): Flow<Boolean> {
        return notiStore.data.map { prefs ->
            prefs[NOTI_SILENT_MODE] ?: false
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

    fun getDailyCalibrationLastTime(): Flow<String?> {
        return notiStore.data.map { prefs ->
            prefs[DAILY_LAST_CALIBRATION_TIME]
        }
    }

    fun getLandScapeMode(): Flow<Boolean?> {
        return dataStore.data.map { prefs ->
            prefs[LANDSCAPE_MODE]
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

    suspend fun setNotiSilentMode(isSilent: Boolean) {
        notiStore.edit { prefs ->
            prefs[NOTI_SILENT_MODE] = isSilent
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

    suspend fun setDailyCalibrationLastTime(time: String) {
        notiStore.edit { prefs ->
            prefs[DAILY_LAST_CALIBRATION_TIME] = time
        }
    }


    suspend fun setLandScapeMode(isCheck: Boolean) {
        dataStore.edit { prefs ->
            prefs[LANDSCAPE_MODE] = isCheck
        }
        Log.d("TEST", "landScapeMode : ${getLandScapeMode().first()}")
    }

    suspend fun setFirstActivateDB(isActivate: Boolean) {
        dataStore.edit { prefs ->
            prefs[STABLE_FIRST_ACTIVATE] = isActivate
        }
    }

    suspend fun setStabilizationCompleted(isCompleted: Boolean) {
        dataStore.edit { prefs ->
            prefs[STABILIZATION_COMPLETED] = isCompleted
        }
    }
}
