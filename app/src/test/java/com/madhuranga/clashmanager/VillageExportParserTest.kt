package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VillageExportParserTest {
    @Test fun parsesCollectionsAndAggregatesCounts() {
        val json = """{"tag":"#UG028J2C","timestamp":1794358996,"buildings":[{"data":1000001,"lvl":14},{"data":1000000,"lvl":11,"cnt":4},{"data":1000000,"lvl":11,"cnt":2}],"heroes":[{"data":28000000,"lvl":53}],"units2":[{"data":4000031,"lvl":12}]}"""
        val result = VillageExportParser.parse(json)
        assertEquals("#UG028J2C", result.playerTag)
        assertEquals(1794358996L, result.timestamp)
        val buildings = result.categories.first { it.key == "buildings" }
        assertEquals(3, buildings.itemCount)
        assertEquals(7, buildings.totalCount)
        assertEquals(6, buildings.items.first { it.dataId == 1000000L }.count)
        assertEquals(53, result.categories.first { it.key == "heroes" }.items.first().level)
        assertTrue(result.categories.any { it.key == "units2" })
    }

    @Test fun rejectsJsonWithoutRecognisedCollections() {
        try {
            VillageExportParser.parse("""{"tag":"#ABC","unknown":[]}""")
            throw AssertionError("Expected rejection")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("No recognised"))
        }
    }

    @Test fun safelyCountsNestedModuleBuildingAsOneTopLevelItem() {
        val result = VillageExportParser.parse("""{"buildings":[{"data":1000097,"types":[{"data":103000011,"modules":[{"data":102000033,"lvl":1}]}]}]}""")
        assertEquals(1, result.categories.first().totalCount)
        assertEquals(1000097L, result.categories.first().items.first().dataId)
    }
}
