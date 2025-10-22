package com.fr.news.state

import com.fr.news.constant.NewsType
import com.fr.news.view_model.newsTitleList

data class MultiState(
    val currentPageType: NewsType = NewsType.ZHIHU,
    val multiListState: Map<NewsType, ResponseState> = newsTitleList.associateBy({ it.type }, { ResponseState() })
)