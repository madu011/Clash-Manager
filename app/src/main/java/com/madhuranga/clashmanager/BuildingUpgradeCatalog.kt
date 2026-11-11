package com.madhuranga.clashmanager

/**
 * Verified Town Hall unlock requirements for a small starter set.
 * Source: chiefpansancolt/clash-of-clans-data, data/home/{defenses}/ JSON.
 * This is intentionally partial: unknown buildings return null, never an estimate.
 */
object BuildingUpgradeCatalog {
    data class LevelRequirement(
        val level: Int,
        val minimumTownHallLevel: Int
    )

    data class Building(
        val dataId: Long,
        val name: String,
        val levels: List<LevelRequirement>
    ) {
        fun maximumLevelAt(townHallLevel: Int): Int? =
            levels.filter { it.minimumTownHallLevel <= townHallLevel }
                .maxOfOrNull { it.level }
    }

    private val buildings = listOf(
        Building(
            dataId = 1000008L,
            name = "Cannon",
            levels = listOf(
                LevelRequirement(1, 1), LevelRequirement(2, 1),
                LevelRequirement(3, 2), LevelRequirement(4, 3),
                LevelRequirement(5, 4), LevelRequirement(6, 5),
                LevelRequirement(7, 6), LevelRequirement(8, 7),
                LevelRequirement(9, 8), LevelRequirement(10, 8),
                LevelRequirement(11, 9), LevelRequirement(12, 10),
                LevelRequirement(13, 10), LevelRequirement(14, 11),
                LevelRequirement(15, 11), LevelRequirement(16, 12),
                LevelRequirement(17, 12), LevelRequirement(18, 13),
                LevelRequirement(19, 13), LevelRequirement(20, 14),
                LevelRequirement(21, 15)
            )
        ),
        Building(
            dataId = 1000009L,
            name = "Archer Tower",
            levels = listOf(
                LevelRequirement(1, 2), LevelRequirement(2, 2),
                LevelRequirement(3, 3), LevelRequirement(4, 4),
                LevelRequirement(5, 5), LevelRequirement(6, 5),
                LevelRequirement(7, 6), LevelRequirement(8, 7),
                LevelRequirement(9, 8), LevelRequirement(10, 8),
                LevelRequirement(11, 9), LevelRequirement(12, 10),
                LevelRequirement(13, 10), LevelRequirement(14, 11),
                LevelRequirement(15, 11), LevelRequirement(16, 12),
                LevelRequirement(17, 12), LevelRequirement(18, 13),
                LevelRequirement(19, 13), LevelRequirement(20, 14),
                LevelRequirement(21, 15)
            )
        )
    )

    fun maximumLevelAt(dataId: Long, townHallLevel: Int): Int? {
        if (townHallLevel !in 1..18) return null
        return buildings.firstOrNull { it.dataId == dataId }
            ?.maximumLevelAt(townHallLevel)
    }

    fun buildingName(dataId: Long): String? =
        buildings.firstOrNull { it.dataId == dataId }?.name
}
