package com.fr.news.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr.news.constant.CLS_DEPTH_CHANNEL_ID
import com.fr.news.constant.CLS_HOT_CHANNEL_ID
import com.fr.news.constant.CLS_TELEGRAPH_CHANNEL_ID
import com.fr.news.constant.NewsType
import com.fr.news.model.data.CLSDepthResponse
import com.fr.news.model.data.CLSHotResponse
import com.fr.news.model.data.CLSTelegraphResponse
import com.fr.news.model.data.NewsData
import com.fr.news.model.data.NewsNowResponse
import com.fr.news.model.service.RetrofitClient
import com.fr.news.state.MultiState
import com.fr.news.state.RequestState
import com.fr.news.state.ResponseState
import com.fr.news.utils.BASE_TAG
import com.fr.news.utils.SearchParamsBuilder
import com.fr.news.utils.SearchParamsChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Buffer
import okio.GzipSource
import retrofit2.Response
import kotlin.apply
import kotlin.collections.toMutableMap
import kotlin.coroutines.cancellation.CancellationException

class NewsViewModel : ViewModel() {
    //利用状态管理，管理当前整合头条新闻的数据。
    private val _newsData = MutableStateFlow(MultiState())
    val newsData: StateFlow<MultiState> = _newsData
    val retrofitClient = RetrofitClient()
    val TAG = BASE_TAG + "NewsViewModel"

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    private lateinit var newsType: NewsType
    fun getData(type: NewsType) {
        newsType = type
        _isRefreshing.value = true
        Log.d(TAG, "getData: start isResfreshing = ${isRefreshing.value}")
        when (type) {
            NewsType.TOUTIAO -> {
                Log.d(TAG, "getData: TOUTIAO")
                getToutiaoData()
            }

            NewsType.ZHIHU -> {
                Log.d(TAG, "getData: ZHIHU")
                getZhiHuData()
            }

            NewsType.WEIBO -> {
                Log.d(TAG, "getData: WEIBO")
                getWeiboData()
            }

            NewsType.WALLSTREET -> {
                Log.d(TAG, "getData: WALLSTREET")
                getWallStreetData()
            }

            NewsType.DOUYIN -> {
                Log.d(TAG, "getData: DOUYIN")
                getDouYinData()
            }

            NewsType.TIEBA -> {
                Log.d(TAG, "getData: TIEBA")
                getTieBaData()
            }

            NewsType.XUEQIU -> {
                Log.d(TAG, "getData: XUEQIU")
                getXueQiuData()
            }

            NewsType.CLS_TELEGRAPH -> {
                Log.d(TAG, "getData: CLS_TELEGRAPH")
                getCLSData(NewsType.CLS_TELEGRAPH)
            }
            NewsType.CLS_DEPTH -> {
                Log.d(TAG, "getData: CLS_DEPTH")
                getCLSData(NewsType.CLS_DEPTH)
            }
            NewsType.CLS_HOT -> {
                Log.d(TAG, "getData: CLS_HOT")
                getCLSData(NewsType.CLS_HOT)
            }
        }
        Log.d(TAG, "getData: end")
    }

