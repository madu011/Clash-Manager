package com.madhuranga.clashmanager

/**
 * Builds an illustrative schedule from verified upgrade durations.
 * Each building's levels remain sequential; different buildings may run in parallel.
 * It does not model resource availability, boosts, builder potions, or game-specific restrictions.
 */
object BuilderSchedulePlanner {
    data class Target(val buildingName: String, val currentLevel: Int, val targetLevel: Int)
    data class ScheduledUpgrade(
        val buildingName: String,
        val targetLevel: Int,
        val builderNumber: Int,
        val startAfterSeconds: Long,
        val finishAfterSeconds: Long,
        val cost: Long,
        val resource: String
    )
    data class Result(
        val upgrades: List<ScheduledUpgrade>,
        val elapsedSeconds: Long,
        val totalBuilderSeconds: Long,
        val totalCosts: Map<String, Long>
    )

    fun create(targets: List<Target>, builderCount: Int): Result? {
        if (builderCount !in 1..6 || targets.isEmpty()) return null
        if (targets.any {
                it.buildingName.isBlank() || it.currentLevel < 0 ||
                    it.targetLevel <= it.currentLevel || it.targetLevel > 100
            }) return null
        if (targets.map { it.buildingName.lowercase() }.distinct().size != targets.size) return null

        val builderReadyAt = LongArray(builderCount)
        val scheduled = mutableListOf<ScheduledUpgrade>()
        for (target in targets) {
            var buildingReadyAt = 0L
            for (level in (target.currentLevel + 1)..target.targetLevel) {
                val step = BuildingUpgradeCatalog.nextUpgrade(target.buildingName, level - 1) ?: return null
                val builderIndex = builderReadyAt.indices.minByOrNull { builderReadyAt[it] } ?: return null
                val start = maxOf(builderReadyAt[builderIndex], buildingReadyAt)
                val finish = start + step.durationSeconds
                if (finish < start) return null
                builderReadyAt[builderIndex] = finish
                buildingReadyAt = finish
                scheduled += ScheduledUpgrade(
                    buildingName = target.buildingName,
                    targetLevel = step.targetLevel,
                    builderNumber = builderIndex + 1,
                    startAfterSeconds = start,
                    finishAfterSeconds = finish,
                    cost = step.cost,
                    resource = step.resource
                )
            }
        }
        val costs = scheduled.groupBy { it.resource }
            .mapValues { (_, items) -> items.sumOf { it.cost } }
        return Result(
            upgrades = scheduled.sortedWith(compareBy<ScheduledUpgrade> { it.startAfterSeconds }.thenBy { it.builderNumber }),
            elapsedSeconds = builderReadyAt.maxOrNull() ?: 0L,
            totalBuilderSeconds = scheduled.sumOf { it.finishAfterSeconds - it.startAfterSeconds },
            totalCosts = costs
        )
    }
}
