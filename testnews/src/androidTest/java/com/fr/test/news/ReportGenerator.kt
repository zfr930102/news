package com.fr.test.news

import com.fr.test.news.result.*
import java.text.SimpleDateFormat
import java.util.*

object ReportGenerator {

    fun generateMarkdownReport(report: LaunchTestReport): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return buildString {
            appendLine("# App 启动性能测试报告")
            appendLine()
            appendLine("## 📊 测试概览")
            appendLine()
            appendLine("| 项目 | 内容 |")
            appendLine("|:---|:---|")
            appendLine("| 测试开始时间 | ${dateFormat.format(Date(report.testStartTime))} |")
            appendLine("| 测试结束时间 | ${dateFormat.format(Date(report.testEndTime))} |")
            appendLine("| 总耗时 | ${report.getTotalDuration() / 1000}秒 |")
            appendLine()

            appendLine("## 📱 设备信息")
            appendLine()
            appendLine("| 项目 | 内容 |")
            appendLine("|:---|:---|")
            appendLine("| 设备型号 | ${report.deviceInfo.manufacturer} ${report.deviceInfo.model} |")
            appendLine("| Android 版本 | ${report.deviceInfo.androidVersion} (API ${report.deviceInfo.sdkVersion}) |")
            appendLine("| CPU 架构 | ${report.deviceInfo.cpuAbi} |")
            appendLine("| 屏幕分辨率 | ${report.deviceInfo.screenResolution} |")
            appendLine("| 屏幕密度 | ${report.deviceInfo.screenDensity} dpi |")
            appendLine("| 总内存 | ${report.deviceInfo.totalMemory} MB |")
            appendLine("| 可用内存 | ${report.deviceInfo.availableMemory} MB |")
            appendLine()

            appendLine("## 📦 应用信息")
            appendLine()
            appendLine("| 项目 | 内容 |")
            appendLine("|:---|:---|")
            appendLine("| 包名 | ${report.appInfo.packageName} |")
            appendLine("| 版本名称 | ${report.appInfo.versionName} |")
            appendLine("| 版本号 | ${report.appInfo.versionCode} |")
            appendLine("| Target SDK | ${report.appInfo.targetSdkVersion} |")
            appendLine("| Min SDK | ${report.appInfo.minSdkVersion} |")
            appendLine()

            // 冷启动统计
            report.coldStartStats?.let { stats ->
                appendLine("## ❄️ 冷启动测试结果 ${report.coldStartGrade.emoji}")
                appendLine()
                appendLine("**性能评级：${report.coldStartGrade.displayName}**")
                appendLine()
                appendLine("| 指标 | 数值 |")
                appendLine("|:---|:---|")
                appendLine("| 测试次数 | ${stats.testCount} |")
                appendLine("| 成功次数 | ${stats.successCount} |")
                appendLine("| 失败次数 | ${stats.failureCount} |")
                appendLine("| 平均时间 | ${stats.averageTime.toInt()} ms |")
                appendLine("| 中位数 | ${stats.medianTime} ms |")
                appendLine("| 最快时间 | ${stats.minTime} ms |")
                appendLine("| 最慢时间 | ${stats.maxTime} ms |")
                appendLine("| 标准差 | ${stats.standardDeviation.toInt()} ms |")
                appendLine("| 90分位数 | ${stats.percentile90} ms |")
                appendLine("| 95分位数 | ${stats.percentile95} ms |")
                appendLine("| 99分位数 | ${stats.percentile99} ms |")
                appendLine()

                appendLine("### 性能标准对比")
                appendLine()
                appendLine(PerformanceStandards.getStandardDescription(LaunchType.COLD))
                appendLine()
            }

            // 温启动统计
            report.warmStartStats?.let { stats ->
                appendLine("## 🌤️ 温启动测试结果 ${report.warmStartGrade.emoji}")
                appendLine()
                appendLine("**性能评级：${report.warmStartGrade.displayName}**")
                appendLine()
                appendLine("| 指标 | 数值 |")
                appendLine("|:---|:---|")
                appendLine("| 测试次数 | ${stats.testCount} |")
                appendLine("| 成功次数 | ${stats.successCount} |")
                appendLine("| 失败次数 | ${stats.failureCount} |")
                appendLine("| 平均时间 | ${stats.averageTime.toInt()} ms |")
                appendLine("| 中位数 | ${stats.medianTime} ms |")
                appendLine("| 最快时间 | ${stats.minTime} ms |")
                appendLine("| 最慢时间 | ${stats.maxTime} ms |")
                appendLine("| 标准差 | ${stats.standardDeviation.toInt()} ms |")
                appendLine("| 90分位数 | ${stats.percentile90} ms |")
                appendLine("| 95分位数 | ${stats.percentile95} ms |")
                appendLine("| 99分位数 | ${stats.percentile99} ms |")
                appendLine()
            }

            // 热启动统计
            report.hotStartStats?.let { stats ->
                appendLine("## ☀️ 热启动测试结果 ${report.hotStartGrade.emoji}")
                appendLine()
                appendLine("**性能评级：${report.hotStartGrade.displayName}**")
                appendLine()
                appendLine("| 指标 | 数值 |")
                appendLine("|:---|:---|")
                appendLine("| 测试次数 | ${stats.testCount} |")
                appendLine("| 成功次数 | ${stats.successCount} |")
                appendLine("| 失败次数 | ${stats.failureCount} |")
                appendLine("| 平均时间 | ${stats.averageTime.toInt()} ms |")
                appendLine("| 中位数 | ${stats.medianTime} ms |")
                appendLine("| 最快时间 | ${stats.minTime} ms |")
                appendLine("| 最慢时间 | ${stats.maxTime} ms |")
                appendLine("| 标准差 | ${stats.standardDeviation.toInt()} ms |")
                appendLine("| 90分位数 | ${stats.percentile90} ms |")
                appendLine("| 95分位数 | ${stats.percentile95} ms |")
                appendLine("| 99分位数 | ${stats.percentile99} ms |")
                appendLine()
            }

            // 优化建议
            appendLine("## 💡 优化建议")
            appendLine()
            report.recommendations.forEach { recommendation ->
                appendLine(recommendation)
                appendLine()
            }

            appendLine("---")
            appendLine()
            appendLine("*报告生成时间: ${dateFormat.format(Date())}*")
        }
    }

    fun generateHtmlReport(report: LaunchTestReport): String {
        // HTML 报告生成逻辑（简化版）
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>App 启动性能测试报告</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #333; }
                    table { border-collapse: collapse; width: 100%; margin: 20px 0; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #4CAF50; color: white; }
                    .excellent { color: #4CAF50; }
                    .good { color: #8BC34A; }
                    .acceptable { color: #FFC107; }
                    .poor { color: #FF9800; }
                    .very-poor { color: #F44336; }
                </style>
            </head>
            <body>
                <h1>App 启动性能测试报告</h1>
                <!-- 报告内容 -->
            </body>
            </html>
        """.trimIndent()
    }
}