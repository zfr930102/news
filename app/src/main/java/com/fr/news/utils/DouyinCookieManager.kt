package com.fr.news.utils

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import com.fr.news.constant.BaseUrl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DouyinCookieManager(context: Context) {

    private var acNonceState = MutableStateFlow(false)
    private var acSignatureState = MutableStateFlow(false)
    private var sessionidState = MutableStateFlow(false)
    private var ttwidState = MutableStateFlow(false)
    val cookieState: StateFlow<Boolean> =
        combine(acNonceState, acSignatureState, ttwidState) { acNode, acSignature, ttwid ->
            acNode || acSignature || ttwid
        }.stateIn(
            CoroutineScope(Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5000),
            false
        )

    //获取sp
    private val sp = context.getSharedPreferences("douyin_cookie", Context.MODE_PRIVATE)


    fun getCookie(name: String): String? {
        Log.d(TAG, "getCookie: name = $name")
        return sp.getString(name, null)
    }

    fun hasAllRequiredCookies(): Boolean {
        return getCookie("__ac_nonce") != null &&
                getCookie("__ac_signature") != null &&
                getCookie("ttwid") != null
        //&&
        //                getCookie("sessionid") != null
    }

    fun saveCookie(name: String, value: String) {
        Log.d(TAG, "saveCookie: name = $name, value = $value")
        when (name) {
            "__ac_nonce" -> {
                acNonceState.value = true
            }
            "__ac_signature" -> {
                acSignatureState.value = true
            }
            "sessionid" -> {
                sessionidState.value = true
            }
            "ttwid" -> {
                ttwidState.value = true
            }
        }
        sp.edit { putString(name, value) }
    }

    fun clearCookie() {
        sp.edit { clear() }
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    fun setCookiesInWebView() {
        Log.d(TAG, "setCookiesInWebView: start")
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        getCookie("__ac_nonce")?.let {
            Log.d(TAG, "setCookiesInWebView: __ac_nonce = $it")
            cookieManager.setCookie(BaseUrl.DOU_YIN_BASE_URL, "__ac_nonce=$it")
        }
        getCookie("__ac_signature")?.let {
            Log.d(TAG, "setCookiesInWebView:  __ac_signature = $it")
            cookieManager.setCookie(BaseUrl.DOU_YIN_BASE_URL, "__ac_signature=$it")
        }
        getCookie("ttwid")?.let {
            Log.d(TAG, "setCookiesInWebView: ttwid = $it")
            cookieManager.setCookie(BaseUrl.DOU_YIN_BASE_URL, "ttwid=$it")
        }
        getCookie("sessionid")?.let {
            Log.d(TAG, "setCookiesInWebView: sessionid = $it")
            cookieManager.setCookie(BaseUrl.DOU_YIN_BASE_URL, "sessionid=$it")
        }
        cookieManager.flush()
    }

    fun getCookiesFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "getCookiesFromServer: getCookiesFromServer")
            fetchDouyinCookies()
        }
    }

    fun fetchDouyinCookies() {
        val client = OkHttpClient.Builder()
            .followRedirects(false) // 禁止重定向以获取初始Cookie
            .cookieJar(object : CookieJar {
                private val cookieStore = mutableMapOf<String, List<Cookie>>()

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                    Log.d(TAG, "saveFromResponse: cookies $cookies")
                    // 提取所需的Cookie
                    cookies.forEach { cookie ->
                        when (cookie.name) {
                            "__ac_nonce" -> {
                                saveCookie("__ac_nonce", cookie.value)
                            }

                            "__ac_signature" -> {
                                saveCookie("__ac_signature", cookie.value)
                            }

                            "sessionid" -> {
                                saveCookie("sessionid", cookie.value)
                            }
                        }
                        setCookiesInWebView()
                    }
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: emptyList()
                }
            })
            .build()

        val request = Request.Builder()
            .url("https://www.douyin.com")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36"
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "获取Cookie失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "onResponse: response = $response")
                if (!response.isSuccessful) {
                    Log.w(TAG, "请求失败: ${response.code}")
                    return
                }

                // 检查是否获取到所需Cookie
                val cookies = response.headers("Set-Cookie")
                cookies.forEach { cookieHeader ->
                    parseCookieHeader(cookieHeader)
                }

                // 如果缺少某些Cookie，可能需要进一步处理
                if (!hasRequiredCookies()) {
                    fetchTTWIDCookie()
                } else {
                    setCookiesInWebView()
                }
            }


        })
    }

    private fun parseCookieHeader(cookieHeader: String) {
        val cookieParts = cookieHeader.split(";")
        cookieParts.forEach { part ->
            val keyValue = part.trim().split("=", limit = 2)
            if (keyValue.size == 2) {
                val key = keyValue[0]
                val value = keyValue[1]

                when (key) {
                    "__ac_nonce",
                    "__ac_signature",
                    "sessionid" -> {
                        saveCookie(key, value)
                    }
                }
            }
        }
    }

    private fun fetchTTWIDCookie() {
        val client = OkHttpClient()

        // 这个URL是获取ttwid的特定端点
        val request = Request.Builder()
            .url("https://www.douyin.com/ttwid")
            .post(FormBody.Builder().build()) // 空POST请求
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36"
            )
            .header("Accept", "application/json, text/plain, */*")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "获取ttwid失败", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "onResponse: response = $response")
                if (!response.isSuccessful) {
                    Log.w(TAG, "ttwid请求失败: ${response.code}")
                    return
                }

                // 从响应头中获取Set-Cookie
                val cookies = response.headers("Set-Cookie")
                cookies.forEach { cookieHeader ->
                    if (cookieHeader.contains("ttwid")) {
                        val pattern = "ttwid=([^;]+)".toRegex()
                        val matchResult = pattern.find(cookieHeader)
                        matchResult?.groups?.get(1)?.value?.let { ttwidValue ->
                            saveCookie("ttwid", ttwidValue)
                            setCookiesInWebView()
                        }
                    }
                }
            }
        })
    }

    private fun hasRequiredCookies(): Boolean {
        val ttwid = getCookie("ttwid")
        return (ttwid != null)
    }

    companion object {
        private const val TAG = BASE_TAG + "DouyinCookieManager"

        @Volatile
        private var instance: DouyinCookieManager? = null
        fun getInstance(context: Context): DouyinCookieManager =
            instance ?: synchronized(this) {
                instance ?: DouyinCookieManager(context).also { instance = it }
            }
    }

}