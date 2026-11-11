package com.madhuranga.clashmanager

/**
 * Calculates recovery progress from explicitly entered starting, current and target levels.
 * Unknown or out-of-range progress is rejected rather than guessed.
 */
object RecoveryProgressPlanner {
    data class BuildingProgress(
        val buildingName: String,
        val startingLevel: Int,
        val currentLevel: Int,
        val targetLevel: Int,
        val completedLevels: Int,
        val remainingLevels: Int,
        val percentComplete: Int
    )

    data class Summary(
        val buildings: List<BuildingProgress>,
        val completedLevels: Int,
        val totalLevels: Int,
        val percentComplete: Int
    )

    fun calculate(
        targets: List<BuilderSchedulePlanner.Target>,
        currentLevels: Map<String, Int>
    ): Summary? {
        if (targets.isEmpty() || targets.size > 12) return null
        if (targets.map { it.buildingName.trim().lowercase() }.distinct().size != targets.size) return null
        val progress = targets.map { target ->
            val name = target.buildingName.trim()
            val current = currentLevels.entries.firstOrNull {
                it.key.trim().equals(name, ignoreCase = true)
            }?.value ?: target.currentLevel
            if (name.isBlank() || target.currentLevel < 0 ||
                target.targetLevel <= target.currentLevel || current !in target.currentLevel..target.targetLevel
            ) return null
            val total = target.targetLevel - target.currentLevel
            val completed = current - target.currentLevel
            BuildingProgress(
                buildingName = name,
                startingLevel = target.currentLevel,
                currentLevel = current,
                targetLevel = target.targetLevel,
                completedLevels = completed,
                remainingLevels = target.targetLevel - current,
                percentComplete = (completed * 100) / total
            )
        }
        val completed = progress.sumOf { it.completedLevels }
        val total = progress.sumOf { it.targetLevel - it.startingLevel }
        return Summary(
            buildings = progress,
            completedLevels = completed,
            totalLevels = total,
            percentComplete = if (total == 0) 100 else (completed * 100) / total
        )
    }
}
