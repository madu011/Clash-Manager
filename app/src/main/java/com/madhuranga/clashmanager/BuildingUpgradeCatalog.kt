package com.madhuranga.clashmanager

/**
 * Verified Town Hall unlock and next-upgrade data for a small starter set.
 * Source: chiefpansancolt/clash-of-clans-data, data/home/defenses JSON.
 * Intentionally partial: unknown buildings/levels return null, never an estimate.
 */
object BuildingUpgradeCatalog {
    data class LevelRequirement(
        val level: Int,
        val minimumTownHallLevel: Int
    )

    data class UpgradeStep(
        val targetLevel: Int,
        val cost: Long,
        val resource: String,
        val durationSeconds: Long
    )

    data class Building(
        val dataId: Long,
        val name: String,
        val levels: List<LevelRequirement>,
        val upgrades: List<UpgradeStep>
    ) {
        fun maximumLevelAt(townHallLevel: Int): Int? =
            levels.filter { it.minimumTownHallLevel <= townHallLevel }
                .maxOfOrNull { it.level }

        /** Returns the verified cost/time to upgrade from currentLevel to currentLevel + 1. */
        fun nextUpgrade(currentLevel: Int): UpgradeStep? =
            upgrades.firstOrNull { it.targetLevel == currentLevel + 1 }
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
            ),
            upgrades = listOf(
                UpgradeStep(2, 1_000L, "Gold", 30L),
                UpgradeStep(3, 4_000L, "Gold", 120L),
                UpgradeStep(4, 16_000L, "Gold", 1_200L),
                UpgradeStep(5, 50_000L, "Gold", 1_800L),
                UpgradeStep(6, 60_000L, "Gold", 3_600L),
                UpgradeStep(7, 100_000L, "Gold", 7_200L),
                UpgradeStep(8, 160_000L, "Gold", 10_800L),
                UpgradeStep(9, 250_000L, "Gold", 12_600L),
                UpgradeStep(10, 330_000L, "Gold", 14_400L),
                UpgradeStep(11, 500_000L, "Gold", 16_200L),
                UpgradeStep(12, 600_000L, "Gold", 18_000L),
                UpgradeStep(13, 660_000L, "Gold", 21_600L),
                UpgradeStep(14, 1_000_000L, "Gold", 28_800L),
                UpgradeStep(15, 1_200_000L, "Gold", 36_000L),
                UpgradeStep(16, 1_300_000L, "Gold", 39_600L),
                UpgradeStep(17, 1_500_000L, "Gold", 43_200L),
                UpgradeStep(18, 1_800_000L, "Gold", 57_600L),
                UpgradeStep(19, 2_000_000L, "Gold", 72_000L),
                UpgradeStep(20, 2_600_000L, "Gold", 86_400L),
                UpgradeStep(21, 3_000_000L, "Gold", 129_600L)
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
            ),
            upgrades = listOf(
                UpgradeStep(2, 2_000L, "Gold", 120L),
                UpgradeStep(3, 5_000L, "Gold", 1_200L),
                UpgradeStep(4, 20_000L, "Gold", 3_600L),
                UpgradeStep(5, 70_000L, "Gold", 5_400L),
                UpgradeStep(6, 80_000L, "Gold", 7_200L),
                UpgradeStep(7, 150_000L, "Gold", 10_800L),
                UpgradeStep(8, 200_000L, "Gold", 14_400L),
                UpgradeStep(9, 400_000L, "Gold", 18_000L),
                UpgradeStep(10, 460_000L, "Gold", 21_600L),
                UpgradeStep(11, 600_000L, "Gold", 25_200L),
                UpgradeStep(12, 700_000L, "Gold", 28_800L),
                UpgradeStep(13, 1_000_000L, "Gold", 36_000L),
                UpgradeStep(14, 1_100_000L, "Gold", 39_600L),
                UpgradeStep(15, 1_300_000L, "Gold", 43_200L),
                UpgradeStep(16, 1_600_000L, "Gold", 57_600L),
                UpgradeStep(17, 1_800_000L, "Gold", 72_000L),
                UpgradeStep(18, 2_000_000L, "Gold", 86_400L),
                UpgradeStep(19, 2_200_000L, "Gold", 108_000L),
                UpgradeStep(20, 3_000_000L, "Gold", 129_600L),
                UpgradeStep(21, 4_000_000L, "Gold", 172_800L)
            )
        ),
        Building(
            dataId = 1000013L,
            name = "Mortar",
            levels = listOf(
                LevelRequirement(1, 3), LevelRequirement(2, 4),
                LevelRequirement(3, 5), LevelRequirement(4, 6),
                LevelRequirement(5, 7), LevelRequirement(6, 8),
                LevelRequirement(7, 9), LevelRequirement(8, 10),
                LevelRequirement(9, 11), LevelRequirement(10, 11),
                LevelRequirement(11, 12), LevelRequirement(12, 12),
                LevelRequirement(13, 13), LevelRequirement(14, 14),
                LevelRequirement(15, 15), LevelRequirement(16, 16),
                LevelRequirement(17, 17), LevelRequirement(18, 18)
            ),
            upgrades = listOf(
                UpgradeStep(2, 25_000L, "Gold", 3_600L),
                UpgradeStep(3, 90_000L, "Gold", 7_200L),
                UpgradeStep(4, 180_000L, "Gold", 10_800L),
                UpgradeStep(5, 300_000L, "Gold", 21_600L),
                UpgradeStep(6, 500_000L, "Gold", 28_800L),
                UpgradeStep(7, 900_000L, "Gold", 43_200L),
                UpgradeStep(8, 1_200_000L, "Gold", 64_800L),
                UpgradeStep(9, 1_600_000L, "Gold", 72_000L),
                UpgradeStep(10, 1_800_000L, "Gold", 86_400L),
                UpgradeStep(11, 2_300_000L, "Gold", 108_000L),
                UpgradeStep(12, 2_400_000L, "Gold", 129_600L),
                UpgradeStep(13, 2_800_000L, "Gold", 172_800L),
                UpgradeStep(14, 4_300_000L, "Gold", 216_000L),
                UpgradeStep(15, 5_000_000L, "Gold", 259_200L),
                UpgradeStep(16, 7_000_000L, "Gold", 345_600L),
                UpgradeStep(17, 13_000_000L, "Gold", 432_000L),
                UpgradeStep(18, 21_000_000L, "Gold", 1_080_000L)
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

    fun maximumLevelAt(buildingName: String, townHallLevel: Int): Int? {
        if (townHallLevel !in 1..18) return null
        return buildings.firstOrNull { it.name.equals(buildingName.trim(), ignoreCase = true) }
            ?.maximumLevelAt(townHallLevel)
    }

    fun nextUpgrade(dataId: Long, currentLevel: Int): UpgradeStep? =
        buildings.firstOrNull { it.dataId == dataId }?.nextUpgrade(currentLevel)

    fun nextUpgrade(buildingName: String, currentLevel: Int): UpgradeStep? =
        buildings.firstOrNull { it.name.equals(buildingName.trim(), ignoreCase = true) }
            ?.nextUpgrade(currentLevel)
}
