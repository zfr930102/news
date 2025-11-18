package com.fr.news.model.service.api_interface

import com.fr.news.model.data.BaiduTieBaResponse
import com.fr.news.model.data.CLSDepthResponse
import com.fr.news.model.data.CLSHotResponse
import com.fr.news.model.data.CLSTelegraphResponse
import com.fr.news.model.data.ToutiaoResponse
import com.fr.news.model.data.WallStreetResponse
import com.fr.news.model.data.WeiboResponse
import com.fr.news.model.data.ZhiHuResponse
import com.fr.news.model.data.DouYinResponse
import com.fr.news.model.data.NewsNowResponse
import com.fr.news.model.data.XueQiuResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface ToutiaoApiService {
    @GET("hot-event/hot-board/")
    suspend fun getHotBoard(@Query("origin") origin: String = "toutiao_mobile"): Response<ToutiaoResponse>
}

interface WeiboApiService{
    @Headers("referer:https://s.weibo" +
            ".com/top/summary?cate=realtimehot","mweibo-pwa:1","x-requested-with:XMLHttpRequest",
        "Accept:application/json","Accept-Language:zh-CN","Accept-Encoding:gzip",
        "User-Agent:Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36")
    @GET("api/container/getIndex")
    suspend fun getWeiboData(
        @Query("containerid") containerid: String = "106003type%3D25%26t%3D3%26disable_hot%3D1%26filter_type%3Drealtimehot&title=%E5%BE%AE%E5%8D%9A%E7%83%AD%E6%90%9C&extparam=filter_type%3Drealtimehot%26mi_cid%3D100103%26pos%3D0_0%26c_type%3D30%26display_time%3D1540538388&luicode=10000011&lfid=231583")
    : Response<WeiboResponse>
}

interface ZhiHuApiService {
    @GET("api/v3/feed/topstory/hot-list-web")
    suspend fun getZhiHuData(
        @Query("limit") limit: Int = 20,
        @Query("desktop")isDesktop: Boolean = true
    ): Response<ZhiHuResponse>
}

interface WallStreetCNApiService {
    @GET("apiv1/content/information-flow")
    suspend fun getWallStreetData(
        @Query("channel") channel: String = "global-channel",
        @Query("accept") accept: String = "article",
        @Query("limit") limit: Int = 30
    ): Response<WallStreetResponse>
}

interface DouYinApiService {
    @GET("passport/general/login_guiding_strategy/")
    suspend fun getDouYinLoginCookie(
        @Query("aid") aid: Int = 6383
    ): Response<Void> //只关心响应头不关心响应体

    @GET("aweme/v1/web/hot/search/list/")
    suspend fun getDouYinData(
        @Query("device_platform") devicePlatform: String = "webapp",
        @Query("aid") aid: Int = 6383,
        @Query("channel") channel: String = "channel_pc_web",
        @Query("detail_list") detailList: Int = 1,
        @Header("Cookie") cookie: String
    ): Response<DouYinResponse>
}

interface BaiduTieBaApiService {
    @GET("hottopic/browse/topicList")
    suspend fun getTieBaData(
        @Query("from") from: String = "pc",
        @Query("kw") kw: String = "百度",
        @Query("pn") pn: Int = 1,
        @Query("rn") rn: Int = 10
    ): Response<BaiduTieBaResponse>
}

interface XueQiuCookieApiService{
    @GET("hp")
    suspend fun getXueQiuCookie(): Response<Void>
}

interface XueQiuApiService {
    @GET("v5/stock/hot_stock/list.json")
    suspend fun getXueQiuData(
        @Query("size") size:Int = 30,
        @Query("_type") _type :Int = 10,
        @Query("type") type:Int = 10,
        @Header("Cookie") cookie:String
    ): Response<XueQiuResponse>
}

interface CLSApiService {
    @GET("v3/depth/home/assembled/1000")
    suspend fun getCLSDepthData(@Query("query") query:String): Response<CLSDepthResponse>

    @GET("v2/article/hot/list")
    suspend fun getCLSHotData(@Query("query") query:String): Response<CLSHotResponse>

    @GET("nodeapi/updateTelegraphList")
    suspend fun getCLSTelegraphData(@Query("query") query:String): Response<CLSTelegraphResponse>
}

interface NewsNowApiService {
    @GET("api/s")
    suspend fun getNewsNowData(
        @Query("id") id: String,
    ): Response<NewsNowResponse>
}


