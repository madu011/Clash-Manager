package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VillageAttackProfileImporterTest {
    @Test
    fun importsKnownTroopSpellAndHeroLevels() {
        val json = """{
            "units":[{"data":4000000,"lvl":10,"cnt":5}],
            "spells":[{"data":26000000,"lvl":8,"cnt":2}],
            "heroes":[{"data":28000000,"lvl":80,"cnt":1}],
            "siege_machines":[]
        }"""

        val result = VillageAttackProfileImporter.fromVillageExport(json)

        assertEquals("Barbarian x 5", result.troops)
        assertEquals("Barbarian:10", result.troopLevels)
        assertEquals("Lightning Spell x 2", result.spells)
        assertEquals("Barbarian King Lv 80", result.heroes)
        assertEquals(3, result.importedEntries)
        assertEquals(0, result.entriesWithoutKnownNames)
        assertEquals(0, result.entriesWithoutLevels)
    }

    @Test
    fun leavesUnknownIdsAndMissingLevelsUninvented() {
        val json = """{
            "units":[
                {"data":4000000,"cnt":3},
                {"data":99999999,"lvl":12,"cnt":1}
            ],
            "heroes":[{"data":28000000,"cnt":1}]
        }"""

        val result = VillageAttackProfileImporter.fromVillageExport(json)

        assertEquals("Barbarian x 3", result.troops)
        assertEquals("", result.troopLevels)
        assertEquals("", result.heroes)
        assertEquals(1, result.entriesWithoutKnownNames)
        assertEquals(2, result.entriesWithoutLevels)
        assertTrue(result.importedEntries > 0)
    }

    @Test
    fun rejectsExportWithoutRecognisedCollections() {
        try {
            VillageAttackProfileImporter.fromVillageExport("""{"unrelated":[]}""")
            throw AssertionError("Expected an invalid export to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected: parser does not accept an unrecognised document.
        }
    }
}
