package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameDataCatalogTest {
    @Test fun resolvesVerifiedHomeVillageIdsInMatchingCollections() {
        assertEquals("Elixir Collector", GameDataCatalog.nameFor(1000002L, "buildings"))
        assertEquals("Gold Mine", GameDataCatalog.nameFor(1000004L, "buildings"))
        assertEquals("Gold Storage", GameDataCatalog.nameFor(1000005L, "buildings"))
        assertEquals("Cannon", GameDataCatalog.nameFor(1000008L, "buildings"))
        assertEquals("Archer Tower", GameDataCatalog.nameFor(1000009L, "buildings"))
        assertEquals("Mortar", GameDataCatalog.nameFor(1000013L, "buildings"))
        assertEquals("Lightning Spell", GameDataCatalog.nameFor(26000000L, "spells"))
        assertEquals("Barbarian King", GameDataCatalog.nameFor(28000000L, "heroes"))
        assertEquals("Archer Queen", GameDataCatalog.nameFor(28000001L, "heroes"))
        assertEquals("Barbarian", GameDataCatalog.nameFor(4000000L, "units"))
        assertEquals("Archer", GameDataCatalog.nameFor(4000001L, "units"))
        assertEquals("Goblin", GameDataCatalog.nameFor(4000002L, "units"))
        assertEquals("Giant", GameDataCatalog.nameFor(4000003L, "units"))
        assertEquals("Dragon", GameDataCatalog.nameFor(4000008L, "units"))
    }

    @Test fun resolvesVerifiedBuilderBaseIdsInMatchingCollections() {
        assertEquals("Gold Mine", GameDataCatalog.nameFor(1000037L, "buildings2"))
        assertEquals("Cannon", GameDataCatalog.nameFor(1000044L, "buildings2"))
        assertEquals("Archer Tower", GameDataCatalog.nameFor(1000048L, "buildings2"))
        assertEquals("Battle Machine", GameDataCatalog.nameFor(28000003L, "heroes2"))
        assertEquals("Battle Copter", GameDataCatalog.nameFor(28000005L, "heroes2"))
        assertEquals("Sneaky Archer", GameDataCatalog.nameFor(4000032L, "units2"))
    }

    @Test fun refusesUnknownIdsAndUnsupportedCollections() {
        assertNull(GameDataCatalog.nameFor(999999999L, "buildings"))
        assertNull(GameDataCatalog.nameFor(1000008L, "unknown"))
    }

    @Test fun legacyLookupReturnsOnlyUnambiguousNames() {
        assertEquals("Cannon", GameDataCatalog.nameFor(1000008L))
        assertNull(GameDataCatalog.nameFor(1000044L))
    }
}
