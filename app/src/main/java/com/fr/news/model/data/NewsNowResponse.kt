package com.fr.news.model.data

import com.google.gson.annotations.SerializedName

data class NewsNowResponse(
    @SerializedName("status") val status: String,
    @SerializedName("id") val id: String,
    @SerializedName("updatedTime") val updatedTime: Long,
    @SerializedName("items") val items: List<NewsNowItem>
)

data class NewsNowItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("mobileUrl") val mobileUrl: String,
    @SerializedName("pubDate") val pubDate: Long,
    @SerializedName("url") val url: String,
    @SerializedName("extra") val extra: NewsNowExtra
)

data class NewsNowExtra(
    @SerializedName("icon") val icon: String
)