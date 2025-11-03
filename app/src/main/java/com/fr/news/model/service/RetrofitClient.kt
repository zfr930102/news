package com.fr.news.model.service

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.fr.news.constant.BaseUrl
import com.fr.news.manager.appContext
import com.fr.news.model.service.api_interface.BaiduTieBaApiService
import com.fr.news.model.service.api_interface.CLSApiService
import com.fr.news.model.service.api_interface.DouYinApiService
import com.fr.news.model.service.api_interface.NewsNowApiService
import com.fr.news.model.service.api_interface.ToutiaoApiService
import com.fr.news.model.service.api_interface.WallStreetCNApiService
import com.fr.news.model.service.api_interface.WeiboApiService
import com.fr.news.model.service.api_interface.XueQiuApiService
import com.fr.news.model.service.api_interface.XueQiuCookieApiService
import com.fr.news.model.service.api_interface.ZhiHuApiService
import com.fr.news.utils.BASE_TAG
import com.fr.news.utils.GzipDecompressionInterceptor
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.GzipSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

const val CACHE_SIZE = 10 * 1024 * 1024 // 10MB
const val CONNECT_TIMEOUT = 15L // 15秒
const val READ_TIMEOUT = 30L // 30秒
const val WRITE_TIMEOUT = 30L // 30秒
const val TAG = BASE_TAG + "RetrofitClient"

class RetrofitClient {
    var okHttpClient = createOkHttpClient()
    var newsNowOkHttpClient = createNewsNowOkHttpClient()

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

    fun createNewsNowOkHttpClient(): OkHttpClient {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "news_now_okhttp_cache")
        val cache = Cache(cacheDir, CACHE_SIZE.toLong())
        return OkHttpClient.Builder()
            .cache(cache) // 添加缓存
            .addNetworkInterceptor(createCacheInterceptor()) // 网络层缓存拦截器
            .addInterceptor(createLoggingInterceptor()) // 添加日志拦截器
            .addInterceptor(createHeadersInterceptor())
            .addInterceptor(createOfflineCacheInterceptor()) // 离线缓存拦截器
            .addInterceptor(errorHandleInterceptor())
            .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // 检查并处理 GZIP 压缩
            val contentEncoding = response.header("Content-Encoding")
            if (contentEncoding != null && contentEncoding.contains("gzip", true)) {
                // OkHttp 通常会自动处理 GZIP，但如果出现问题可以手动处理
                val source = response.body?.source()
                if (source != null) {
                    try {
                        val gzipSource = GzipSource(source)
                        val buffer = Buffer()
                        buffer.writeAll(gzipSource)
                        gzipSource.close()

                        val contentType = response.body?.contentType()
                        val responseBody = okhttp3.ResponseBody.create(contentType, buffer.readByteString())
                        Log.d(TAG, "createNewsNowOkHttpClient: responseBody = ${responseBody.contentLength()}")
                        return@Interceptor response.newBuilder()
                            .body(responseBody)
                            .header("Content-Encoding", "") // 移除编码头避免重复处理
                            .build()
                    } catch (e: Exception) {
                        // 如果手动处理失败，返回原始响应
                        return@Interceptor response
                    }
                }
            }
            response
        })
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // 自动重连
            .build()
     }
    private val USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36"

    private fun createHeadersInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            val newRequest = originalRequest.newBuilder().apply {
                // 添加浏览器标识头
                header("User-Agent", USER_AGENT)
                header("Accept-Language", "zh-CN")
                header("Accept-Encoding", "gzip")

            }.build()

            chain.proceed(newRequest)
        }
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
                    Log.e(TAG, "HTTP错误: ${response.code}")
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

    val clsApiService: CLSApiService =
        Retrofit.Builder().baseUrl(BaseUrl.CLS_BASE_URL).client(okHttpClient).addConverterFactory(
            GsonConverterFactory.create()
        ).build().create(CLSApiService::class.java)

    val gson = GsonBuilder().setLenient().create()
    val newsNowApiService: NewsNowApiService = Retrofit.Builder()
        .baseUrl(BaseUrl.NEWS_NOW_BASE_URL)
        .client(newsNowOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(NewsNowApiService::class.java)
}

class RequestCanceledException(message: String) : IOException(message)
class NetworkException(message: String) : IOException(message)
