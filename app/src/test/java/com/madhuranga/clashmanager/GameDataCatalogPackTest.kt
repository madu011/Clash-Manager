package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Test

class GameDataCatalogPackTest {
    @Test fun validatedPackOverridesBuiltInNameForMatchingCollection() {
        val pack = GameDataPackParser.parse(
            """{"schemaVersion":1,"dataPackVersion":2,"items":[{"dataId":1000008,"name":"Cannon Updated","base":"home","category":"buildings"}]}""",
            2
        )
        assertEquals("Cannon Updated", GameDataCatalog.nameFor(1000008L, "buildings", pack))
    }

    @Test fun packDoesNotLeakAcrossCollections() {
        val pack = GameDataPackParser.parse(
            """{"schemaVersion":1,"dataPackVersion":2,"items":[{"dataId":1000008,"name":"Cannon Updated","base":"home","category":"buildings"}]}""",
            2
        )
        assertEquals(null, GameDataCatalog.nameFor(1000008L, "traps", pack))
    }
}