    private fun getCLSData(type: NewsType) {
        updatePageState(pageType = type){
            Log.d(TAG, "getCLSData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val query = SearchParamsBuilder.instance.getSearchParamsSync(
                    emptyMap(),
                    SearchParamsChannel.CLS_SEARCH_PARAMS_CHANNEL
                )
                var response: Response<*> = Response.error<Any>(600,okhttp3.ResponseBody.create(null, "Unsupported type"))
                when(type){
                    NewsType.CLS_TELEGRAPH -> {
                        response = retrofitClient.newsNowApiService.getNewsNowData(CLS_TELEGRAPH_CHANNEL_ID)
                    }
                    NewsType.CLS_DEPTH -> {
                        response = retrofitClient.newsNowApiService.getNewsNowData(CLS_DEPTH_CHANNEL_ID)
                    }
                    NewsType.CLS_HOT -> {
                        response = retrofitClient.newsNowApiService.getNewsNowData(CLS_HOT_CHANNEL_ID)
                    }
                    else -> {
                        Response.error<Any>(600,okhttp3.ResponseBody.create(null, "Unsupported type"))
                    }
                }

                if (response.isSuccessful) {
//                    val clsNewsData: List<NewsData>? = response.body()?.let {
//                        parseClsData(it, type)
//                    }
                    val newsNowResponse = response.body() as NewsNowResponse
                    val clsNewsData: List<NewsData>? = newsNowResponse.items.map {
                        NewsData(it.title, it.mobileUrl)
                    }
                    Log.d(TAG, "getCLSData: data size = ${clsNewsData?.size} type = ${type.name}")
                    updatePageState(type) {
                        it.copy(isLoading = false, data = clsNewsData)
                    }
                } else {
                    updatePageState(type){
                        Log.d(TAG, "getCLSData: failed code ${response.code()} type = ${type.name}")
                        it.copy(isLoading = false, error = "获取财联社电报数据失败,失败信息为：${response.code()}")
                    }
                }
            } catch (e: Exception) {
                updatePageState(type){
                    Log.d(TAG, "getCLSData: 获取财联社电报数据失败，失败信息是：${e.message} type = ${type.name}")
                    it.copy(isLoading = false, error = "获取财联社电报数据失败，失败信息是：${e.message}")
                }

            }
        }
    }

    private fun parseClsData(data: Any, type: NewsType): List<NewsData>? {
        return when(type){
            NewsType.CLS_TELEGRAPH -> {
                val clsTelegraphData = data as CLSTelegraphResponse
                clsTelegraphData.data.roll_data.filter { it.is_ad != 1 }.map {
                    NewsData(
                        title = it.title?.takeIf { it.isNotEmpty() } ?: it.brief,
                        url = "https://www.cls.cn/detail/${it.id}"
                    )
                }
            }
            NewsType.CLS_DEPTH -> {
                val clsDepthData = data as CLSDepthResponse
                clsDepthData.data.depth_list.sortedByDescending { it.ctime}.map {
                    NewsData(
                        title = it.title ?:it.brief,
                        url = "https://www.cls.cn/detail/${it.id}"
                    )
                }
            }

            NewsType.CLS_HOT -> {
                val clsHotData = data as CLSHotResponse
                clsHotData.data.map {
                    NewsData(
                        title = it.title ?:it.brief,
                        url = "https://www.cls.cn/detail/${it.id}"
                    )
                }
            }

            else -> {
                null
            }
        }
    }

    private fun getXueQiuData() {
        updatePageState(NewsType.XUEQIU) {
            Log.d(TAG, "getXueQiuData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val xueQiuCookieData = retrofitClient.xueQiuCookieApiService.getXueQiuCookie()
                val xueQiuCookie: String = getCookiesForResponse(xueQiuCookieData)
                if (xueQiuCookie.isEmpty()) {
                    updatePageState(NewsType.XUEQIU){
                        Log.d(TAG, "getXueQiuData: cookie is null")
                        it.copy(isLoading = false, error = "获取雪球数据失败,失败信息为：cookie是空")
                    }
                    return@launch
                }
                val xueQiuData = retrofitClient.xueQiuApiService.getXueQiuData(cookie = xueQiuCookie)
                if (xueQiuData.isSuccessful) {
                    val xueQiuNewsData = xueQiuData.body()?.data?.items?.filter {
                        it.ad == 0
                    }?.map {
                        NewsData(it.name,"https://xueqiu.com/s/${it.code}")
                    }
                    Log.d(TAG, "getXueQiuData: data size = ${xueQiuNewsData?.size}")
                    updatePageState(NewsType.XUEQIU) {
                        it.copy(isLoading = false, data = xueQiuNewsData)
                    }
                } else {
                    updatePageState(NewsType.XUEQIU){
                        Log.d(TAG, "getXueQiuData: failed code ${xueQiuData.code()}")
                        it.copy(isLoading = false, error = "获取雪球数据失败,失败信息为：${xueQiuData.code()}")
                    }
                }
            } catch (e: Exception) {
                updatePageState(NewsType.XUEQIU){
                    Log.d(TAG, "getXueQiuData: 获取雪球数据失败,失败信息为：${e.message}")
                    it.copy(isLoading = false, error = "获取雪球数据失败,失败信息为：${e.message}")
                }
            }
        }
    }

    private fun getTieBaData() {
        updatePageState(NewsType.TIEBA) {
            Log.d(TAG, "getTieBaData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val tieBaData = retrofitClient.baiduTieBaApiService.getTieBaData()
                if (tieBaData.isSuccessful) {
                    val newsData = tieBaData.body()?.data?.bang_topic?.topic_list?.map {
                        NewsData(it.topic_name, it.topic_url)
                    }
                    Log.d(TAG, "getTieBaData: success newsData.size = ${newsData?.size}")
                    updatePageState(NewsType.TIEBA) {
                        it.copy(isLoading = false, data = newsData)
                    }
                } else {
                    updatePageState(NewsType.TIEBA) {
                        Log.d(TAG, "getTieBaData: 获取贴吧数据失败.失败code为${tieBaData.code()}")
                        it.copy(
                            isLoading = false,
                            error = "获取贴吧数据失败.失败code为${tieBaData.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "getTieBaData: 获取贴吧数据失败,失败信息为：${e.message}")
                updatePageState(NewsType.TIEBA) {
                    it.copy(isLoading = false, error = "获取贴吧数据失败,失败信息为：${e.message}")
                }
            }

        }
    }

    private fun getDouYinData() {
        updatePageState(NewsType.DOUYIN) {
            Log.d(TAG, "getDouYinData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val cookieResponse = retrofitClient.douYinApiService.getDouYinLoginCookie()
                val cookie = cookieResponse.headers().values("Set-Cookie")
                val cookieString = cookie.joinToString("; ") { it ->
                    it.substringBefore(";")
                }
                Log.d(TAG, "getDouYinData: cookieString = $cookieString")
                val douYinData =
                    retrofitClient.douYinApiService.getDouYinData(cookie = cookieString)
                if (douYinData.isSuccessful) {
                    val newsData = douYinData.body()?.data?.word_list?.map { k ->
                        NewsData(k.word, "https://www.douyin.com/hot/${k.sentence_id}")
                    }
                    Log.d(TAG, "getDouYinData: success newsData.size = ${newsData?.size}")
                    updatePageState(NewsType.DOUYIN) {
                        it.copy(isLoading = false, data = newsData)
                    }
                } else {
                    updatePageState(NewsType.DOUYIN) {
                        Log.d(TAG, "getDouYinData: 获取抖音数据失败.失败code为${douYinData.code()}")
                        it.copy(
                            isLoading = false,
                            error = "获取抖音数据失败.失败code为${douYinData.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                updatePageState(NewsType.DOUYIN) {
                    Log.d(TAG, "getDouYinData: 获取抖音数据失败,失败信息为：${e.message}")
                    it.copy(isLoading = false, error = "获取抖音数据失败,失败信息为：${e.message}")
                }
            }
        }
    }

    private fun getWallStreetData() {
        updatePageState(NewsType.WALLSTREET) {
            Log.d(TAG, "getWallStreetData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val wallStreetData = retrofitClient.wallStreetApiService.getWallStreetData()
                if (wallStreetData.isSuccessful) {
                    wallStreetData.body().let {
                        val newsData = it?.data?.items?.filter { k ->
                            (k.resource_type != "theme" && k.resource_type != "ad"
                                    && k.resource.type != "live")
                        }?.map { k ->
                            NewsData(
                                k.resource.title.ifEmpty { k.resource.content_short },
                                k.resource.uri
                            )
                        }
                        Log.d(TAG, "getWallStreetData: success newsData.size = ${newsData?.size}")
                        updatePageState(NewsType.WALLSTREET) { pageState ->
                            pageState.copy(isLoading = false, data = newsData)
                        }
                    }
                } else {
                    updatePageState(NewsType.WALLSTREET) {
                        Log.d(
                            TAG,
                            "getWallStreetData: 获取华尔街见闻数据失败.失败code为${wallStreetData.code()}"
                        )
                        it.copy(
                            isLoading = false,
                            error = "获取华尔街见闻数据失败.失败code为${wallStreetData.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                updatePageState(NewsType.WALLSTREET) {
                    Log.d(TAG, "getWallStreetData: 获取华尔街见闻数据失败,失败信息为：${e.message}")
                    it.copy(
                        isLoading = false,
                        error = "获取华尔街见闻数据失败,失败信息为：${e.message}"
                    )
                }
            }
        }
    }

    private fun getWeiboData() {
        updatePageState(NewsType.WEIBO) {
            Log.d(TAG, "getWeiboData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val weiboData = retrofitClient.weiboApiService.getWeiboData()
                if (weiboData.isSuccessful) {
                    val newsData = weiboData.body().let {
                        if (it?.ok != 1) {
                            return@let null
                        }
                        val newsDataTemp =
                            it.data.cards.firstOrNull()?.card_group?.filterIndexed { index, _ ->
                                index != 0 //过滤第一个元素
                            }?.filter { card ->
                                Log.d(
                                    TAG,
                                    "getWeiboData:card.actionlog.ext = ${card.actionlog.ext}"
                                )
                                !card.actionlog.ext.contains("ads_word")
                            }?.map { card ->
                                NewsData(
                                    title = card.desc,
                                    url = card.scheme
                                )
                            }
                        return@let newsDataTemp
                    }
                    Log.d(TAG, "getWeiboData: success newsData.size = ${newsData?.size}")
                    updatePageState(NewsType.WEIBO) {
                        it.copy(isLoading = false, data = newsData)
                    }
                } else {
                    updatePageState(NewsType.WEIBO) {
                        Log.d(TAG, "getWeiboData: 获取微博数据失败.失败code为${weiboData.code()}")
                        it.copy(
                            isLoading = false,
                            error = "获取微博数据失败.失败code为${weiboData.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                updatePageState(NewsType.WEIBO) {
                    Log.d(TAG, "getWeiboData: 获取微博数据失败,失败信息为：${e.message}")
                    it.copy(isLoading = false, error = "获取微博数据失败,失败信息为：${e.message}")
                }
            }
        }
    }

    private fun getZhiHuData() {
        updatePageState(NewsType.ZHIHU) {
            Log.d(TAG, "getZhiHuData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val zhiHuData = retrofitClient.zhiHuApiService.getZhiHuData()
                if (zhiHuData.isSuccessful) {
                    zhiHuData.body().let {
                        val newsData = it?.data?.map { item ->
                            NewsData(item.target.title_area.text, item.target.link.url)
                        }
                        Log.d(TAG, "getZhiHuData: success newsData.size = ${newsData?.size}")
                        updatePageState(NewsType.ZHIHU) { pageState ->
                            pageState.copy(isLoading = false, data = newsData)
                        }
                    }
                } else {
                    updatePageState(NewsType.ZHIHU) {
                        Log.d(TAG, "getZhiHuData: 获取知乎数据失败.失败code为${zhiHuData.code()}")
                        it.copy(
                            isLoading = false,
                            error = "获取知乎数据失败.失败code为${zhiHuData.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                updatePageState(NewsType.ZHIHU) {
                    Log.d(TAG, "getZhiHuData: 获取知乎数据失败,失败信息为：${e.message}")
                    it.copy(isLoading = false, error = "获取知乎数据失败,失败信息为：${e.message}")
                }
            }
        }
    }

    private fun getToutiaoData() {
        updatePageState(NewsType.TOUTIAO) {
            Log.d(TAG, "getToutiaoData: start")
            it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                val touTiaoData = retrofitClient.touTiaoApiService.getHotBoard()
                if (touTiaoData.isSuccessful) {
                    touTiaoData.body().let {
                        val newsData = it?.data?.map { item ->
                            NewsData(
                                item.Title,
                                "https://www.toutiao.com/trending/${item.ClusterIdStr}"
                            )
                        }
                        Log.d(TAG, "getToutiaoData: success newsData.size = ${newsData?.size}")
                        updatePageState(NewsType.TOUTIAO) { pageState ->
                            pageState.copy(isLoading = false, data = newsData)
                        }
                    }
                } else {
                    updatePageState(NewsType.TOUTIAO) {
                        Log.d(
                            TAG,
                            "getToutiaoData: 获取今日头条数据失败.失败code为${touTiaoData.code()}"
                        )
                        it.copy(
                            isLoading = false,
                            error = "获取今日头条数据失败.失败code为${touTiaoData.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                updatePageState(NewsType.TOUTIAO) {
                    Log.d(TAG, "getToutiaoData: 获取今日头条数据失败,失败信息为：${e.message}")
                    it.copy(
                        isLoading = false,
                        error = "获取今日头条数据失败,失败信息为：${e.message}"
                    )
                }
            }
        }

    }

    fun refreshData() {
        Log.d(TAG, "refreshData: start")
        getData(newsType)
        Log.d(TAG, "refreshData: end")
    }


    private fun <T> safeFlow(block: suspend () -> T): Flow<RequestState<T>> = flow {
        try {
            emit(RequestState.Loading)
            val result = block()
            emit(RequestState.Success(result))
        } catch (e: CancellationException) {
            // 忽略取消
        } catch (e: Exception) {
            emit(RequestState.Error(e))
        }
    }

    private fun updatePageState(pageType: NewsType, update: (ResponseState) -> ResponseState) {
        updateState { currentState ->
            currentState.copy(
                multiListState = currentState.multiListState.toMutableMap().apply {
                    this[pageType] = update(this[pageType]!!)
                }
            )
        }
    }

    private fun updateState(update: (MultiState) -> MultiState) {
        _newsData.update {
            update(it)
        }
    }

    private fun getCookiesForResponse(cookieResponse: Response<Void>):String{
        val cookie = cookieResponse.headers().values("Set-Cookie")
        val cookieString = cookie.joinToString("; ") { it ->
            it.substringBefore(";")
        }

        return cookieString
    }

}

