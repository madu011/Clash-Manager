package com.madhuranga.clashmanager

/**
 * Offline-first village planning models.
 * Values are entered by each player; no account is hard-coded.
 */
data class VillageSnapshot(
    val playerTag: String,
    val townHallLevel: Int,
    val heroLevels: Map<String, Int> = emptyMap(),
    val heroTargetLevels: Map<String, Int> = emptyMap(),
    val troopLevels: Map<String, Int> = emptyMap(),
    val troopTargetLevels: Map<String, Int> = emptyMap(),
    val buildingLevels: Map<String, Int> = emptyMap(),
    val buildingTargetLevels: Map<String, Int> = emptyMap(),
    val rushedBuildings: List<String> = emptyList(),
    val availableBuilders: Int = 0
)

data class UpgradeRecommendation(
    val title: String,
    val reason: String,
    val priority: Int,
    val category: String
)

object VillageAnalyzer {
    fun analyze(village: VillageSnapshot): List<UpgradeRecommendation> {
        val th = village.townHallLevel.coerceIn(1, 18)
        val result = mutableListOf<UpgradeRecommendation>()

        if (village.availableBuilders > 0) {
            result += UpgradeRecommendation(
                title = "Keep builders active",
                reason = "${village.availableBuilders} builder slot(s) available. Assign a useful upgrade to avoid idle time.",
                priority = 100,
                category = "Builder management"
            )
        }

        if (village.rushedBuildings.isNotEmpty()) {
            village.rushedBuildings.distinct().take(8).forEachIndexed { index, building ->
                result += UpgradeRecommendation(
                    title = "Upgrade $building",
                    reason = "This building was marked as behind your current Town Hall level.",
                    priority = 95 - index,
                    category = "Rush recovery"
                )
            }
        }

        val keyOffence = listOf("Laboratory", "Clan Castle", "Army Camps", "Spell Factory")
        keyOffence.forEachIndexed { index, name ->
            result += UpgradeRecommendation(
                title = "Review $name",
                reason = "Prioritise core offensive capacity so farming and attacks remain effective.",
                priority = 90 - index * 3,
                category = "Offence"
            )
        }

        village.buildingLevels.forEach { (building, level) ->
            val target = village.buildingTargetLevels[building] ?: return@forEach
            if (level < target) result += UpgradeRecommendation(
                title = "Plan $building upgrade",
                reason = "Current level $level; your selected target is $target.",
                priority = 78,
                category = "Buildings"
            )
        }

        village.heroLevels.forEach { (hero, level) ->
            val target = village.heroTargetLevels[hero] ?: return@forEach
            if (level < target) result += UpgradeRecommendation(
                title = "Plan $hero upgrade",
                reason = "Current level $level; your selected target is $target.",
                priority = 75,
                category = "Heroes"
            )
        }
        village.troopLevels.forEach { (troop, level) ->
            val target = village.troopTargetLevels[troop] ?: return@forEach
            if (level < target) result += UpgradeRecommendation(
                title = "Plan $troop research",
                reason = "Current level $level; your selected target is $target.",
                priority = 72,
                category = "Laboratory"
            )
        }

        result += UpgradeRecommendation(
            title = "Balance resource spending",
            reason = "Avoid starting more upgrades than your builders and available resources can support.",
            priority = 60,
            category = "Resources"
        )

        return result.sortedByDescending { it.priority }
    }
}
