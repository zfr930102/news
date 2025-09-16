package com.fr.news.model.service.api_interface

import com.fr.news.model.data.BaiduTieBaResponse
import com.fr.news.model.data.ToutiaoResponse
import com.fr.news.model.data.WallStreetResponse
import com.fr.news.model.data.WeiboResponse
import com.fr.news.model.data.ZhiHuResponse
import com.fr.news.model.data.DouYinResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ToutiaoApiService {
    @GET("hot-event/hot-board/")
    suspend fun getHotBoard(@Query("origin") origin: String = "toutiao_mobile"): Response<ToutiaoResponse>
}

interface WeiboApiService{
    @GET("api/container/getIndex")
    suspend fun getWeiboData(
        @Query("containerid") containerid: String = "106003",
        @Query("type") type:Int = 25,
        @Query("t") t:Int = 3,
        @Query("disable_hot") disableHot: Int = 1,
        @Query("filter_type") filterType: String = "realtimehot")
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



