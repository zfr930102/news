package com.fr.test.news

import com.fr.test.news.result.LaunchType
import com.fr.test.news.result.PerformanceGrade
/**
 * 行业性能标准（基于 Google Android Vitals 和行业最佳实践）
 */
object PerformanceStandards {

    /**
     * 冷启动时间标准 (毫秒)
     * 参考：
     * - Google Android Vitals: < 5000ms
     * - 行业优秀标准: < 600ms
     * - 行业良好标准: < 800ms
     * - 行业可接受标准: < 1000ms
     */
    object ColdStart {
        const val EXCELLENT = 600L      // 优秀
        const val GOOD = 800L           // 良好
        const val ACCEPTABLE = 1000L    // 可接受
        const val POOR = 2000L          // 较差
        const val VERY_POOR = 5000L     // 很差（Google Vitals 阈值）
    }

    /**
     * 温启动时间标准 (毫秒)
     * 参考：
     * - Google Android Vitals: < 2000ms
     * - 行业优秀标准: < 200ms
     * - 行业良好标准: < 400ms
     * - 行业可接受标准: < 600ms
     */
    object WarmStart {
        const val EXCELLENT = 200L
        const val GOOD = 400L
        const val ACCEPTABLE = 600L
        const val POOR = 1000L
        const val VERY_POOR = 2000L     // Google Vitals 阈值
    }

    /**
     * 热启动时间标准 (毫秒)
     * 参考：
     * - Google Android Vitals: < 1500ms
     * - 行业优秀标准: < 100ms
     * - 行业良好标准: < 200ms
     * - 行业可接受标准: < 400ms
     */
    object HotStart {
        const val EXCELLENT = 100L
        const val GOOD = 200L
        const val ACCEPTABLE = 400L
        const val POOR = 800L
        const val VERY_POOR = 1500L     // Google Vitals 阈值
    }

    /**
     * 根据启动时间评估性能等级
     */
    fun evaluatePerformance(launchType: LaunchType, timeMs: Long): PerformanceGrade {
        return when (launchType) {
            LaunchType.COLD -> when {
                timeMs <= ColdStart.EXCELLENT -> PerformanceGrade.EXCELLENT
                timeMs <= ColdStart.GOOD -> PerformanceGrade.GOOD
                timeMs <= ColdStart.ACCEPTABLE -> PerformanceGrade.ACCEPTABLE
                timeMs <= ColdStart.POOR -> PerformanceGrade.POOR
                else -> PerformanceGrade.VERY_POOR
            }
            LaunchType.WARM -> when {
                timeMs <= WarmStart.EXCELLENT -> PerformanceGrade.EXCELLENT
                timeMs <= WarmStart.GOOD -> PerformanceGrade.GOOD
                timeMs <= WarmStart.ACCEPTABLE -> PerformanceGrade.ACCEPTABLE
                timeMs <= WarmStart.POOR -> PerformanceGrade.POOR
                else -> PerformanceGrade.VERY_POOR
            }
            LaunchType.HOT -> when {
                timeMs <= HotStart.EXCELLENT -> PerformanceGrade.EXCELLENT
                timeMs <= HotStart.GOOD -> PerformanceGrade.GOOD
                timeMs <= HotStart.ACCEPTABLE -> PerformanceGrade.ACCEPTABLE
                timeMs <= HotStart.POOR -> PerformanceGrade.POOR
                else -> PerformanceGrade.VERY_POOR
            }
        }
    }

    /**
     * 获取性能标准说明
     */
    fun getStandardDescription(launchType: LaunchType): String {
        return when (launchType) {
            LaunchType.COLD -> """
                冷启动性能标准：
                🌟 优秀：≤ ${ColdStart.EXCELLENT}ms
                ✅ 良好：≤ ${ColdStart.GOOD}ms
                ⚠️ 可接受：≤ ${ColdStart.ACCEPTABLE}ms
                ❌ 较差：≤ ${ColdStart.POOR}ms
                💔 很差：> ${ColdStart.POOR}ms
                
                Google Android Vitals 阈值：${ColdStart.VERY_POOR}ms
            """.trimIndent()

            LaunchType.WARM -> """
                温启动性能标准：
                🌟 优秀：≤ ${WarmStart.EXCELLENT}ms
                ✅ 良好：≤ ${WarmStart.GOOD}ms
                ⚠️ 可接受：≤ ${WarmStart.ACCEPTABLE}ms
                ❌ 较差：≤ ${WarmStart.POOR}ms
                💔 很差：> ${WarmStart.POOR}ms
                
                Google Android Vitals 阈值：${WarmStart.VERY_POOR}ms
            """.trimIndent()

            LaunchType.HOT -> """
                热启动性能标准：
                🌟 优秀：≤ ${HotStart.EXCELLENT}ms
                ✅ 良好：≤ ${HotStart.GOOD}ms
                ⚠️ 可接受：≤ ${HotStart.ACCEPTABLE}ms
                ❌ 较差：≤ ${HotStart.POOR}ms
                💔 很差：> ${HotStart.POOR}ms
                
                Google Android Vitals 阈值：${HotStart.VERY_POOR}ms
            """.trimIndent()
        }
    }

