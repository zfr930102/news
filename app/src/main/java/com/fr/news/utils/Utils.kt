package com.fr.news.utils

import android.util.Log
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
const val TAG = "Utils"
// URL编码函数
fun encodeURIComponent(text: String): String {
    return try {
        val encode = URLEncoder.encode(text, "UTF-8")
        Log.d(TAG, "encodeURIComponent: encode = $encode")
        encode
    } catch (e: UnsupportedEncodingException) {
        Log.d(TAG, "encodeURIComponent: ")
        text
    }
}