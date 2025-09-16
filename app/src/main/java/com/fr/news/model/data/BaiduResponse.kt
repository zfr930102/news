package com.fr.news.model.data

class BaiduResponse : ArrayList<BaiduResponseItem>()

data class BaiduResponseItem(
    val id: String,
    val title: String,
    val url: String
)


