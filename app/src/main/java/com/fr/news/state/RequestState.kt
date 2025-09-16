package com.fr.news.state

sealed class RequestState<out T> {
    object Loading : RequestState<Nothing>()
    data class Success<out T>(val data: T) : RequestState<T>()
    data class Error(val exception: Throwable) : RequestState<Nothing>()
}