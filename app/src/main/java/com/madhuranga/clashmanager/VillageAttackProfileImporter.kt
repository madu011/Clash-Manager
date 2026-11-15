package com.madhuranga.clashmanager

/**
 * Army and progression fields that can be safely recovered from an imported in-game village export.
 * Missing levels and unknown game IDs are deliberately omitted rather than guessed.
 */
data class ImportedAttackProfile(
    val troops: String,
    val troopLevels: String,
    val spells: String,
    val heroes: String,
    val pets: String,
    val heroEquipment: String,
    val builderTroops: String,
    val builderHeroes: String,
    val siegeMachine: String,
    val importedEntries: Int,
    val entriesWithoutKnownNames: Int,
    val entriesWithoutLevels: Int
)

object VillageAttackProfileImporter {
    fun fromVillageExport(rawJson: String, gameDataPack: ParsedGameDataPack? = null): ImportedAttackProfile {
        val snapshot = VillageExportParser.parse(rawJson)
        val categories = snapshot.categories.associateBy { it.key }
        var unknownNames = 0
        var missingLevels = 0

        fun namedItems(key: String): List<Pair<ExportItem, String>> {
            val category = categories[key] ?: return emptyList()
            return category.items.mapNotNull { item ->
                val name = GameDataCatalog.nameFor(item.dataId, key, gameDataPack)
                if (name == null) {
                    unknownNames++
                    null
                } else {
                    if (item.level == null) missingLevels++
                    item to name
                }
            }
        }

        val troops = namedItems("units")
        val spells = namedItems("spells")
        val heroes = namedItems("heroes")
        val pets = namedItems("pets")
        val equipment = namedItems("equipment")
        val builderTroops = namedItems("units2")
        val builderHeroes = namedItems("heroes2")
        val siege = namedItems("siege_machines")

        fun quantities(items: List<Pair<ExportItem, String>>): String =
            items.joinToString(", ") { (item, name) -> "$name x ${item.count}" }

        fun levels(items: List<Pair<ExportItem, String>>): String =
            items.mapNotNull { (item, name) -> item.level?.let { "$name:$it" } }
                .distinct()
                .joinToString(", ")

        val heroLevels = heroes.mapNotNull { (item, name) ->
            item.level?.let { "$name Lv $it" }
        }.distinct().joinToString(", ")

        val importedCount = troops.size + spells.size + heroes.size + pets.size + equipment.size + builderTroops.size + builderHeroes.size + siege.size
        return ImportedAttackProfile(
            troops = quantities(troops),
            troopLevels = levels(troops),
            spells = quantities(spells),
            heroes = heroLevels,
            pets = pets.joinToString(", ") { (item, name) ->
                val level = item.level?.let { " Lv $it" } ?: ""
                "$name$level x ${item.count}"
            },
            heroEquipment = equipment.joinToString(", ") { (item, name) ->
                item.level?.let { "$name Lv $it" } ?: name
            }.distinct(),
            builderTroops = builderTroops.joinToString(", ") { (item, name) ->
                val level = item.level?.let { " Lv $it" } ?: ""
                "$name$level x ${item.count}"
            },
            builderHeroes = builderHeroes.mapNotNull { (item, name) ->
                item.level?.let { "$name Lv $it" }
            }.distinct().joinToString(", "),
            siegeMachine = siege.firstOrNull()?.second.orEmpty(),
            importedEntries = importedCount,
            entriesWithoutKnownNames = unknownNames,
            entriesWithoutLevels = missingLevels
        )
    }
}
