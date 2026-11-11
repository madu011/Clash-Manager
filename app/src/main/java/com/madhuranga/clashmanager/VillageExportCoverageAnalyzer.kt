package com.madhuranga.clashmanager

data class VillageExportCoverage(
    val totalItems: Int,
    val catalogMatchedItems: Int,
    val catalogUnmatchedItems: Int,
    val collectionsWithItems: Int,
    val collectionsWithoutItemDetails: Int
) {
    fun asMessage(): String =
        "Item entries: $totalItems • names matched in built-in catalog: $catalogMatchedItems • IDs not yet in catalog: $catalogUnmatchedItems • collections with item details: $collectionsWithItems • sections without item details: $collectionsWithoutItemDetails"
}

/**
 * Describes parser/catalog coverage without treating an unrecognised ID as invalid.
 * The built-in name catalog is intentionally partial.
 */
object VillageExportCoverageAnalyzer {
    fun analyze(snapshot: VillageExportSnapshot): VillageExportCoverage {
        val total = snapshot.categories.sumOf { category -> category.items.sumOf { it.count } }
        val matched = snapshot.categories.sumOf { category ->
            category.items.filter { GameDataCatalog.nameFor(it.dataId, category.key) != null }
                .sumOf { it.count }
        }
        val collectionsWithItems = snapshot.categories.count { it.items.isNotEmpty() }
        val sectionsWithoutDetails = snapshot.sections.count { section ->
            snapshot.categories.none { it.key == section.key && it.items.isNotEmpty() }
        }
        return VillageExportCoverage(
            totalItems = total,
            catalogMatchedItems = matched,
            catalogUnmatchedItems = (total - matched).coerceAtLeast(0),
            collectionsWithItems = collectionsWithItems,
            collectionsWithoutItemDetails = sectionsWithoutDetails
        )
    }
}
