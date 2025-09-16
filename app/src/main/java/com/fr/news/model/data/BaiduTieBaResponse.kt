package com.fr.news.model.data

data class BaiduTieBaResponse(
    val data: BaiduTieBaData,
    val errmsg: String,
    val errno: Int
)

data class BaiduTieBaData(
    val bang_head_pic: String,
    val bang_topic: BangTopic,
    val manual_topic: ManualTopic,
    val sug_topic: SugTopic,
    val timestamp: Long,
    val user_his_topic: UserHisTopic
)

data class BangTopic(
    val module_title: String,
    val topic_list: List<Topic>
)

data class ManualTopic(
    val module_title: String,
    val topic_list: List<Any>
)

data class SugTopic(
    val module_title: String,
    val topic_list: List<Any>
)

data class UserHisTopic(
    val module_title: String,
    val topic_list: List<Any>
)

data class Topic(
    val _abstract: String,
    val content_num: Int,
    val create_time: Int,
    val discuss_num: Int,
    val idx_num: Int,
    val is_video_topic: String,
    val tag: Int,
    val topic_avatar: String,
    val topic_default_avatar: String,
    val topic_desc: String,
    val topic_id: Int,
    val topic_name: String,
    val topic_pic: String,
    val topic_url: String
)
