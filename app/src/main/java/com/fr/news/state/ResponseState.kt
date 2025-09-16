package com.fr.news.state

import com.fr.news.model.data.NewsData

data class ResponseState(
    val isLoading: Boolean = false,
    val data: List<NewsData>? = null,
    val error: String? = null
)