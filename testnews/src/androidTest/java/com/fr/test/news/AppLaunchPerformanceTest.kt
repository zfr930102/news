package com.fr.test.news

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.fr.test.news.result.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppLaunchPerformanceTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context

    private val packageName = BuildConfig.TARGET_PACKAGE
    private val activityName = BuildConfig.TARGET_ACTIVITY

    // 测试配置
    private val testConfig = TestConfig(
        coldStartIterations = 10,
        warmStartIterations = 10,
        hotStartIterations = 10,
        waitTimeBeforeLaunch = 3000,  // 3秒
        waitTimeAfterLaunch = 2000     // 2秒
    )

    // 测试结果存储
    private val coldStartResults = mutableListOf<LaunchResult>()
    private val warmStartResults = mutableListOf<LaunchResult>()
    private val hotStartResults = mutableListOf<LaunchResult>()

    private var testStartTime = 0L
    private var testEndTime = 0L

    companion object {
        private const val TAG = "LaunchPerformanceTest"
        private const val LAUNCH_TIMEOUT = 15000L  // 15秒超时
    }

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        testStartTime = System.currentTimeMillis()

        Log.d(TAG, "========================================")
        Log.d(TAG, "开始 App 启动性能测试")
        Log.d(TAG, "目标应用: $packageName")
        Log.d(TAG, "========================================")
    }

    @Test
    fun testAppLaunchPerformance() {
        // 1. 测试冷启动
        testColdStart()

        // 2. 测试温启动
        testWarmStart()

        // 3. 测试热启动
        testHotStart()

        // 4. 生成测试报告
        generateReport()
    }

    /**
     * 测试冷启动
     */
    private fun testColdStart() {
        Log.d(TAG, "\n========== 开始冷启动测试 ==========")
        Log.d(TAG, "测试次数: ${testConfig.coldStartIterations}")

        repeat(testConfig.coldStartIterations) { index ->
            val testNumber = index + 1
            Log.d(TAG, "\n--- 第 $testNumber 次冷启动测试 ---")

            try {
                // 强制停止应用（模拟冷启动）
                device.executeShellCommand("am force-stop $packageName")
                Thread.sleep(testConfig.waitTimeBeforeLaunch)

                // 清除 logcat
                device.executeShellCommand("logcat -c")

                // 启动应用并测量时间
                val startTime = System.currentTimeMillis()
                val output = device.executeShellCommand(
                    "am start -W -n $packageName$activityName"
                )

                // 等待应用完全启动
                val launched = device.wait(
                    Until.hasObject(By.pkg(packageName).depth(0)),
                    LAUNCH_TIMEOUT
                )

                val endTime = System.currentTimeMillis()

                if (!launched) {
                    Log.e(TAG, "应用启动超时")
                    coldStartResults.add(
                        LaunchResult(
                            testNumber = testNumber,
                            launchType = LaunchType.COLD,
                            totalTime = -1,
                            waitTime = -1,
                            thisTime = -1,
                            displayedTime = -1,
                            customMeasuredTime = endTime - startTime,
                            success = false,
                            errorMessage = "启动超时"
                        )
                    )
                    return@repeat
                }

                // 解析启动时间
                val times = parseAmStartOutput(output)

                // 从 logcat 获取 Displayed 时间
                Thread.sleep(1000)
                val displayedTime = getDisplayedTimeFromLogcat()

                val result = LaunchResult(
                    testNumber = testNumber,
                    launchType = LaunchType.COLD,
                    totalTime = times["TotalTime"] ?: -1,
                    waitTime = times["WaitTime"] ?: -1,
                    thisTime = times["ThisTime"] ?: -1,
                    displayedTime = displayedTime,
                    customMeasuredTime = endTime - startTime
                )

                coldStartResults.add(result)

                Log.d(TAG, "TotalTime: ${result.totalTime}ms")
                Log.d(TAG, "DisplayedTime: ${result.displayedTime}ms")
                Log.d(TAG, "自定义测量时间: ${result.customMeasuredTime}ms")

                // 等待一段时间再进行下一次测试
                Thread.sleep(testConfig.waitTimeAfterLaunch)

            } catch (e: Exception) {
                Log.e(TAG, "测试失败: ${e.message}", e)
                coldStartResults.add(
                    LaunchResult(
                        testNumber = testNumber,
                        launchType = LaunchType.COLD,
                        totalTime = -1,
                        waitTime = -1,
                        thisTime = -1,
                        displayedTime = -1,
                        customMeasuredTime = -1,
                        success = false,
                        errorMessage = e.message
                    )
                )
            }
        }
    }

    /**
     * 测试温启动
     */
    private fun testWarmStart() {
        Log.d(TAG, "\n========== 开始温启动测试 ==========")
        Log.d(TAG, "测试次数: ${testConfig.warmStartIterations}")

        repeat(testConfig.warmStartIterations) { index ->
            val testNumber = index + 1
            Log.d(TAG, "\n--- 第 $testNumber 次温启动测试 ---")

            try {
                // 先启动一次应用
                if (index == 0) {
                    device.executeShellCommand("am start -n $packageName$activityName")
                    device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
                    Thread.sleep(1000)
                }

                // 按 Home 键（保留进程，但销毁 Activity）
                device.pressHome()
                Thread.sleep(1000)

                // 使用 am kill 销毁 Activity（但保留进程）
                device.executeShellCommand("am kill $packageName")
                Thread.sleep(testConfig.waitTimeBeforeLaunch)

                // 清除 logcat
                device.executeShellCommand("logcat -c")

                // 启动应用并测量时间
                val startTime = System.currentTimeMillis()
                val output = device.executeShellCommand(
                    "am start -W -n $packageName$activityName"
                )

                val launched = device.wait(
                    Until.hasObject(By.pkg(packageName).depth(0)),
                    LAUNCH_TIMEOUT
                )

                val endTime = System.currentTimeMillis()

                if (!launched) {
                    Log.e(TAG, "应用启动超时")
                    warmStartResults.add(
                        LaunchResult(
                            testNumber = testNumber,
                            launchType = LaunchType.WARM,
                            totalTime = -1,
                            waitTime = -1,
                            thisTime = -1,
                            displayedTime = -1,
                            customMeasuredTime = endTime - startTime,
                            success = false,
                            errorMessage = "启动超时"
                        )
                    )
                    return@repeat
                }

                val times = parseAmStartOutput(output)
                Thread.sleep(1000)
                val displayedTime = getDisplayedTimeFromLogcat()

                val result = LaunchResult(
                    testNumber = testNumber,
                    launchType = LaunchType.WARM,
                    totalTime = times["TotalTime"] ?: -1,
                    waitTime = times["WaitTime"] ?: -1,
                    thisTime = times["ThisTime"] ?: -1,
                    displayedTime = displayedTime,
                    customMeasuredTime = endTime - startTime
                )

                warmStartResults.add(result)

                Log.d(TAG, "TotalTime: ${result.totalTime}ms")
                Log.d(TAG, "DisplayedTime: ${result.displayedTime}ms")

                Thread.sleep(testConfig.waitTimeAfterLaunch)

            } catch (e: Exception) {
                Log.e(TAG, "测试失败: ${e.message}", e)
                warmStartResults.add(
                    LaunchResult(
                        testNumber = testNumber,
                        launchType = LaunchType.WARM,
                        totalTime = -1,
                        waitTime = -1,
                        thisTime = -1,
                        displayedTime = -1,
                        customMeasuredTime = -1,
                        success = false,
                        errorMessage = e.message
                    )
                )
            }
        }
    }

    /**
     * 测试热启动
     */
    private fun testHotStart() {
        Log.d(TAG, "\n========== 开始热启动测试 ==========")
        Log.d(TAG, "测试次数: ${testConfig.hotStartIterations}")

        repeat(testConfig.hotStartIterations) { index ->
            val testNumber = index + 1
            Log.d(TAG, "\n--- 第 $testNumber 次热启动测试 ---")

            try {
                // 先启动应用
                if (index == 0) {
                    device.executeShellCommand("am start -n $packageName$activityName")
                    device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
                    Thread.sleep(1000)
                }

                // 按 Home 键（应用进程和 Activity 都保留）
                device.pressHome()
                Thread.sleep(1000)

                // 清除 logcat
                device.executeShellCommand("logcat -c")

                // 启动应用并测量时间
                val startTime = System.currentTimeMillis()
                val output = device.executeShellCommand(
                    "am start -W -n $packageName$activityName"
                )

                val launched = device.wait(
                    Until.hasObject(By.pkg(packageName).depth(0)),
                    LAUNCH_TIMEOUT
                )

                val endTime = System.currentTimeMillis()

                if (!launched) {
                    Log.e(TAG, "应用启动超时")
                    hotStartResults.add(
                        LaunchResult(
                            testNumber = testNumber,
                            launchType = LaunchType.HOT,
                            totalTime = -1,
                            waitTime = -1,
                            thisTime = -1,
                            displayedTime = -1,
                            customMeasuredTime = endTime - startTime,
                            success = false,
                            errorMessage = "启动超时"
                        )
                    )
                    return@repeat
                }

                val times = parseAmStartOutput(output)
                Thread.sleep(1000)
                val displayedTime = getDisplayedTimeFromLogcat()

                val result = LaunchResult(
                    testNumber = testNumber,
                    launchType = LaunchType.HOT,
                    totalTime = times["TotalTime"] ?: -1,
                    waitTime = times["WaitTime"] ?: -1,
                    thisTime = times["ThisTime"] ?: -1,
                    displayedTime = displayedTime,
                    customMeasuredTime = endTime - startTime
                )

                hotStartResults.add(result)

                Log.d(TAG, "TotalTime: ${result.totalTime}ms")
                Log.d(TAG, "DisplayedTime: ${result.displayedTime}ms")

                Thread.sleep(testConfig.waitTimeAfterLaunch)

            } catch (e: Exception) {
                Log.e(TAG, "测试失败: ${e.message}", e)
                hotStartResults.add(
                    LaunchResult(
                        testNumber = testNumber,
                        launchType = LaunchType.HOT,
                        totalTime = -1,
                        waitTime = -1,
                        thisTime = -1,
                        displayedTime = -1,
                        customMeasuredTime = -1,
                        success = false,
                        errorMessage = e.message
                    )
                )
            }
        }
    }

    /**
     * 解析 am start -W 的输出
     */
    private fun parseAmStartOutput(output: String): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        val totalTimeRegex = "TotalTime: (\\d+)".toRegex()
        val waitTimeRegex = "WaitTime: (\\d+)".toRegex()
        val thisTimeRegex = "ThisTime: (\\d+)".toRegex()

        totalTimeRegex.find(output)?.let {
            result["TotalTime"] = it.groupValues[1].toLong()
        }

        waitTimeRegex.find(output)?.let {
            result["WaitTime"] = it.groupValues[1].toLong()
        }

        thisTimeRegex.find(output)?.let {
            result["ThisTime"] = it.groupValues[1].toLong()
        }

        return result
    }

    /**
     * 从 logcat 获取 Displayed 时间
     */
    private fun getDisplayedTimeFromLogcat(): Long {
        val output = device.executeShellCommand(
            "logcat -d | grep 'Displayed $packageName'"
        )

        // 解析格式: Displayed com.fr.news/.MainActivity: +1s234ms
        val regex1 = "\\+(\\d+)s(\\d+)ms".toRegex()
        val match1 = regex1.find(output)
        if (match1 != null) {
            val seconds = match1.groupValues[1].toLong()
            val millis = match1.groupValues[2].toLong()
            return seconds * 1000 + millis
        }

        // 解析格式: Displayed com.fr.news/.MainActivity: +234ms
        val regex2 = "\\+(\\d+)ms".toRegex()
        val match2 = regex2.find(output)
        if (match2 != null) {
            return match2.groupValues[1].toLong()
        }

        return -1
    }

    /**
     * 生成测试报告
     */
    private fun generateReport() {
        testEndTime = System.currentTimeMillis()

        Log.d(TAG, "\n========================================")
        Log.d(TAG, "生成测试报告")
        Log.d(TAG, "========================================")

        // 计算统计数据
        val coldStats = if (coldStartResults.isNotEmpty()) {
            StatisticsCalculator.calculate(LaunchType.COLD, coldStartResults)
        } else null

        val warmStats = if (warmStartResults.isNotEmpty()) {
            StatisticsCalculator.calculate(LaunchType.WARM, warmStartResults)
        } else null

        val hotStats = if (hotStartResults.isNotEmpty()) {
            StatisticsCalculator.calculate(LaunchType.HOT, hotStartResults)
        } else null

        // 评估性能等级
        val coldGrade = coldStats?.let {
            PerformanceStandards.evaluatePerformance(LaunchType.COLD, it.averageTime.toLong())
        } ?: PerformanceGrade.VERY_POOR

        val warmGrade = warmStats?.let {
            PerformanceStandards.evaluatePerformance(LaunchType.WARM, it.averageTime.toLong())
        } ?: PerformanceGrade.VERY_POOR

        val hotGrade = hotStats?.let {
            PerformanceStandards.evaluatePerformance(LaunchType.HOT, it.averageTime.toLong())
        } ?: PerformanceGrade.VERY_POOR

        // 生成优化建议
        val recommendations = PerformanceStandards.generateRecommendations(
            coldGrade, warmGrade, hotGrade,
            coldStats?.averageTime ?: 0.0,
            warmStats?.averageTime ?: 0.0,
            hotStats?.averageTime ?: 0.0
        )

        // 创建完整报告
        val report = LaunchTestReport(
            deviceInfo = getDeviceInfo(),
            testConfig = testConfig,
            coldStartResults = coldStartResults,
            warmStartResults = warmStartResults,
            hotStartResults = hotStartResults,
            coldStartStats = coldStats,
            warmStartStats = warmStats,
            hotStartStats = hotStats,
            coldStartGrade = coldGrade,
            warmStartGrade = warmGrade,
            hotStartGrade = hotGrade,
            recommendations = recommendations,
            testStartTime = testStartTime,
            testEndTime = testEndTime,
            appInfo = getAppInfo()
        )

        // 保存报告
        saveReport(report)
    }

    /**
     * 获取设备信息
     */
    private fun getDeviceInfo(): DeviceInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            cpuAbi = Build.SUPPORTED_ABIS[0],
            screenDensity = context.resources.displayMetrics.densityDpi,
            screenResolution = "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}",
            totalMemory = memoryInfo.totalMem / (1024 * 1024),
            availableMemory = memoryInfo.availMem / (1024 * 1024)
        )
    }

    /**
     * 获取应用信息
     */
    private fun getAppInfo(): AppInfo {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            AppInfo(
                packageName = packageName,
                versionName = packageInfo.versionName ?: "unknown",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                targetSdkVersion = packageInfo.applicationInfo?.targetSdkVersion ?: -1,
                minSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    packageInfo.applicationInfo?.minSdkVersion ?: -1
                } else {
                    -1
                }
            )
        } catch (e: PackageManager.NameNotFoundException) {
            AppInfo(
                packageName = packageName,
                versionName = "unknown",
                versionCode = -1,
                targetSdkVersion = -1,
                minSdkVersion = -1
            )
        }
    }

    /**
     * 保存报告
     */
    private fun saveReport(report: LaunchTestReport) {
        try {
            // 保存到外部存储
            val externalDir = context.getExternalFilesDir(null)
            val reportDir = File(externalDir, "launch_performance_reports")
            if (!reportDir.exists()) {
                reportDir.mkdirs()
            }

            val timestamp = java.text.SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            // 保存 JSON 格式报告
            val jsonFile = File(reportDir, "launch_report_$timestamp.json")
            jsonFile.writeText(report.toJson())
            Log.d(TAG, "JSON 报告已保存: ${jsonFile.absolutePath}")

            // 生成并保存 Markdown 格式报告
            val markdownReport = ReportGenerator.generateMarkdownReport(report)
            val mdFile = File(reportDir, "launch_report_$timestamp.md")
            mdFile.writeText(markdownReport)
            Log.d(TAG, "Markdown 报告已保存: ${mdFile.absolutePath}")

            // 生成并保存 HTML 格式报告
            val htmlReport = ReportGenerator.generateHtmlReport(report)
            val htmlFile = File(reportDir, "launch_report_$timestamp.html")
            htmlFile.writeText(htmlReport)
            Log.d(TAG, "HTML 报告已保存: ${htmlFile.absolutePath}")

            // 打印报告摘要到 Logcat
            printReportSummary(report)

        } catch (e: Exception) {
            Log.e(TAG, "保存报告失败: ${e.message}", e)
        }
    }

    /**
     * 打印报告摘要
     */
    private fun printReportSummary(report: LaunchTestReport) {
        Log.d(TAG, "\n")
        Log.d(TAG, "========================================")
        Log.d(TAG, "       App 启动性能测试报告摘要")
        Log.d(TAG, "========================================")
        Log.d(TAG, "")
        Log.d(TAG, "【设备信息】")
        Log.d(TAG, "设备: ${report.deviceInfo.manufacturer} ${report.deviceInfo.model}")
        Log.d(TAG, "Android: ${report.deviceInfo.androidVersion} (API ${report.deviceInfo.sdkVersion})")
        Log.d(TAG, "")
        Log.d(TAG, "【应用信息】")
        Log.d(TAG, "包名: ${report.appInfo.packageName}")
        Log.d(TAG, "版本: ${report.appInfo.versionName} (${report.appInfo.versionCode})")
        Log.d(TAG, "")

        report.coldStartStats?.let { stats ->
            Log.d(TAG, "【冷启动】${report.coldStartGrade.emoji}")
            Log.d(TAG, "评级: ${report.coldStartGrade.displayName}")
            Log.d(TAG, "平均时间: ${stats.averageTime.toInt()}ms")
            Log.d(TAG, "中位数: ${stats.medianTime}ms")
            Log.d(TAG, "最快: ${stats.minTime}ms | 最慢: ${stats.maxTime}ms")
            Log.d(TAG, "")
        }

        report.warmStartStats?.let { stats ->
            Log.d(TAG, "【温启动】${report.warmStartGrade.emoji}")
            Log.d(TAG, "评级: ${report.warmStartGrade.displayName}")
            Log.d(TAG, "平均时间: ${stats.averageTime.toInt()}ms")
            Log.d(TAG, "中位数: ${stats.medianTime}ms")
            Log.d(TAG, "最快: ${stats.minTime}ms | 最慢: ${stats.maxTime}ms")
            Log.d(TAG, "")
        }

        report.hotStartStats?.let { stats ->
            Log.d(TAG, "【热启动】${report.hotStartGrade.emoji}")
            Log.d(TAG, "评级: ${report.hotStartGrade.displayName}")
            Log.d(TAG, "平均时间: ${stats.averageTime.toInt()}ms")
            Log.d(TAG, "中位数: ${stats.medianTime}ms")
            Log.d(TAG, "最快: ${stats.minTime}ms | 最慢: ${stats.maxTime}ms")
            Log.d(TAG, "")
        }

        Log.d(TAG, "【优化建议】")
        report.recommendations.forEach { recommendation ->
            Log.d(TAG, recommendation)
        }

        Log.d(TAG, "")
        Log.d(TAG, "========================================")
    }

    @After
    fun tearDown() {
        // 清理：停止应用
        try {
            device.executeShellCommand("am force-stop $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "清理失败: ${e.message}")
        }
    }
}
