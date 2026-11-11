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
        ),
        Building(
            dataId = 1000011L,
            name = "Wizard Tower",
            levels = listOf(
                LevelRequirement(1, 5), LevelRequirement(2, 5),
                LevelRequirement(3, 6), LevelRequirement(4, 7),
                LevelRequirement(5, 8), LevelRequirement(6, 8),
                LevelRequirement(7, 9), LevelRequirement(8, 10),
                LevelRequirement(9, 10), LevelRequirement(10, 11),
                LevelRequirement(11, 12), LevelRequirement(12, 13),
                LevelRequirement(13, 13), LevelRequirement(14, 14),
                LevelRequirement(15, 15), LevelRequirement(16, 16),
                LevelRequirement(17, 17)
            ),
            upgrades = listOf(
                UpgradeStep(2, 150_000L, "Gold", 5_400L),
                UpgradeStep(3, 250_000L, "Gold", 14_400L),
                UpgradeStep(4, 400_000L, "Gold", 28_800L),
                UpgradeStep(5, 550_000L, "Gold", 36_000L),
                UpgradeStep(6, 660_000L, "Gold", 43_200L),
                UpgradeStep(7, 1_000_000L, "Gold", 64_800L),
                UpgradeStep(8, 1_100_000L, "Gold", 72_000L),
                UpgradeStep(9, 1_300_000L, "Gold", 86_400L),
                UpgradeStep(10, 2_000_000L, "Gold", 108_000L),
                UpgradeStep(11, 2_500_000L, "Gold", 129_600L),
                UpgradeStep(12, 2_600_000L, "Gold", 151_200L),
                UpgradeStep(13, 3_000_000L, "Gold", 172_800L),
                UpgradeStep(14, 4_500_000L, "Gold", 259_200L),
                UpgradeStep(15, 5_500_000L, "Gold", 345_600L),
                UpgradeStep(16, 8_000_000L, "Gold", 388_800L),
                UpgradeStep(17, 14_000_000L, "Gold", 475_200L)
            )
        ),
        Building(
            dataId = 1000012L,
            name = "Air Defense",
            levels = listOf(
                LevelRequirement(1, 4),
                LevelRequirement(2, 4),
                LevelRequirement(3, 5),
                LevelRequirement(4, 6),
                LevelRequirement(5, 7),
                LevelRequirement(6, 8),
                LevelRequirement(7, 9),
                LevelRequirement(8, 10),
                LevelRequirement(9, 11),
                LevelRequirement(10, 12),
                LevelRequirement(11, 13),
                LevelRequirement(12, 14),
                LevelRequirement(13, 15),
                LevelRequirement(14, 16),
                LevelRequirement(15, 17),
                LevelRequirement(16, 18)
            ),
            upgrades = listOf(
                UpgradeStep(2, 90000L, "Gold", 7200L),
                UpgradeStep(3, 210000L, "Gold", 21600L),
                UpgradeStep(4, 500000L, "Gold", 43200L),
                UpgradeStep(5, 800000L, "Gold", 64800L),
                UpgradeStep(6, 1000000L, "Gold", 86400L),
                UpgradeStep(7, 1750000L, "Gold", 172800L),
                UpgradeStep(8, 2300000L, "Gold", 216000L),
                UpgradeStep(9, 3400000L, "Gold", 259200L),
                UpgradeStep(10, 5000000L, "Gold", 345600L),
                UpgradeStep(11, 5600000L, "Gold", 388800L),
                UpgradeStep(12, 6500000L, "Gold", 432000L),
                UpgradeStep(13, 8000000L, "Gold", 518400L),
                UpgradeStep(14, 9000000L, "Gold", 561600L),
                UpgradeStep(15, 15000000L, "Gold", 604800L),
                UpgradeStep(16, 26000000L, "Gold", 1144800L)
            )
        ),
        Building(
            dataId = 1000028L,
            name = "Air Sweeper",
            levels = listOf(
                LevelRequirement(1, 6),
                LevelRequirement(2, 6),
                LevelRequirement(3, 7),
                LevelRequirement(4, 8),
                LevelRequirement(5, 9),
                LevelRequirement(6, 10),
                LevelRequirement(7, 11)
            ),
            upgrades = listOf(
                UpgradeStep(2, 300000L, "Gold", 21600L),
                UpgradeStep(3, 450000L, "Gold", 28800L),
                UpgradeStep(4, 800000L, "Gold", 43200L),
                UpgradeStep(5, 1200000L, "Gold", 86400L),
                UpgradeStep(6, 1900000L, "Gold", 172800L),
                UpgradeStep(7, 3400000L, "Gold", 259200L)
            )
        ),
        Building(
            dataId = 1000032L,
            name = "Bomb Tower",
            levels = listOf(
                LevelRequirement(1, 8),
                LevelRequirement(2, 8),
                LevelRequirement(3, 9),
                LevelRequirement(4, 10),
                LevelRequirement(5, 11),
                LevelRequirement(6, 11),
                LevelRequirement(7, 12),
                LevelRequirement(8, 13),
                LevelRequirement(9, 14),
                LevelRequirement(10, 15),
                LevelRequirement(11, 16),
                LevelRequirement(12, 17),
                LevelRequirement(13, 18)
            ),
            upgrades = listOf(
                UpgradeStep(2, 1000000L, "Gold", 64800L),
                UpgradeStep(3, 1300000L, "Gold", 86400L),
                UpgradeStep(4, 1800000L, "Gold", 129600L),
                UpgradeStep(5, 1900000L, "Gold", 151200L),
                UpgradeStep(6, 2000000L, "Gold", 172800L),
                UpgradeStep(7, 4000000L, "Gold", 259200L),
                UpgradeStep(8, 5000000L, "Gold", 302400L),
                UpgradeStep(9, 6000000L, "Gold", 345600L),
                UpgradeStep(10, 7000000L, "Gold", 388800L),
                UpgradeStep(11, 8500000L, "Gold", 432000L),
                UpgradeStep(12, 14500000L, "Gold", 604800L),
                UpgradeStep(13, 25000000L, "Gold", 1123200L)
            )
        ),
        Building(
            dataId = 1000019L,
            name = "Hidden Tesla",
            levels = listOf(
                LevelRequirement(1, 7),
                LevelRequirement(2, 7),
                LevelRequirement(3, 7),
                LevelRequirement(4, 8),
                LevelRequirement(5, 8),
                LevelRequirement(6, 8),
                LevelRequirement(7, 9),
                LevelRequirement(8, 10),
                LevelRequirement(9, 11),
                LevelRequirement(10, 12),
                LevelRequirement(11, 13),
                LevelRequirement(12, 13),
                LevelRequirement(13, 14),
                LevelRequirement(14, 15),
                LevelRequirement(15, 16),
                LevelRequirement(16, 17),
                LevelRequirement(17, 18)
            ),
            upgrades = listOf(
                UpgradeStep(2, 350000L, "Gold", 10800L),
                UpgradeStep(3, 500000L, "Gold", 14400L),
                UpgradeStep(4, 600000L, "Gold", 21600L),
                UpgradeStep(5, 800000L, "Gold", 43200L),
                UpgradeStep(6, 1200000L, "Gold", 86400L),
                UpgradeStep(7, 1400000L, "Gold", 108000L),
                UpgradeStep(8, 1600000L, "Gold", 129600L),
                UpgradeStep(9, 2100000L, "Gold", 151200L),
                UpgradeStep(10, 3000000L, "Gold", 172800L),
                UpgradeStep(11, 3100000L, "Gold", 216000L),
                UpgradeStep(12, 3700000L, "Gold", 259200L),
                UpgradeStep(13, 5100000L, "Gold", 302400L),
                UpgradeStep(14, 6500000L, "Gold", 345600L),
                UpgradeStep(15, 8200000L, "Gold", 432000L),
                UpgradeStep(16, 15000000L, "Gold", 604800L),
                UpgradeStep(17, 25000000L, "Gold", 1123200L)
            )
        ),
        Building(
            dataId = 1000027L,
            name = "Inferno Tower",
            levels = listOf(
                LevelRequirement(1, 10),
                LevelRequirement(2, 10),
                LevelRequirement(3, 10),
                LevelRequirement(4, 11),
                LevelRequirement(5, 11),
                LevelRequirement(6, 12),
                LevelRequirement(7, 13),
                LevelRequirement(8, 14),
                LevelRequirement(9, 15),
                LevelRequirement(10, 16),
                LevelRequirement(11, 17),
                LevelRequirement(12, 18)
            ),
            upgrades = listOf(
                UpgradeStep(2, 1200000L, "Gold", 86400L),
                UpgradeStep(3, 2400000L, "Gold", 172800L),
                UpgradeStep(4, 3400000L, "Gold", 216000L),
                UpgradeStep(5, 4200000L, "Gold", 259200L),
                UpgradeStep(6, 6000000L, "Gold", 345600L),
                UpgradeStep(7, 8000000L, "Gold", 432000L),
                UpgradeStep(8, 9500000L, "Gold", 518400L),
                UpgradeStep(9, 10000000L, "Gold", 561600L),
                UpgradeStep(10, 11000000L, "Gold", 604800L),
                UpgradeStep(11, 16000000L, "Gold", 691200L),
                UpgradeStep(12, 26500000L, "Gold", 1166400L)
            )
        ),
        Building(
            dataId = 1000031L,
            name = "Eagle Artillery",
            levels = listOf(
                LevelRequirement(1, 11),
                LevelRequirement(2, 11),
                LevelRequirement(3, 12),
                LevelRequirement(4, 13),
                LevelRequirement(5, 14),
                LevelRequirement(6, 15),
                LevelRequirement(7, 16)
            ),
            upgrades = listOf(
                UpgradeStep(2, 6000000L, "Gold", 432000L),
                UpgradeStep(3, 9000000L, "Gold", 604800L),
                UpgradeStep(4, 10000000L, "Gold", 648000L),
                UpgradeStep(5, 12000000L, "Gold", 691200L),
                UpgradeStep(6, 13000000L, "Gold", 777600L),
                UpgradeStep(7, 14000000L, "Gold", 820800L)
            )
        ),
        Building(
            dataId = 1000077L,
            name = "Monolith",
            levels = listOf(
                LevelRequirement(1, 15),
                LevelRequirement(2, 15),
                LevelRequirement(3, 16),
                LevelRequirement(4, 17),
                LevelRequirement(5, 18)
            ),
            upgrades = listOf(
                UpgradeStep(2, 250000L, "Dark Elixir", 691200L),
                UpgradeStep(3, 260000L, "Dark Elixir", 777600L),
                UpgradeStep(4, 300000L, "Dark Elixir", 950400L),
                UpgradeStep(5, 470000L, "Dark Elixir", 1296000L)
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
