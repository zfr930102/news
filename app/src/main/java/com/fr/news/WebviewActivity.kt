package com.fr.news

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr.news.constant.NewsType
import com.fr.news.manager.appContext
import com.fr.news.ui.theme.NewsTheme
import com.fr.news.utils.BASE_TAG
import com.fr.news.utils.DouyinCookieManager

var maxRedirectionCount = 1
var redirectionCount = 0
const val WEB_TAG = BASE_TAG +"WebviewActivity"
class WebviewActivity:ComponentActivity() {

    val douyinCookieManager = DouyinCookieManager.getInstance(appContext)

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var webUrl = intent.getStringExtra("url") ?: "https://www.baidu.com"
        val newsType = intent.getStringExtra(/* name = */ "type")?:""
        if (NewsType.valueOf(newsType)== NewsType.DOUYIN){
            webUrl = "https://www.douyin.com/hot"
            //处理抖音链接,需要获取相关的Cookie.
            if (douyinCookieManager.hasAllRequiredCookies()) {
                //直接设置 Cookie
                douyinCookieManager.setCookiesInWebView()
            } else {
                //获取新的Cookie并设置到CookieManager
                douyinCookieManager.getCookiesFromServer()
            }
        }
        setContent {
            NewsTheme {
                WebViewScreen(url = webUrl,newsType)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        redirectionCount = 0
        douyinCookieManager.clearCookie()
    }
}

@Composable
fun WebViewScreen(url: String, newsType:String) {
    // 创建 WebView
    val webView = rememberWebView(url,newsType)
    val context = LocalContext.current
    //获取WindowInsets
    val windowInsets = ViewCompat.getRootWindowInsets(webView.rootView)
    val top = windowInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    val density = LocalDensity.current
    val statusBarTopDp = with(density){
        top.toDp()
    }
    Log.d(TAG, "WebViewScreen: top = $top statusBarTopDp = $statusBarTopDp")
    // 处理返回键
    BackHandler(enabled = true) {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            (context as Activity).finish()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 加载进度指示器
        val loadingProgress = remember { mutableIntStateOf(0) }
        if (loadingProgress.value < 100) {
            LinearProgressIndicator(
                progress = loadingProgress.value / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // WebView 容器
        AndroidView(
            factory = { context ->
                webView.apply {
                    // 配置 WebView
                    val webSettings = webView.settings

                    // 基本设置
                    webSettings.javaScriptEnabled = true
                    webSettings.domStorageEnabled = true
                    webSettings.databaseEnabled = true

                    // 设置移动端User-Agent
                    webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36"

                    // 优化设置
                    webSettings.loadWithOverviewMode = true
                    webSettings.useWideViewPort = true
                    webSettings.setSupportZoom(false)
                    webSettings.builtInZoomControls = false
                    webSettings.displayZoomControls = false
                    webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

                    // 混合内容处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }

                    // Cookie设置
                    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().flush()

                    // 设置 WebView 客户端
                    webViewClient = rememberWebViewClient { it ->
                        loadingProgress.intValue = it
                    }

                    // 设置进度监听
                    webChromeClient = rememberWebChromeClient {
                        loadingProgress.intValue = it
                    }
                }
            },
            modifier = Modifier
                .weight(1f).padding(top =statusBarTopDp)
        )
    }
}
@Composable
fun rememberWebView(url: String, newsType: String): WebView {
    val context = LocalContext.current
    val cookieState by DouyinCookieManager.getInstance(appContext).cookieState.collectAsStateWithLifecycle()
    val webView = remember {
        WebView(context).apply {
            if (newsType == NewsType.DOUYIN.name) {
                Log.d(WEB_TAG, "rememberWebView: load douyin url")
                if (cookieState) {
                    loadUrl(url)
                }
            } else {
                Log.d(WEB_TAG, "rememberWebView: load url")
                loadUrl( url)
            }
        }
    }

    return webView
}

fun rememberWebViewClient(onProgressChanged: (Int) -> Unit): WebViewClient{
    return object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Log.d(WEB_TAG, "shouldOverrideUrlLoading: request = $request")
            request?.url.toString().let {
                return handleUrl(view, it)
            }
            return false
        }

        //兼容旧版本
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Log.d(WEB_TAG, "shouldOverrideUrlLoading: url = $url")
            if (url == null) {
                return false
            }
            return handleUrl(view, url)
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d(WEB_TAG, "onPageFinished: url = $url")
            view?.progress.let {
                if (it == 100) {
                    onProgressChanged(100)
                    Log.d(TAG, "onPageFinished: over")
                }
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(WEB_TAG, "onPageStarted: url = $url")
            view?.progress?.let {
                if (it > 0) {
                    Log.d(TAG, "onPageStarted: web already loading so return started method")
                    return
                }
            }
            onProgressChanged(0)
            injectAntiDetectionScript(view)
        }


        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            Log.e(WEB_TAG, "onReceivedHttpError: errorResponse = ${errorResponse?.statusCode}", )
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            Log.e(WEB_TAG, "onReceivedError: errorCode = $errorCode, description = $description, failingUrl = $failingUrl")
            super.onReceivedError(view, errorCode, description, failingUrl)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.e(WEB_TAG, "onReceivedError: error = $error code = ${error?.errorCode}")
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            Log.e(WEB_TAG, "onReceivedSslError: error = $error primaryError = " +
                    " ${error?.primaryError}")
            super.onReceivedSslError(view, handler, error)
        }


    }
}

