package com.fr.news.utils
import okhttp3.Interceptor
import okhttp3.Response

class DebugInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // 复制响应体以便检查
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer?.clone()

        val rawContent = buffer?.readString(Charsets.UTF_8) ?: ""

        println("=== RAW RESPONSE ANALYSIS ===")
        println("URL: ${request.url}")
        println("Status: ${response.code} ${response.message}")
        println("Content-Type: ${response.header("Content-Type")}")
        println("Content-Length: ${rawContent.length}")
        println("First 500 chars: ${rawContent.take(500)}")
        println("Starts with: '${rawContent.take(10)}'")

        // 检查常见问题
        checkCommonIssues(rawContent)

        return response
    }

    private fun checkCommonIssues(content: String) {
        when {
            content.trim().isEmpty() -> {
                println("❌ 问题: 响应为空")
            }
            content.startsWith("<") -> {
                println("❌ 问题: 响应是HTML而不是JSON")
                if (content.contains("cloudflare") || content.contains("Security")) {
                    println("⚠️  可能被Cloudflare拦截")
                }
            }
            content.startsWith("{") || content.startsWith("[") -> {
                println("✅ 格式: 看起来是有效的JSON")
                // 尝试验证JSON
                try {
                    val json = com.google.gson.JsonParser.parseString(content)
                    println("✅ JSON语法验证通过")
                } catch (e: Exception) {
                    println("❌ JSON语法错误: ${e.message}")
                }
            }
            content.startsWith("\"") -> {
                println("⚠️  格式: JSON字符串（可能双重编码）")
            }
            else -> {
                println("❓ 未知格式")
            }
        }
    }
}