package kr.co.uxn.agms_p.api

import kr.co.uxn.agms_p.BuildConfig
import kr.co.uxn.agms_p.api.token.TokenAuthenticator
import kr.co.uxn.agms_p.api.token.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val BASE_URL_SERVER = BuildConfig.base_url

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply { level = HttpLoggingInterceptor.Level.BODY })
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
//            .addInterceptor(HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply { level = HttpLoggingInterceptor.Level.BODY })
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
//            .addInterceptor(HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply { level = HttpLoggingInterceptor.Level.BODY })
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

    val emptyRetrofit: RemoteDataSource by lazy {
        retrofit.create(RemoteDataSource::class.java)
    }

    val tokenRetrofit: RemoteDataSource by lazy {
        retrofit2.create(RemoteDataSource::class.java)
    }

    val refreshRetrofit: RemoteDataSource by lazy {
        retrofit3.create(RemoteDataSource::class.java)
    }
}