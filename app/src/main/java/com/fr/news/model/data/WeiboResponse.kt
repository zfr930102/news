package com.fr.news.model.data

data class WeiboResponse(
    val data: WeiboData,
    val ok: Int
)

data class WeiboData(
    val cardlistInfo: CardlistInfo,
    val cards: List<Card>,
    val mapi: List<Any?>,
    val scheme: String,
    val showAppTips: Int
)

data class CardlistInfo(
    val can_shared: Int,
    val cardlist_head_cards: List<CardlistHeadCards>,
    val cardlist_menus: List<Any>,
    val config: Config,
    val containerid: String,
    val enable_load_imge_scrolling: Int,
    val headbg_animation: String,
    val load_more_threshold: Int,
    val nick: String,
    val page: Any,
    val page_common_ext: String,
    val page_size: Int,
    val page_title: String,
    val page_type: String,
    val pagesize: Int,
    val search_request_id: String,
    val select_id: String,
    val show_style: Int,
    val starttime: Int,
    val title_top: String,
    val total: Int,
    val v_p: String
)

data class Card(
    val card_group: List<CardGroup>,
    val card_type: Int,
    val itemid: String,
    val show_type: Int,
    val title: String
)


data class CardlistHeadCards(
    val channel_list: List<Channel>,
    val head_data: HeadData,
    val head_type: Int,
    val head_type_name: String,
    val show_menu: Boolean,
    val title_top: String
)

data class Config(
    val effective_cache_duration: Int
)

data class Channel(
    val actionlog: Actionlog,
    val containerid: String,
    val default_add: Int,
    val id: Int,
    val must_show: Int,
    val name: String,
    val replaced_name: String,
    val scheme: String
)

data class HeadData(
    val cover_url: String,
    val data_type: Int,
    val scheme: String,
    val show_navi_mask: Boolean,
    val show_title: Boolean
)

data class Actionlog(
    val act_code: Int,
    val act_type: Int,
    val ext: String,
    val fid: String,
    val lfid: String,
    val luicode: String,
    val uicode: String
)

data class CardGroup(
    val actionlog: Actionlog,
    val card_type: Int,
    val desc: String,
    val desc_extr: Int,
    val display_arrow: Int,
    val icon: String,
    val icon_height: Int,
    val icon_width: Int,
    val is_show_arrow: Int,
    val itemid: String,
    val left_tag_img: String,
    val pic: String,
    val promotion: Promotion,
    val scheme: String,
    val show_type: Int,
    val sub_title: String,
    val title: String
)

data class Promotion(
    val monitor_url: List<MonitorUrl>
)

data class MonitorUrl(
    val third_party_click: String,
    val third_party_show: String,
    val type: String
)
