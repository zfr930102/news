package com.fr.news.model.data

data class WallStreetResponse(
    val code: Int,
    val data: WallStreetData,
    val message: String
)

data class WallStreetData(
    val is_inhouse: Boolean,
    val item_count: Int,
    val items: List<Item>,
    val next_cursor: String,
    val next_page_token: String,
    val protection: String
)

data class Item(
    val resource: Resource,
    val resource_owner: String,
    val resource_type: String
)

data class Resource(
    val article: Article,
    val author: Author,
    val ban_comment: Boolean,
    val categories: List<Category>,
    val channels: List<String>,
    val comment_count: Int,
    val content: String,
    val content_args: List<ContentArg>,
    val content_more: String,
    val content_short: String,
    val content_text: String,
    val contents: List<Content>,
    val cover_images: List<Any?>,
    val display_time: Int,
    val global_channel_name: String,
    val global_more_uri: String,
    val hang_down: HangDown,
    val highlight_title: String,
    val id: Int,
    val image: ImageXXXX,
    val image_uri: String,
    val images: List<Any?>,
    val is_calendar: Boolean,
    val is_favourite: Boolean,
    val is_in_vip_privilege: Boolean,
    val is_istio_api: Boolean,
    val is_paid: Boolean,
    val is_priced: Boolean,
    val is_scaling: Boolean,
    val is_trial: Boolean,
    val layout: String,
    val limited_time: Int,
    val most_recent_content_time: Int,
    val next: Any,
    val previous: Any,
    val reference: String,
    val related_themes: List<RelatedTheme>,
    val related_topics: List<RelatedTopic>,
    val score: Int,
    val source_name: String,
    val source_uri: String,
    val subtitle: String,
    val symbols: List<Any?>,
    val tags: List<Any?>,
    val title: String,
    val type: String,
    val uri: String,
    val vip_type: String
)

data class Article(
    val id: Int,
    val image: ImageXXXX,
    val platforms: List<String>,
    val title: String,
    val uri: String
)

data class Author(
    val avatar: String,
    val display_name: String,
    val id: Long,
    val is_followed: Boolean,
    val uri: String
)

data class Category(
    val property_key: String,
    val property_name: String
)

data class ContentArg(
    val `class`: String,
    val cover_img_uri: String,
    val duration: String,
    val height: String,
    val placeholder: String,
    val show_on_global: Boolean,
    val size: String,
    val src: String,
    val title: String,
    val type: String,
    val uri: String,
    val width: String
)

data class Content(
    val resource: ResourceX,
    val resource_type: String
)

data class HangDown(
    val image: List<ImageXXX>,
    val video: List<VideoX>
)

data class ImageXXXX(
    val height: Int,
    val size: Int,
    val uri: String,
    val width: Int
)

data class RelatedTheme(
    val description: String,
    val id: Int,
    val image_uri: String,
    val is_followed: Boolean,
    val is_priced: Boolean,
    val key: String,
    val title: String,
    val type: String,
    val uri: String
)

data class RelatedTopic(
    val id: Int,
    val image_uri: String,
    val title: String,
    val uri: String
)

data class ResourceX(
    val content: String,
    val content_short: String,
    val cover_images: Any,
    val id: Int,
    val image: ImageXXXX,
    val images: List<ImageXXXX>,
    val title: String,
    val uri: String,
    val videos: List<Video>
)

data class Video(
    val cover_img_uri: String,
    val duration: String,
    val height: Int,
    val title: String,
    val uri: String,
    val width: Int
)

data class ImageXXX(
    val height: String,
    val isCut: String,
    val size: String,
    val type: String,
    val uri: String,
    val width: String
)

data class VideoX(
    val cover_img_uri: String,
    val duration: String,
    val height: String,
    val show_global: String,
    val size: String,
    val title: String,
    val type: String,
    val uri: String,
    val width: String
)
