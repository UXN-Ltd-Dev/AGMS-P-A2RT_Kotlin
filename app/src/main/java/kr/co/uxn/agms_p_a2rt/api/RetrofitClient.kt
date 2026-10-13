package kr.co.uxn.agms_p_a2rt.api

import kr.co.uxn.agms_p_a2rt.BuildConfig
import kr.co.uxn.agms_p_a2rt.api.token.TokenAuthenticator
import kr.co.uxn.agms_p_a2rt.api.token.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    /**
     * HTTP 로그는 **디버그 빌드에서만** 본문까지 남긴다.
     *
     * 배포 빌드에서 Level.BODY 로 두면 요청·응답 본문이 그대로 logcat 에 찍힌다.
     * 토큰·이메일·시리얼·MAC·혈당·임피던스가 모두 본문에 들어 있어, 루팅하지
     * 않은 기기에서도 수집된다.
     *
     * Level.BASIC 도 쓰지 않는다. 요청 줄에 URL 이 들어가고, 시리얼을 query 로
     * 붙여 보내는 경로가 있어 URL 만으로도 식별정보가 남는다. 그래서 배포
     * 빌드는 NONE 이다.
     */
    private fun loggingInterceptor() =
        HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    private const val BASE_URL_SERVER = "https://agmscare.kr:8080"
    private const val BASE_URL_SERVER2 = BuildConfig.base_url

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SERVER)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    private val okHttpClient2 by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .addInterceptor(TokenInterceptor())
            .authenticator(TokenAuthenticator())
            .build()
    }

    private val retrofit2 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SERVER)
            .client(okHttpClient2)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient3 by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()
    }

    private val retrofit3 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SERVER)
            .client(okHttpClient3)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient4 by lazy {
        OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    private val retrofit4 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SERVER)
            .client(okHttpClient4)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val emptyRetrofit: RemoteDataSource by lazy {
        retrofit.create(RemoteDataSource::class.java)
    }

    val tokenRetrofit: RemoteDataSource by lazy {
        retrofit2.create(RemoteDataSource::class.java)
    }

    val refreshRetrofit: RemoteDataSource by lazy {
        retrofit3.create(RemoteDataSource::class.java)
    }

    val preRetrofit: RemoteDataSource by lazy {
        retrofit4.create(RemoteDataSource::class.java)
    }
}