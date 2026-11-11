package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameDataCatalogTest {
    @Test fun resolvesOnlyVerifiedGameDataIds() {
        assertEquals("Cannon", GameDataCatalog.nameFor(1000008L))
        assertEquals("Lightning Spell", GameDataCatalog.nameFor(26000000L))
        assertEquals("Barbarian King", GameDataCatalog.nameFor(28000000L))
        assertEquals("Barbarian", GameDataCatalog.nameFor(4000000L))
    }

    @Test fun unknownIdsRemainUnresolved() {
        assertNull(GameDataCatalog.nameFor(999999999L))
    }
}
