package com.fr.news.view_model

import com.fr.news.R
import com.fr.news.constant.NewsType

data class NewTitleItem(
    val imageRes: Int,
    val title: String,
    val type: NewsType
)

//差 酷安\虎扑
val newsTitleList = listOf(
    NewTitleItem(
        imageRes = R.mipmap.zhihu,
        title = "知乎",
        type = NewsType.ZHIHU
    ),

    NewTitleItem(
        imageRes = R.mipmap.weibo,
        title = "微博",
        type = NewsType.WEIBO
    ),
    NewTitleItem(
        imageRes = R.mipmap.wallstreetcn,
        title = "华尔街见闻",
        type = NewsType.WALLSTREET
    ),
    NewTitleItem(
        imageRes = R.mipmap.tieba,
        title = "百度贴吧",
        type = NewsType.TIEBA
    ),
    NewTitleItem(
        imageRes = R.mipmap.toutiao,
        title = "今日头条",
        type = NewsType.TOUTIAO
    ),
//    NewTitleItem(
//        imageRes = R.mipmap.thepaper,
//        title = "澎湃新闻",
//        type = NewsType.THEPAPER
//    ),
    NewTitleItem(
        imageRes = R.mipmap.xueqiu,
        title = "雪球",
        type = NewsType.XUEQIU
    ),
    NewTitleItem(
        imageRes = R.mipmap.cls,
        title = "财联社（电报）",
        type = NewsType.CLS_TELEGRAPH
    ),
//    NewTitleItem(
//        imageRes = R.mipmap.cls,
//        title = "财联社（深度）",
//        type = NewsType.CLS_DEPTH
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.cls,
//        title = "财联社（热门）",
//        type = NewsType.CLS_HOT
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.hackernews,
//        title = "Hacker News"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.producthunt,
//        title = "Product Hunt"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.github,
//        title = "GitHub"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.bilibili,
//        title = "哔哩哔哩"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.baidu,
//        title = "百度热搜"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.nowcoder,
//        title = "牛客网"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.sspai,
//        title = "少数派"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.juejin,
//        title = "稀土掘金"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.ifeng,
//        title = "凤凰网"
//    ),
//    NewTitleItem(
//        imageRes = R.mipmap.chongbuluo,
//        title = "虫部落"
//    )
)