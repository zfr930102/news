package com.fr.news.model.data

data class DouYinResponse(
    val banner_dark: BannerDark,
    val banner_light: BannerLight,
    val data: DouyinData,
    val extra: Extra,
    val log_pb: LogPb,
    val status_code: Int
)

data class BannerDark(
    val uri: String,
    val url_list: List<String>
)

data class BannerLight(
    val uri: String,
    val url_list: List<String>
)

data class DouyinData(
    val active_time: String,
    val display_style: Int,
    val share_info: ShareInfo,
    val trending_desc: String,
    val trending_list: List<Trending>,
    val word_list: List<Word>
)

data class Extra(
    val logid: String,
    val now: Long,
    val time_cost: TimeCost
)

data class LogPb(
    val impr_id: String
)

data class ShareInfo(
    val share_link_desc: String,
    val share_title: String,
    val share_url: String
)

data class Trending(
    val article_detail_count: Int,
    val discuss_video_count: Int,
    val display_style: Int,
    val event_time: Int,
    val group_id: String,
    val hot_value: Int,
    val hotlist_param: String,
    val label: Int,
    val sentence_id: String,
    val sentence_tag: Int,
    val video_count: Int,
    val word: String,
    val word_cover: WordCover,
    val word_type: Int
)

data class Word(
    val article_detail_count: Int,
    val can_extend_detail: Boolean,
    val discuss_video_count: Int,
    val display_style: Int,
    val event_time: Int,
    val group_id: String,
    val hot_value: Int,
    val hotlist_param: String,
    val is_n1: Boolean,
    val label: Int,
    val label_url: String,
    val max_rank: Int,
    val position: Int,
    val post_aweme_info: String,
    val room_count: Int,
    val sentence_id: String,
    val sentence_tag: Int,
    val video_count: Int,
    val word: String,
    val word_cover: WordCover,
    val word_sub_board: List<Int>,
    val word_type: Int
)

data class WordCover(
    val uri: String,
    val url_list: List<String>
)

data class TimeCost(
    val stream_inner: Int
)