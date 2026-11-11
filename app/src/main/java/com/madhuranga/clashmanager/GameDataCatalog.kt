package com.madhuranga.clashmanager

/**
 * Small verified ID-to-name seed catalog.
 * Source: chiefpansancolt/clash-of-clans-data (MIT), dataId/name fields.
 * This intentionally returns null for IDs not yet verified against the source.
 */
object GameDataCatalog {
    private val namesByDataId = mapOf(
        1000008L to "Cannon",
        26000000L to "Lightning Spell",
        28000000L to "Barbarian King",
        4000000L to "Barbarian"
    )

    fun nameFor(dataId: Long): String? = namesByDataId[dataId]
}
