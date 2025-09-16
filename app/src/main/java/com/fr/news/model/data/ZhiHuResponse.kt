package com.fr.news.model.data

data class ZhiHuResponse(
    val data: List<ZhiHuData>
)

data class ZhiHuData(
    val attached_info: String,
    val card_id: String,
    val card_label: CardLabel,
    val feed_specific: FeedSpecific,
    val id: String,
    val style_type: String,
    val target: Target,
    val type: String
)

data class CardLabel(
    val icon: String,
    val night_icon: String,
    val type: String
)

data class FeedSpecific(
    val answer_count: Int
)

data class Target(
    val excerpt_area: ExcerptArea,
    val image_area: ImageArea,
    val label_area: LabelArea,
    val link: Link,
    val metrics_area: MetricsArea,
    val title_area: TitleArea
)

data class ExcerptArea(
    val text: String
)

data class ImageArea(
    val url: String
)

data class LabelArea(
    val night_color: String,
    val normal_color: String,
    val trend: Int,
    val type: String
)

data class Link(
    val url: String
)

data class MetricsArea(
    val background: String,
    val font_color: String,
    val text: String,
    val weight: String
)

data class TitleArea(
    val text: String
)