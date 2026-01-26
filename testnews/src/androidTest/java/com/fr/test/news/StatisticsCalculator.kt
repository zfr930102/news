package com.fr.test.news

import com.fr.test.news.result.*
import kotlin.math.pow
import kotlin.math.sqrt

object StatisticsCalculator {

    fun calculate(launchType: LaunchType, results: List<LaunchResult>): LaunchStatistics {
        val successResults = results.filter { it.success && it.totalTime > 0 }
        val times = successResults.map { it.totalTime }

        if (times.isEmpty()) {
            return LaunchStatistics(
                launchType = launchType,
                testCount = results.size,
                successCount = 0,
                failureCount = results.size,
                averageTime = 0.0,
                medianTime = 0,
                minTime = 0,
                maxTime = 0,
                standardDeviation = 0.0,
                percentile90 = 0,
                percentile95 = 0,
                percentile99 = 0
            )
        }

        val sortedTimes = times.sorted()

        return LaunchStatistics(
            launchType = launchType,
            testCount = results.size,
            successCount = successResults.size,
            failureCount = results.size - successResults.size,
            averageTime = times.average(),
            medianTime = calculateMedian(sortedTimes),
            minTime = sortedTimes.first(),
            maxTime = sortedTimes.last(),
            standardDeviation = calculateStandardDeviation(times),
            percentile90 = calculatePercentile(sortedTimes, 90),
            percentile95 = calculatePercentile(sortedTimes, 95),
            percentile99 = calculatePercentile(sortedTimes, 99)
        )
    }

    private fun calculateMedian(sortedList: List<Long>): Long {
        val size = sortedList.size
        return if (size % 2 == 0) {
            (sortedList[size / 2 - 1] + sortedList[size / 2]) / 2
        } else {
            sortedList[size / 2]
        }
    }

    private fun calculateStandardDeviation(values: List<Long>): Double {
        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }

    private fun calculatePercentile(sortedList: List<Long>, percentile: Int): Long {
        val index = (sortedList.size * percentile / 100.0).toInt()
        return sortedList[index.coerceIn(0, sortedList.size - 1)]
    }
}
