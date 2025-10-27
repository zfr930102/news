package com.fr.news.model.data

import com.google.gson.annotations.SerializedName

data class XueQiuResponse(
    @SerializedName("data")
    val data: StockData
)
// 包含股票项列表的数据结构
data class StockData(
    @SerializedName("items")
    val items: List<StockItem>
)

// 单个股票项的数据类
data class StockItem(
    @SerializedName("code")
    val code: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("percent")
    val percent: Double,

    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("ad")
    val ad: Int
)