private fun injectAntiDetectionScript(view: WebView?) {
    val jsCode = """
            // 删除WebView环境检测
            delete window.navigator.__proto__.webdriver;
            
            // 修改属性以模拟真实浏览器
            Object.defineProperty(navigator, 'webdriver', { get: () => false });
            Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3] });
            Object.defineProperty(navigator, 'languages', { get: () => ['zh-CN', 'zh'] });
            
            // 覆盖console.log方法
            const originalLog = console.log;
            console.log = function() {
                // 过滤掉包含"webdriver"的日志
                if (arguments.length > 0 && typeof arguments[0] === 'string' && arguments[0].includes('webdriver')) {
                    return;
                }
                originalLog.apply(console, arguments);
            };
        """.trimIndent()

    view?.evaluateJavascript(jsCode, null)
}

private fun handleUrl(view: WebView?, url: String): Boolean {
    Log.d(WEB_TAG, "handleUrl: url = $url maxRedirectionCount = $maxRedirectionCount")

    // 1. 处理自定义协议（如 zhihu://）
    if (url.startsWith("zhihu://") && redirectionCount < maxRedirectionCount) {
        Log.d(WEB_TAG, "handleUrl: zhihu")
        maxRedirectionCount = 1
        // 尝试转换为标准的知乎网页 URL
        val standardUrl = convertZhihuSchemeToStandardUrl(url)
        view?.loadUrl(standardUrl)
        redirectionCount++
        return true // 表示已处理该 URL
    }

    if (url.startsWith("bytedance://") && redirectionCount < maxRedirectionCount){
        maxRedirectionCount = 1
        handleByteDanceUrl(view, url)
        redirectionCount++
        return true
    }

    // 2. 处理其他自定义协议（如 intent://, weixin:// 等），可选
    if (url.startsWith("intent://") || url.startsWith("weixin://")) {
        // 尝试用其他应用处理
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            view?.context?.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            // 没有应用能处理此链接，可以提示用户
            Log.e("WebView", "无法处理链接: $url")
        }
        return true
    }
    // 3. 对于 http/https 协议，检查重定向次数
    if (url.startsWith("http://") || url.startsWith("https://")) {
        if (maxRedirectionCount == 1) {
            maxRedirectionCount = 5
        }
        redirectionCount++
        if (redirectionCount > maxRedirectionCount) { // 设定一个阈值，例如5次
            // 可能是重定向循环，停止加载并提示
            Log.w(WEB_TAG, "重定向次数过多，可能陷入循环。")
            // 可以在这里加载一个错误页面或提示用户
            return true
        }
        // 允许 WebView 继续加载
        return false
    }
    // 4. 其他未知协议
    return true
}

private fun handleByteDanceUrl(view: WebView?, url: String) {
    Log.d(WEB_TAG, "handleByteDanceUrl: url = $url")
    try {
        val uri = Uri.parse(url)
        val path = uri.path ?: ""
        val queryParams = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) }
        Log.d(WEB_TAG, "handleByteDanceUrl: queryParams = $queryParams path = $path")
    } catch (e: Exception) {
        Log.e(WEB_TAG, "Error handling bytedance link", e)
        Toast.makeText(appContext, "无法处理此链接", Toast.LENGTH_SHORT).show()
    }
}

private fun convertZhihuSchemeToStandardUrl(zhihuUrl: String): String {
    return try {
        // 尝试从 zhihu://questions/1946649238894248296 转换为 https://www.zhihu.com/question/1946649238894248296
        val pattern = "zhihu://questions/(\\d+)".toRegex()
        val matchResult = pattern.find(zhihuUrl)
        if (matchResult != null) {
            val questionId = matchResult.groupValues[1]
            "https://www.zhihu.com/question/$questionId"
        } else {
            // 如果无法提取问题ID，退回知乎首页
            "https://www.zhihu.com"
        }
    } catch (e: Exception) {
        // 转换失败，退回知乎首页
        "https://www.zhihu.com"
    }
}

fun rememberWebChromeClient(onProgressChanged: (Int) -> Unit): WebChromeClient{
    return object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            onProgressChanged(newProgress)
        }
    }
}