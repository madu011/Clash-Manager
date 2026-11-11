package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameDataCatalogTest {
    @Test fun resolvesOnlyVerifiedGameDataIds() {
        assertEquals("Gold Mine", GameDataCatalog.nameFor(1000004L))
        assertEquals("Cannon", GameDataCatalog.nameFor(1000008L))
        assertEquals("Archer Tower", GameDataCatalog.nameFor(1000009L))
        assertEquals("Lightning Spell", GameDataCatalog.nameFor(26000000L))
        assertEquals("Barbarian King", GameDataCatalog.nameFor(28000000L))
        assertEquals("Archer Queen", GameDataCatalog.nameFor(28000001L))
        assertEquals("Battle Machine", GameDataCatalog.nameFor(28000003L))
        assertEquals("Barbarian", GameDataCatalog.nameFor(4000000L))
        assertEquals("Archer", GameDataCatalog.nameFor(4000001L))
        assertEquals("Dragon", GameDataCatalog.nameFor(4000008L))
    }

    @Test fun unknownIdsRemainUnresolved() {
        assertNull(GameDataCatalog.nameFor(999999999L))
    }
}
