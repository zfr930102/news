package com.fr.test.news.result

import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LaunchResult(
    val testNumber: Int,                    // 测试序号
    val launchType: LaunchType,            // 启动类型
    val totalTime: Long,                    // am start -W 返回的 TotalTime (ms)
    val waitTime: Long,                     // am start -W 返回的 WaitTime (ms)
    val thisTime: Long,                     // am start -W 返回的 ThisTime (ms)
    val displayedTime: Long,                // Logcat 中的 Displayed 时间 (ms)
    val customMeasuredTime: Long,           // 自定义测量时间 (ms)
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true,            // 是否成功
    val errorMessage: String? = null        // 错误信息
) {
    fun getTimestampString(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(timestamp))
    }
}

/**
 * 启动类型
 */
enum class LaunchType(val displayName: String, val description: String) {
    COLD("冷启动", "应用首次启动或进程被完全杀死后启动"),
    WARM("温启动", "应用进程存在但Activity被销毁后启动"),
    HOT("热启动", "应用在后台，直接从后台恢复")
}

/**
 * 测试统计数据
 */
data class LaunchStatistics(
    val launchType: LaunchType,
    val testCount: Int,                     // 测试次数
    val successCount: Int,                  // 成功次数
    val failureCount: Int,                  // 失败次数
    val averageTime: Double,                // 平均启动时间 (ms)
    val medianTime: Long,                   // 中位数 (ms)
    val minTime: Long,                      // 最快启动时间 (ms)
    val maxTime: Long,                      // 最慢启动时间 (ms)
    val standardDeviation: Double,          // 标准差
    val percentile90: Long,                 // 90分位数 (ms)
    val percentile95: Long,                 // 95分位数 (ms)
    val percentile99: Long                  // 99分位数 (ms)
)

/**
 * 性能评估等级
 */
enum class PerformanceGrade(
    val displayName: String,
    val color: String,
    val emoji: String
) {
    EXCELLENT("优秀", "#4CAF50", "🌟"),
    GOOD("良好", "#8BC34A", "✅"),
    ACCEPTABLE("可接受", "#FFC107", "⚠️"),
    POOR("较差", "#FF9800", "❌"),
    VERY_POOR("很差", "#F44336", "💔")
}

/**
 * 完整的测试报告
 */
data class LaunchTestReport(
    val deviceInfo: DeviceInfo,
    val testConfig: TestConfig,
    val coldStartResults: List<LaunchResult>,
    val warmStartResults: List<LaunchResult>,
    val hotStartResults: List<LaunchResult>,
    val coldStartStats: LaunchStatistics?,
    val warmStartStats: LaunchStatistics?,
    val hotStartStats: LaunchStatistics?,
    val coldStartGrade: PerformanceGrade,
    val warmStartGrade: PerformanceGrade,
    val hotStartGrade: PerformanceGrade,
    val recommendations: List<String>,
    val testStartTime: Long,
    val testEndTime: Long,
    val appInfo: AppInfo
) {
    fun getTotalDuration(): Long = testEndTime - testStartTime

    fun toJson(): String {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(this)
    }
}

/**
 * 设备信息
 */
data class DeviceInfo(
    val manufacturer: String,               // 制造商
    val model: String,                      // 型号
    val androidVersion: String,             // Android 版本
    val sdkVersion: Int,                    // SDK 版本
    val cpuAbi: String,                     // CPU 架构
    val screenDensity: Int,                 // 屏幕密度
    val screenResolution: String,           // 屏幕分辨率
    val totalMemory: Long,                  // 总内存 (MB)
    val availableMemory: Long               // 可用内存 (MB)
)

/**
 * 应用信息
 */
data class AppInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdkVersion: Int,
    val minSdkVersion: Int
)

/**
 * 测试配置
 */
data class TestConfig(
    val coldStartIterations: Int,           // 冷启动测试次数
    val warmStartIterations: Int,           // 温启动测试次数
    val hotStartIterations: Int,            // 热启动测试次数
    val waitTimeBeforeLaunch: Long,         // 启动前等待时间 (ms)
    val waitTimeAfterLaunch: Long           // 启动后等待时间 (ms)
)
