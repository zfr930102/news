package com.fr.news.model.data

data class CLSItem(
    val id: String,
    val title: String?,
    val brief: String?,
    val shareurl: String,
    val ctime: String,
    val is_ad: Boolean
)

data class CLSDepthData(
    val top_article:List<CLSItem>,
    val depth_list:List<CLSItem>
)
data class CLSDepthResponse(
    val data: CLSDepthData
)

data class CLSHotResponse(
    val data:List<CLSItem>
)
data class CLSTelegraphData(
    val roll_data: List<CLSItem>
)
data class CLSTelegraphResponse(
    val data: CLSTelegraphData
)
