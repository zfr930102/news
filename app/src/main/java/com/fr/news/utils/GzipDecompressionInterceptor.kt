package com.fr.news.utils

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.GzipSource

class GzipDecompressionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val response = chain.proceed(request)

        // 检查并处理 GZIP 压缩
        val contentEncoding = response.header("Content-Encoding")
        if (contentEncoding != null && contentEncoding.contains("gzip", true)) {
            // OkHttp 通常会自动处理 GZIP，但如果出现问题可以手动处理
            val source = response.body?.source()
            if (source != null) {
                try {
                    val gzipSource = GzipSource(source)
                    val buffer = Buffer()
                    buffer.writeAll(gzipSource)
                    gzipSource.close()

                    val contentType = response.body?.contentType()
                    val responseBody =
                        okhttp3.ResponseBody.create(contentType, buffer.readByteString())
                    Log.d(TAG, "intercept: responseBody contentLength =: ${responseBody.contentLength()}")
                    return response.newBuilder()
                        .body(responseBody)
                        .header("Content-Encoding", "") // 移除编码头避免重复处理
                        .build()
                } catch (e: Exception) {
                    // 如果手动处理失败，返回原始响应
                    return response
                }
            }
        }
        return response
    }
}
