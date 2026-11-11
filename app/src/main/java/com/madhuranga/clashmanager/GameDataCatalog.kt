package com.madhuranga.clashmanager

/**
 * Small verified ID-to-name seed catalog.
 * Source: chiefpansancolt/clash-of-clans-data (MIT), dataId/name fields.
 * This intentionally returns null for IDs not yet verified against the source.
 */
object GameDataCatalog {
    private val namesByDataId = mapOf(
        1000004L to "Gold Mine",
        1000008L to "Cannon",
        1000009L to "Archer Tower",
        26000000L to "Lightning Spell",
        28000000L to "Barbarian King",
        28000001L to "Archer Queen",
        28000003L to "Battle Machine",
        4000000L to "Barbarian",
        4000001L to "Archer",
        4000008L to "Dragon"
    )

    fun nameFor(dataId: Long): String? = namesByDataId[dataId]
}
