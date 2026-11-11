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

    @Test fun unknownBuildingsAndInvalidTownHallLevelsStayUnknown() {
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(123456789L, 10))
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000008L, 0))
        assertNull(BuildingUpgradeCatalog.maximumLevelAt(1000008L, 19))
    }
}
