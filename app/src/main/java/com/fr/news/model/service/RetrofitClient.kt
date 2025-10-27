package com.fr.news.model.service

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.fr.news.constant.BaseUrl
import com.fr.news.manager.appContext
import com.fr.news.model.service.api_interface.BaiduTieBaApiService
import com.fr.news.model.service.api_interface.DouYinApiService
import com.fr.news.model.service.api_interface.ToutiaoApiService
import com.fr.news.model.service.api_interface.WallStreetCNApiService
import com.fr.news.model.service.api_interface.WeiboApiService
import com.fr.news.model.service.api_interface.XueQiuApiService
import com.fr.news.model.service.api_interface.XueQiuCookieApiService
import com.fr.news.model.service.api_interface.ZhiHuApiService
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

const val CACHE_SIZE = 10 * 1024 * 1024 // 10MB
const val CONNECT_TIMEOUT = 15L // 15秒
const val READ_TIMEOUT = 30L // 30秒
const val WRITE_TIMEOUT = 30L // 30秒
const val TAG = "RetrofitClient"

class RetrofitClient {
    var okHttpClient = createOkHttpClient()

    fun createOkHttpClient(): OkHttpClient {
        // 创建缓存目录
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "okhttp_cache")
        val cache = Cache(cacheDir, CACHE_SIZE.toLong())

        return OkHttpClient.Builder()
            .cache(cache) // 添加缓存
            .addInterceptor(createLoggingInterceptor()) // 添加日志拦截器
            .addNetworkInterceptor(createCacheInterceptor()) // 网络层缓存拦截器
            .addInterceptor(createOfflineCacheInterceptor()) // 离线缓存拦截器
            .addInterceptor(errorHandleInterceptor())
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // 自动重连
            .build()
    }

    // 创建日志拦截器
    fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 调试模式下打印完整日志
        }
    }

    // 创建网络缓存拦截器
    fun createCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(2, TimeUnit.MINUTES) // 2分钟缓存
                .build()
            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }

    // 创建离线缓存拦截器
    fun createOfflineCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable()) {
                // 无网络时使用缓存
                Log.d(TAG, "createOfflineCacheInterceptor: no have internet")
                val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS) // 7天离线缓存
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }
            chain.proceed(request)
        }
    }

    // 检查网络状态
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun errorHandleInterceptor(): Interceptor {
        return Interceptor { chain ->
            try {
                Log.d(TAG, "errorHandleInterceptor: run")
                val request = chain.request()
                val response = chain.proceed(request)

                if (!response.isSuccessful) {
                    Log.e("OkHttp", "HTTP错误: ${response.code}")
                }

                return@Interceptor response
            } catch (e: Exception) {
                if (e.message == "Canceled") {
                    Log.d(TAG, "请求被取消: ${e.message}")
                    throw RequestCanceledException("请求被取消")
                } else {
                    Log.e(TAG, "网络错误: ${e.message}")
                    throw NetworkException("网络连接失败: ${e.message}")
                }
            }

        }
    }

    val touTiaoApiService: ToutiaoApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.TOU_TIAO_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ToutiaoApiService::class.java)

    val weiboApiService: WeiboApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.WEI_BO_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeiboApiService::class.java)

    val zhiHuApiService: ZhiHuApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.ZHI_HU_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ZhiHuApiService::class.java)

    val baiduTieBaApiService: BaiduTieBaApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.BAIDU_TIE_BA_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BaiduTieBaApiService::class.java)

    val douYinApiService: DouYinApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.DOU_YIN_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DouYinApiService::class.java)

    val wallStreetApiService: WallStreetCNApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.WALL_STREET_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WallStreetCNApiService::class.java)

    val xueQiuCookieApiService: XueQiuCookieApiService =
        Retrofit.Builder().baseUrl(BaseUrl.XUE_QIU_COOKIE_BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build()
            .create(XueQiuCookieApiService::class.java)

    val xueQiuApiService: XueQiuApiService =
        Retrofit.Builder().baseUrl(BaseUrl.XUE_QIU_BASE_URL)
            .client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build()
            .create(XueQiuApiService::class.java)
}

class RequestCanceledException(message: String) : IOException(message)
class NetworkException(message: String) : IOException(message)