    /**
     * 生成优化建议
     */
    fun generateRecommendations(
        coldGrade: PerformanceGrade,
        warmGrade: PerformanceGrade,
        hotGrade: PerformanceGrade,
        coldAvgTime: Double,
        warmAvgTime: Double,
        hotAvgTime: Double
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // 冷启动优化建议
        when (coldGrade) {
            PerformanceGrade.EXCELLENT -> {
                recommendations.add("✅ 冷启动性能优秀（${coldAvgTime.toInt()}ms），继续保持！")
            }
            PerformanceGrade.GOOD -> {
                recommendations.add("✅ 冷启动性能良好（${coldAvgTime.toInt()}ms），可以尝试进一步优化至 ${ColdStart.EXCELLENT}ms 以内")
            }
            else -> {
                recommendations.add("❌ 冷启动性能需要优化（当前：${coldAvgTime.toInt()}ms）")
                recommendations.addAll(getColdStartOptimizationTips())
            }
        }

        // 温启动优化建议
        when (warmGrade) {
            PerformanceGrade.EXCELLENT -> {
                recommendations.add("✅ 温启动性能优秀（${warmAvgTime.toInt()}ms），继续保持！")
            }
            PerformanceGrade.GOOD -> {
                recommendations.add("✅ 温启动性能良好（${warmAvgTime.toInt()}ms），可以尝试进一步优化")
            }
            else -> {
                recommendations.add("❌ 温启动性能需要优化（当前：${warmAvgTime.toInt()}ms）")
                recommendations.addAll(getWarmStartOptimizationTips())
            }
        }

        // 热启动优化建议
        when (hotGrade) {
            PerformanceGrade.EXCELLENT -> {
                recommendations.add("✅ 热启动性能优秀（${hotAvgTime.toInt()}ms），继续保持！")
            }
            PerformanceGrade.GOOD -> {
                recommendations.add("✅ 热启动性能良好（${hotAvgTime.toInt()}ms），可以尝试进一步优化")
            }
            else -> {
                recommendations.add("❌ 热启动性能需要优化（当前：${hotAvgTime.toInt()}ms）")
                recommendations.addAll(getHotStartOptimizationTips())
            }
        }

        return recommendations
    }

    private fun getColdStartOptimizationTips(): List<String> {
        return listOf(
            "【Application.onCreate() 优化】",
            "  • 延迟初始化非必要的第三方 SDK",
            "  • 将耗时操作移至后台线程",
            "  • 使用 App Startup Library 统一管理初始化",
            "",
            "【Activity 启动优化】",
            "  • 减少 Activity.onCreate() 中的工作量",
            "  • 延迟加载非首屏必需的视图",
            "  • 使用 ViewStub 延迟加载复杂布局",
            "",
            "【布局优化】",
            "  • 减少布局层级，使用 ConstraintLayout",
            "  • 避免过度绘制",
            "  • 使用 <merge> 标签减少布局嵌套",
            "",
            "【资源加载优化】",
            "  • 异步加载图片和大型资源",
            "  • 压缩图片资源",
            "  • 使用 WebP 格式替代 PNG/JPG",
            "",
            "【代码优化】",
            "  • 使用 Baseline Profiles（可提升 30% 性能）",
            "  • 启用 R8 代码压缩和优化",
            "  • 减少反射和注解处理器的使用",
            "",
            "【启动画面优化】",
            "  • 使用 Android 12+ SplashScreen API",
            "  • 避免在启动画面做耗时操作"
        )
    }

    private fun getWarmStartOptimizationTips(): List<String> {
        return listOf(
            "【Activity 恢复优化】",
            "  • 优化 onStart() 和 onResume() 中的逻辑",
            "  • 避免在生命周期回调中做耗时操作",
            "",
            "【状态恢复优化】",
            "  • 使用 ViewModel 保存 UI 状态",
            "  • 避免在 onRestoreInstanceState() 中做重复工作",
            "",
            "【内存管理】",
            "  • 合理使用缓存，避免频繁重建对象",
            "  • 注意内存泄漏，防止 Activity 被系统回收"
        )
    }

    private fun getHotStartOptimizationTips(): List<String> {
        return listOf(
            "【Activity 切换优化】",
            "  • 优化 onResume() 回调",
            "  • 减少 UI 刷新操作",
            "",
            "【后台任务管理】",
            "  • 合理管理后台任务，避免阻塞主线程",
            "  • 使用 WorkManager 管理后台工作"
        )
    }
}