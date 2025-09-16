package com.fr.news.model.data

data class ToutiaoResponse(
    val data: List<ToutiaoData>?,
    val fixed_top_data: List<ToutiaoData>?,
    val fixed_top_style: String?,
    val impr_id:String?,
    val status:String?
)
data class ToutiaoData(
    val ClusterId: Double,
    val ClusterIdStr: String,
    val ClusterType: Int,
    val HotValue: String,
    val Image: Image,
    val Label: String,
    val LabelDesc: String,
    val LabelUri: LabelUri,
    val LabelUrl: String,
    val QueryWord: String,
    val Schema: String,
    val Title: String,
    val Url: String
)

data class Image(
    val height: Int,
    val image_type: Int,
    val uri: String,
    val url: String,
    val url_list: List<Url>,
    val width: Int
)

data class LabelUri(
    val height: Int,
    val image_type: Int,
    val uri: String,
    val url: String,
    val url_list: List<Url>,
    val width: Int
)

data class Url(
    val url: String
)