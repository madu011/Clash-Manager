package com.madhuranga.clashmanager

object RecoveryAnalytics {
    private const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000

    data class Week(
        val startMillis: Long,
        val completedLevels: Int,
        val upgradeEvents: Int
    )

    data class Summary(
        val totalUpgradeEvents: Int,
        val totalLevelsGained: Int,
        val buildingsImproved: Int,
        val weekly: List<Week>,
        val averageLevelsPerWeek: Double
    )

    fun summarize(
        history: List<RecoveryHistoryEntry>,
        nowMillis: Long,
        weekCount: Int = 8
    ): Summary {
        val safeWeeks = weekCount.coerceIn(1, 52)
        val valid = history.filter {
            it.buildingName.isNotBlank() && it.fromLevel >= 0 &&
                it.toLevel > it.fromLevel && it.recordedAtMillis >= 0 &&
                it.recordedAtMillis <= nowMillis
        }
        val currentWeekStart = Math.floorDiv(nowMillis, WEEK_MILLIS) * WEEK_MILLIS
        val firstWeekStart = currentWeekStart - (safeWeeks - 1) * WEEK_MILLIS
        val buckets = (0 until safeWeeks).map { index ->
            val start = firstWeekStart + index * WEEK_MILLIS
            val entries = valid.filter {
                val week = Math.floorDiv(it.recordedAtMillis, WEEK_MILLIS) * WEEK_MILLIS
                week == start
            }
            Week(
                startMillis = start,
                completedLevels = entries.sumOf { it.toLevel - it.fromLevel },
                upgradeEvents = entries.size
            )
        }
        val recentLevels = buckets.sumOf { it.completedLevels }
        val activeWeeks = buckets.count { it.completedLevels > 0 }
        return Summary(
            totalUpgradeEvents = valid.size,
            totalLevelsGained = valid.sumOf { it.toLevel - it.fromLevel },
            buildingsImproved = valid.map { it.buildingName.lowercase() }.distinct().size,
            weekly = buckets,
            averageLevelsPerWeek = if (activeWeeks == 0) 0.0 else recentLevels.toDouble() / safeWeeks
        )
    }
}
