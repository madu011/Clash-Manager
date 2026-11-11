package com.madhuranga.clashmanager

/**
 * Creates a recovery checklist only from building levels and targets explicitly supplied by the player.
 * It does not infer rush status or recommend unverified target levels.
 */
object RushRecoveryPlanner {
    data class Task(
        val buildingName: String,
        val currentLevel: Int,
        val targetLevel: Int,
        val remainingLevels: Int,
        val priority: Int
    )

    fun create(village: VillageSnapshot): List<Task> {
        if (village.townHallLevel !in 1..18) return emptyList()
        return village.buildingTargetLevels.mapNotNull { (name, target) ->
            val current = village.buildingLevels[name] ?: return@mapNotNull null
            if (name.isBlank() || current < 0 || target <= current || target > 100) return@mapNotNull null
            val gap = target - current
            Task(
                buildingName = name,
                currentLevel = current,
                targetLevel = target,
                remainingLevels = gap,
                priority = 70 + gap.coerceAtMost(10)
            )
        }.sortedWith(compareByDescending<Task> { it.priority }.thenBy { it.buildingName.lowercase() })
    }
}
