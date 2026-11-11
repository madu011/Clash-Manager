package com.madhuranga.clashmanager

import org.junit.Assert.assertEquals
import org.junit.Test

class VillageExportCoverageAnalyzerTest {
    @Test fun reportsCatalogMatchesAndUnknownIdsSeparately() {
        val snapshot = VillageExportParser.parse(
            """{"buildings":[{"data":1000008,"lvl":10,"cnt":2},{"data":99999999,"lvl":1}],"heroes":[{"data":28000000,"lvl":50}],"boosts":{"builder":1}}"""
        )
        val report = VillageExportCoverageAnalyzer.analyze(snapshot)
        assertEquals(4, report.totalItems)
        assertEquals(3, report.catalogMatchedItems)
        assertEquals(1, report.catalogUnmatchedItems)
        assertEquals(2, report.collectionsWithItems)
        assertEquals(1, report.collectionsWithoutItemDetails)
    }

    @Test fun emptyOrUnmappedCollectionsAreNotCalledInvalid() {
        val snapshot = VillageExportParser.parse(
            """{"buildings":[],"traps":[{"data":987654321,"lvl":1}]}"""
        )
        val report = VillageExportCoverageAnalyzer.analyze(snapshot)
        assertEquals(1, report.totalItems)
        assertEquals(0, report.catalogMatchedItems)
        assertEquals(1, report.catalogUnmatchedItems)
        assertEquals(1, report.collectionsWithItems)
        assertEquals(1, report.collectionsWithoutItemDetails)
    }
}
