package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuildingUpgradeCatalogTest {
    @Test fun cannonMaximumLevelFollowsTownHallRequirements() {
        assertEquals(2, BuildingUpgradeCatalog.maximumLevelAt(1000008L, 1))
        assertEquals(10, BuildingUpgradeCatalog.maximumLevelAt(1000008L, 8))
        assertEquals(19, BuildingUpgradeCatalog.maximumLevelAt(1000008L, 13))
        assertEquals(21, BuildingUpgradeCatalog.maximumLevelAt(1000008L, 15))
    }

    @Test fun archerTowerMaximumLevelFollowsTownHallRequirements() {
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000009L, 1))
        assertEquals(2, BuildingUpgradeCatalog.maximumLevelAt(1000009L, 2))
        assertEquals(10, BuildingUpgradeCatalog.maximumLevelAt(1000009L, 8))
        assertEquals(21, BuildingUpgradeCatalog.maximumLevelAt(1000009L, 15))
    }

    @Test fun cannonNextUpgradeReturnsVerifiedGoldAndDuration() {
        val step = BuildingUpgradeCatalog.nextUpgrade("Cannon", 10)
        assertEquals(11, step?.targetLevel)
        assertEquals(500_000L, step?.cost)
        assertEquals("Gold", step?.resource)
        assertEquals(16_200L, step?.durationSeconds)
    }

    @Test fun archerTowerFinalUpgradeReturnsVerifiedGoldAndDuration() {
        val step = BuildingUpgradeCatalog.nextUpgrade(1000009L, 20)
        assertEquals(21, step?.targetLevel)
        assertEquals(4_000_000L, step?.cost)
        assertEquals(172_800L, step?.durationSeconds)
    }

    @Test fun missingUpgradeDataAndUnknownBuildingsStayUnknown() {
        assertNull(BuildingUpgradeCatalog.nextUpgrade("Cannon", 21))
        assertNull(BuildingUpgradeCatalog.nextUpgrade("Unknown Building", 1))
        assertNull(BuildingUpgradeCatalog.nextUpgrade(123456789L, 1))
    }

    @Test fun unknownBuildingsAndInvalidTownHallLevelsStayUnknown() {
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(123456789L, 10))
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000008L, 0))
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000008L, 19))
    }

    @Test fun mortarTownHallUnlocksAndUpgradeCostsAreVerified() {
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000013L, 2))
        assertEquals(1, BuildingUpgradeCatalog.maximumLevelAt(1000013L, 3))
        assertEquals(10, BuildingUpgradeCatalog.maximumLevelAt(1000013L, 11))
        assertEquals(18, BuildingUpgradeCatalog.maximumLevelAt(1000013L, 18))

        val step = BuildingUpgradeCatalog.nextUpgrade("Mortar", 17)
        assertEquals(18, step?.targetLevel)
        assertEquals(21_000_000L, step?.cost)
        assertEquals("Gold", step?.resource)
        assertEquals(1_080_000L, step?.durationSeconds)
    }

    @Test fun mortarDoesNotIncludeSuperchargeAsNormalUpgradeLevels() {
        assertNull(BuildingUpgradeCatalog.nextUpgrade("Mortar", 18))
    }


    @Test fun wizardTowerUnlocksAndUpgradeCostsAreVerified() {
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000011L, 4))
        assertEquals(2, BuildingUpgradeCatalog.maximumLevelAt(1000011L, 5))
        assertEquals(17, BuildingUpgradeCatalog.maximumLevelAt(1000011L, 18))

        val step = BuildingUpgradeCatalog.nextUpgrade("Wizard Tower", 16)
        assertEquals(17, step?.targetLevel)
        assertEquals(14_000_000L, step?.cost)
        assertEquals("Gold", step?.resource)
        assertEquals(475_200L, step?.durationSeconds)
    }

    @Test fun wizardTowerDoesNotInventLevelAfterVerifiedMaximum() {
        assertNull(BuildingUpgradeCatalog.nextUpgrade("Wizard Tower", 17))
    }

}
