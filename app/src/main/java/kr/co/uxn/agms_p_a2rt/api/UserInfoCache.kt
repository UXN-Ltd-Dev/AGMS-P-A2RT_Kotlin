package kr.co.uxn.agms_p_a2rt.api

import kotlinx.coroutines.flow.first
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.token.CachedUserInfo
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager

object UserInfoCache {
    suspend fun getUserInfo(): CachedUserInfo? {
        val userId = DataStoreManager.getUserId().first() ?: return null
        DataStoreManager.getCachedUserInfo(userId).first()?.let { return it }

        val response = tokenRetrofit.getUser(userId)
        val userData = response.body()
        if (!response.isSuccessful || userData?.isSuccess != true) return null

        return CachedUserInfo(
            userId = userId,
            email = userData.email,
            name = userData.name,
            sex = userData.sex,
            age = userData.age,
            height = userData.height,
            weight = userData.weight,
            diabetesType = userData.diabetesType,
            targetGlucoseMin = userData.targetGlucoseMin,
            targetGlucoseMax = userData.targetGlucoseMax
        ).also { DataStoreManager.saveCachedUserInfo(it) }
    }
}
