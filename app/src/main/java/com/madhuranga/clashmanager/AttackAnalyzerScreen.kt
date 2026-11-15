package com.madhuranga.clashmanager

import android.net.Uri
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun loadVillageAttackProfile(
    context: android.content.Context,
    prefs: android.content.SharedPreferences,
    profileKey: String
): ImportedAttackProfile {
    val rawExport = requireNotNull(prefs.getString("village_export_json_$profileKey", null)) {
        "Import your village JSON on the Village screen first."
    }
    val activePack = runCatching {
        val packFile = java.io.File(context.filesDir, "game-data/active-pack.json")
        val packJson = packFile.readText(Charsets.UTF_8)
        GameDataPackParser.parse(packJson, JSONObject(packJson).getInt("dataPackVersion"))
    }.getOrNull()
    return VillageAttackProfileImporter.fromVillageExport(rawExport, activePack)
}

private fun countConfiguredUnits(input: String): Int {
    val patterns = listOf(
        Regex("(?i)\\bx\\s*(\\d+)\\b"),
        Regex("(?i)\\b(\\d+)\\s*x\\b")
    )
    return patterns.flatMap { regex ->
        regex.findAll(input).mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
    }.sum()
}

@Composable
private fun CapacityStatusRow(label: String, value: String, isOver: Boolean, gold: Color, muted: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = gold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Text(value, color = if (isOver) Color(0xFFFF8A80) else muted, fontSize = 10.sp)
    }
}

@Composable
fun AttackAnalyzerScreen(playerTag: String, screenshotUri: Uri?, onPickScreenshot: () -> Unit) {
    var mode by remember { mutableStateOf("Clan War") }
    var ownTownHall by remember { mutableStateOf("") }
    var targetTownHall by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("3-star") }
    var notes by remember { mutableStateOf("") }
    var armySection by remember { mutableStateOf("TROOPS") }
    val profileKey = playerTag.ifBlank { "unselected" }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("clash_manager", 0) }
    var troopSetup by remember(profileKey) { mutableStateOf(prefs.getString("attack_troops_$profileKey", "") ?: "") }
    var troopLevels by remember(profileKey) { mutableStateOf(prefs.getString("attack_troop_levels_$profileKey", "") ?: "") }
    var spellSetup by remember(profileKey) { mutableStateOf(prefs.getString("attack_spells_$profileKey", "") ?: "") }
    var heroSetup by remember(profileKey) { mutableStateOf(prefs.getString("attack_heroes_$profileKey", "") ?: "") }
    var heroEquipment by remember(profileKey) { mutableStateOf(prefs.getString("attack_hero_equipment_$profileKey", "") ?: "") }
    var siegeSetup by remember(profileKey) { mutableStateOf(prefs.getString("attack_siege_$profileKey", "") ?: "") }
    var troopCapacity by remember(profileKey) { mutableStateOf(prefs.getString("attack_troop_capacity_$profileKey", "") ?: "") }
    var spellCapacity by remember(profileKey) { mutableStateOf(prefs.getString("attack_spell_capacity_$profileKey", "") ?: "") }
    var siegeCapacity by remember(profileKey) { mutableStateOf(prefs.getString("attack_siege_capacity_$profileKey", "1") ?: "1") }
    var prepared by remember { mutableStateOf(false) }
    var villageImportStatus by remember(profileKey) { mutableStateOf("") }
    var validationErrors by remember { mutableStateOf(emptyList<String>()) }
    val historyKey = "attack_plans_${playerTag.ifBlank { "unselected" }}"
    var editingPlanIndex by remember(historyKey) { mutableStateOf(-1) }
    var savedPlans by remember(historyKey) {
        mutableStateOf(runCatching {
            val array = JSONArray(prefs.getString(historyKey, "[]") ?: "[]")
            (0 until array.length()).map { array.getJSONObject(it).toString() }
        }.getOrDefault(emptyList()))
    }
    val resultsKey = "attack_results_${playerTag.ifBlank { "unselected" }}"
    var attackResults by remember(resultsKey) {
        mutableStateOf(runCatching {
            val array = JSONArray(prefs.getString(resultsKey, "[]") ?: "[]")
            (0 until array.length()).map { array.getJSONObject(it).toString() }
        }.getOrDefault(emptyList()))
    }
    var resultPlanIndex by remember(resultsKey) { mutableStateOf(-1) }
    var resultStars by remember(resultsKey) { mutableStateOf(0) }
    var resultDestruction by remember(resultsKey) { mutableStateOf("") }
    var resultNotes by remember(resultsKey) { mutableStateOf("") }
    var resultSearch by remember(resultsKey) { mutableStateOf("") }
    var resultFromDate by remember(resultsKey) { mutableStateOf("") }
    var resultToDate by remember(resultsKey) { mutableStateOf("") }
    val panel = Color(0xFF1A2230)
    val gold = Color(0xFFFFC857)
    val muted = Color(0xFF9AA8BA)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ATTACK ANALYZER", color = gold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Plan attacks using your selected player profile. No separate enemy account login is needed.", color = muted, fontSize = 12.sp)
                Text(if (playerTag.isBlank()) "Select your Player Tag on Dashboard first." else "Your profile: $playerTag", color = Color.White, fontSize = 12.sp)
                Text("ATTACK MODE", color = gold, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Clan War", "CWL", "Multiplayer", "Friendly").forEach {
                        FilterChip(selected = mode == it, onClick = { mode = it; prepared = false }, label = { Text(it, fontSize = 10.sp) })
                    }
                }
                OutlinedTextField(ownTownHall, { ownTownHall = it.filter(Char::isDigit).take(2) }, label = { Text("Your Town Hall level") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(targetTownHall, { targetTownHall = it.filter(Char::isDigit).take(2) }, label = { Text("Target Town Hall level") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222F40)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80643B))
                ) {
                    Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("⚔️", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val data = loadVillageAttackProfile(context, prefs, profileKey)
                                            troopSetup = data.troops
                                            troopLevels = data.troopLevels
                                            spellSetup = data.spells
                                            heroSetup = data.heroes
                                            heroEquipment = data.heroEquipment
                                            siegeSetup = data.siegeMachine
                                            prefs.edit()
                                                .putString("attack_troops_$profileKey", troopSetup)
                                                .putString("attack_troop_levels_$profileKey", troopLevels)
                                                .putString("attack_spells_$profileKey", spellSetup)
                                                .putString("attack_heroes_$profileKey", heroSetup)
                                                .putString("attack_hero_equipment_$profileKey", heroEquipment)
                                                .putString("attack_pets_$profileKey", data.pets)
                                                .putString("attack_builder_troops_$profileKey", data.builderTroops)
                                                .putString("attack_builder_heroes_$profileKey", data.builderHeroes)
                                                .putString("attack_siege_$profileKey", siegeSetup)
                                                .apply()
                                            prepared = false
                                            villageImportStatus = "Imported ${data.importedEntries} entries • Unknown IDs: ${data.entriesWithoutKnownNames} • Missing levels: ${data.entriesWithoutLevels}"
                                        } catch (error: Exception) {
                                            villageImportStatus = error.message ?: "Village import failed."
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("IMPORT VILLAGE ARMY DATA") }
                                if (villageImportStatus.isNotBlank()) {
                                    Text(villageImportStatus, color = muted, fontSize = 10.sp)
                                }
                                Text("ARMY COMPOSITION", color = gold, fontWeight = FontWeight.ExtraBold)
                                Text("Your own troop, spell and hero details", color = muted, fontSize = 10.sp)
                            }
                            Text("MANUAL", color = Color(0xFF9BE7A8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            listOf("TROOPS", "SPELLS", "HEROES", "EQUIPMENT").forEach { section ->
                                val selected = armySection == section
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    onClick = { armySection = section },
                                    color = if (selected) Color(0xFF765326) else Color(0xFF172230),
                                    shape = RoundedCornerShape(9.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) gold else Color(0xFF465365))
                                ) {
                                    Text(section, modifier = Modifier.padding(vertical = 10.dp, horizontal = 3.dp), color = if (selected) Color.White else muted, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                        when (armySection) {
                            "TROOPS" -> {
                                OutlinedTextField(troopSetup, { value -> troopSetup = value.take(1200); prepared = false; prefs.edit().putString("attack_troops_$profileKey", value.take(1200)).apply() }, label = { Text("Troops and quantities") }, placeholder = { Text("Example: Root Rider x 8, Valkyrie x 4") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(troopLevels, { value -> troopLevels = value.take(1200); prepared = false; prefs.edit().putString("attack_troop_levels_$profileKey", value.take(1200)).apply() }, label = { Text("Troop levels") }, placeholder = { Text("Example: Root Rider:3, Valkyrie:12") }, supportingText = { Text("Format: Unit name:level, separated by commas. Enter only levels you have checked.") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(siegeSetup, { siegeSetup = it.take(200); prepared = false; prefs.edit().putString("attack_siege_$profileKey", siegeSetup).apply() }, label = { Text("Siege machine (optional)") }, placeholder = { Text("Example: Log Launcher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            }
                            "SPELLS" -> OutlinedTextField(spellSetup, { value -> spellSetup = value.take(800); prepared = false; prefs.edit().putString("attack_spells_$profileKey", value.take(800)).apply() }, label = { Text("Spells and quantities") }, placeholder = { Text("Example: Rage x 2, Freeze x 3") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                            "HEROES" -> OutlinedTextField(heroSetup, { value -> heroSetup = value.take(500); prepared = false; prefs.edit().putString("attack_heroes_$profileKey", value.take(500)).apply() }, label = { Text("Hero levels") }, placeholder = { Text("Example: Barbarian King Lv 90, Archer Queen Lv 95") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                            else -> {
                                Text("Record equipment selected for each hero. Use the names shown in your current game version.", color = muted, fontSize = 11.sp)
                                OutlinedTextField(heroEquipment, { value -> heroEquipment = value.take(1000); prepared = false; prefs.edit().putString("attack_hero_equipment_$profileKey", value.take(1000)).apply() }, label = { Text("Hero equipment selection") }, placeholder = { Text("Example: Barbarian King: Giant Gauntlet, Rage Vial\nArcher Queen: Frozen Arrow, Invisibility Vial") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        Text("Your troop levels and hero equipment are saved to this player profile. Automatic detection is not active yet.", color = muted, fontSize = 10.sp)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222F40)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80643B))
                ) {
                    Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("🏰", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ARMY CAPACITY", color = gold, fontWeight = FontWeight.ExtraBold)
                                Text("Enter capacities from your current in-game army", color = muted, fontSize = 10.sp)
                            }
                        }
                        OutlinedTextField(troopCapacity, { value -> troopCapacity = value.filter(Char::isDigit).take(4); prepared = false; prefs.edit().putString("attack_troop_capacity_$profileKey", troopCapacity).apply() }, label = { Text("Troop housing capacity") }, placeholder = { Text("Your available troop space") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(spellCapacity, { value -> spellCapacity = value.filter(Char::isDigit).take(4); prepared = false; prefs.edit().putString("attack_spell_capacity_$profileKey", spellCapacity).apply() }, label = { Text("Spell capacity") }, placeholder = { Text("Your available spell space") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(siegeCapacity, { value -> siegeCapacity = value.filter(Char::isDigit).take(2); prepared = false; prefs.edit().putString("attack_siege_capacity_$profileKey", siegeCapacity).apply() }, label = { Text("Siege machine slots available") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        val usedTroops = countConfiguredUnits(troopSetup)
                        val usedSpells = countConfiguredUnits(spellSetup)
                        val troopCap = troopCapacity.toIntOrNull()
                        val spellCap = spellCapacity.toIntOrNull()
                        val siegeCap = siegeCapacity.toIntOrNull()
                        val troopStatus = when {
                            troopCap == null -> "Capacity not entered"
                            usedTroops > troopCap -> "Over capacity by ${usedTroops - troopCap}"
                            else -> "${troopCap - usedTroops} space remaining"
                        }
                        val spellStatus = when {
                            spellCap == null -> "Capacity not entered"
                            usedSpells > spellCap -> "Over capacity by ${usedSpells - spellCap}"
                            else -> "${spellCap - usedSpells} space remaining"
                        }
                        val siegeUsed = if (siegeSetup.isBlank()) 0 else 1
                        val siegeStatus = when {
                            siegeCap == null -> "Capacity not entered"
                            siegeUsed > siegeCap -> "Over capacity"
                            else -> "${siegeCap - siegeUsed} slot(s) remaining"
                        }
                        CapacityStatusRow("TROOPS", if (troopCap == null) "${usedTroops} used • $troopStatus" else "${usedTroops} / $troopCap • $troopStatus", troopCap != null && usedTroops > troopCap, gold, muted)
                        CapacityStatusRow("SPELLS", if (spellCap == null) "${usedSpells} used • $spellStatus" else "${usedSpells} / $spellCap • $spellStatus", spellCap != null && usedSpells > spellCap, gold, muted)
                        CapacityStatusRow("SIEGE", if (siegeCap == null) siegeStatus else "${siegeUsed} / $siegeCap • $siegeStatus", siegeCap != null && siegeUsed > siegeCap, gold, muted)
                        Text("Counts are estimated from entries formatted as Unit x quantity. Enter each in-game capacity yourself; this is not an automatic game-data lookup.", color = muted, fontSize = 10.sp)
                    }
                }
                Text("ATTACK GOAL", color = gold, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("1-star", "2-star", "3-star").forEach {
                        FilterChip(selected = goal == it, onClick = { goal = it; prepared = false }, label = { Text(it) })
                    }
                }
                OutlinedTextField(notes, { notes = it; prepared = false }, label = { Text("Base notes (optional)") }, placeholder = { Text("Defence positions, compartments…") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { onPickScreenshot(); prepared = false }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (screenshotUri == null) "ATTACH BASE SCREENSHOT" else "CHANGE BASE SCREENSHOT")
                }
                Text(if (screenshotUri == null) "No target base image attached." else "Target base screenshot attached.", color = muted, fontSize = 11.sp)
                Button(onClick = {
                    validationErrors = AttackAnalysisValidator.validate(
                        playerTag = playerTag,
                        ownTownHallLevel = ownTownHall.toIntOrNull(),
                        targetTownHallLevel = targetTownHall.toIntOrNull(),
                        starGoal = goal.substringBefore("-").toIntOrNull() ?: 0,
                        hasScreenshot = screenshotUri != null,
                        notes = notes
                    )
                    val imageError = screenshotUri?.let { uri ->
                        val imageSize = runCatching {
                            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
                        }.getOrNull()
                        AttackScreenshotValidator.validate(context.contentResolver.getType(uri), imageSize)
                    }
                    if (imageError != null) validationErrors = validationErrors + imageError
                    prepared = validationErrors.isEmpty()
                }, enabled = true, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color(0xFF10141D))) {
                    Text("PREPARE ANALYSIS", fontWeight = FontWeight.Bold)
                }
                validationErrors.forEach { error ->
                    Text("• $error", color = Color(0xFFFF9B8F), fontSize = 11.sp)
                }
            }
        }
        if (prepared) {
            Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ANALYSIS INPUT READY", color = gold, fontWeight = FontWeight.Bold)
                    Text("$mode • TH$ownTownHall vs TH$targetTownHall • Goal: $goal", color = Color.White)
                    Text("The image is selected and the attack scenario is recorded on screen. Automated image recognition, troop-level matching and probability calculation require the next analysis-engine/backend integration; this screen does not invent a troop army or success score.", color = muted, fontSize = 12.sp)
                    Text("Hidden traps, defending Clan Castle troops, unseen levels, timing and player execution prevent a guaranteed 100% result.", color = Color(0xFFFFD18A), fontSize = 12.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF222F40)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("ATTACK PLAN SUMMARY", color = gold, fontWeight = FontWeight.ExtraBold)
                            CapacityStatusRow("MODE", mode, false, gold, Color.White)
                            CapacityStatusRow("TOWN HALL", "TH$ownTownHall → TH$targetTownHall", false, gold, Color.White)
                            CapacityStatusRow("STAR GOAL", goal, false, gold, Color.White)
                            Divider(color = Color(0xFF465365))
                            Text("⚔️ TROOPS", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(troopSetup.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            if (troopLevels.isNotBlank()) Text("Levels: $troopLevels", color = muted, fontSize = 10.sp)
                            Text("✨ SPELLS", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(spellSetup.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🦸 HEROES", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(heroSetup.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🐾 PETS", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(petSetup.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🛠️ BUILDER BASE TROOPS", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(builderTroopSetup.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🦾 BUILDER BASE HERO", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(builderHeroSetup.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🎖️ HERO EQUIPMENT", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(heroEquipment.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🏰 SIEGE MACHINE", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(siegeSetup.ifBlank { "Not selected" }, color = Color.White, fontSize = 11.sp)
                            Divider(color = Color(0xFF465365))
                            CapacityStatusRow("TROOP CAPACITY", if (troopCapacity.isBlank()) "Not entered" else "$troopCapacity available • ${countConfiguredUnits(troopSetup)} listed", false, gold, muted)
                            CapacityStatusRow("SPELL CAPACITY", if (spellCapacity.isBlank()) "Not entered" else "$spellCapacity available • ${countConfiguredUnits(spellSetup)} listed", false, gold, muted)
                            CapacityStatusRow("SIEGE SLOTS", if (siegeCapacity.isBlank()) "Not entered" else "$siegeCapacity available", false, gold, muted)
                            CapacityStatusRow("BASE SCREENSHOT", if (screenshotUri == null) "Not attached" else "Attached", false, gold, muted)
                            if (notes.isNotBlank()) {
                                Text("BASE NOTES", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(notes, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Button(onClick = {
                        val item = JSONObject().apply {
                            put("mode", mode); put("ownTownHall", ownTownHall)
                            put("targetTownHall", targetTownHall); put("goal", goal)
                            put("notes", notes)
                            put("troops", troopSetup); put("troopLevels", troopLevels); put("spells", spellSetup)
                            put("heroes", heroSetup); put("pets", petSetup); put("heroEquipment", heroEquipment)
                            put("builderTroops", builderTroopSetup); put("builderHeroes", builderHeroSetup); put("siegeMachine", siegeSetup)
                            put("troopCapacity", troopCapacity); put("spellCapacity", spellCapacity); put("siegeCapacity", siegeCapacity)
                            put("createdAt", System.currentTimeMillis())
                            put("screenshotUri", screenshotUri?.toString() ?: "")
                        }
                        val updated = if (editingPlanIndex in savedPlans.indices) {
                            savedPlans.toMutableList().apply { set(editingPlanIndex, item.toString()) }
                        } else {
                            (listOf(item.toString()) + savedPlans).take(30)
                        }
                        val array = JSONArray()
                        updated.forEach { array.put(JSONObject(it)) }
                        prefs.edit().putString(historyKey, array.toString()).apply()
                        savedPlans = updated
                        editingPlanIndex = -1
                    }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color(0xFF10141D))
                    ) { Text(if (editingPlanIndex in savedPlans.indices) "UPDATE ATTACK PLAN" else "SAVE ATTACK PLAN") }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("SAVED ATTACK PLANS", color = gold, fontWeight = FontWeight.Bold)
                if (savedPlans.isEmpty()) Text("No saved plans for this player profile yet.", color = muted, fontSize = 11.sp)
                savedPlans.forEachIndexed { index, raw ->
                    val plan = runCatching { JSONObject(raw) }.getOrNull()
                    if (plan != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("${plan.optString("mode")} • ${plan.optString("goal")}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("TH${plan.optString("ownTownHall")} vs TH${plan.optString("targetTownHall")}", color = muted, fontSize = 11.sp)
                            }
                            Column {
                                TextButton(onClick = {
                                    mode = plan.optString("mode", "Clan War")
                                    ownTownHall = plan.optString("ownTownHall")
                                    targetTownHall = plan.optString("targetTownHall")
                                    goal = plan.optString("goal", "3-star")
                                    notes = plan.optString("notes")
                                    troopSetup = plan.optString("troops")
                                    troopLevels = plan.optString("troopLevels")
                                    spellSetup = plan.optString("spells")
                                    heroSetup = plan.optString("heroes")
                                    petSetup = plan.optString("pets")
                                    builderTroopSetup = plan.optString("builderTroops")
                                    builderHeroSetup = plan.optString("builderHeroes")
                                    heroEquipment = plan.optString("heroEquipment")
                                    siegeSetup = plan.optString("siegeMachine")
                                    troopCapacity = plan.optString("troopCapacity")
                                    spellCapacity = plan.optString("spellCapacity")
                                    siegeCapacity = plan.optString("siegeCapacity", "1")
                                    armySection = "TROOPS"
                                    editingPlanIndex = index
                                    prepared = false
                                    validationErrors = emptyList()
                                }) { Text("Load / Edit") }
                                TextButton(onClick = {
                                val updated = savedPlans.filterIndexed { i, _ -> i != index }
                                val array = JSONArray()
                                updated.forEach { array.put(JSONObject(it)) }
                                prefs.edit().putString(historyKey, array.toString()).apply()
                                savedPlans = updated
                                if (editingPlanIndex == index) editingPlanIndex = -1
                            }) { Text("Delete") }
                            }
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222F40)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80643B))
                ) {
                    Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("ATTACK RESULTS HISTORY", color = gold, fontWeight = FontWeight.ExtraBold)
                        Text("Record the actual result after your attack. Results are entered by you, not estimated by the app.", color = muted, fontSize = 10.sp)
                        if (savedPlans.isEmpty()) {
                            Text("Save an attack plan first to record its result.", color = muted, fontSize = 11.sp)
                        } else {
                            var resultMenuExpanded by remember { mutableStateOf(false) }
                            OutlinedButton(onClick = { resultMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (resultPlanIndex in savedPlans.indices) {
                                    val selected = JSONObject(savedPlans[resultPlanIndex])
                                    "${selected.optString("mode")} • TH${selected.optString("targetTownHall")}"
                                } else "Choose saved attack plan")
                            }
                            DropdownMenu(expanded = resultMenuExpanded, onDismissRequest = { resultMenuExpanded = false }) {
                                savedPlans.forEachIndexed { index, raw ->
                                    val plan = runCatching { JSONObject(raw) }.getOrNull()
                                    if (plan != null) DropdownMenuItem(
                                        text = { Text("${plan.optString("mode")} • TH${plan.optString("targetTownHall")} • ${plan.optString("goal")}") },
                                        onClick = { resultPlanIndex = index; resultMenuExpanded = false }
                                    )
                                }
                            }
                            Text("ACTUAL STARS", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..3).forEach { stars ->
                                    FilterChip(selected = resultStars == stars, onClick = { resultStars = stars }, label = { Text("$stars ★") })
                                }
                            }
                            OutlinedTextField(resultDestruction, { resultDestruction = it.filter(Char::isDigit).take(3) }, label = { Text("Destruction percentage (0–100)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(resultNotes, { resultNotes = it.take(1000) }, label = { Text("Battle result notes (optional)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    val percent = resultDestruction.toIntOrNull()
                                    if (resultPlanIndex in savedPlans.indices && percent != null && percent in 0..100) {
                                        val plan = JSONObject(savedPlans[resultPlanIndex])
                                        val result = JSONObject().apply {
                                            put("planCreatedAt", plan.optLong("createdAt"))
                                            put("mode", plan.optString("mode"))
                                            put("targetTownHall", plan.optString("targetTownHall"))
                                            put("goal", plan.optString("goal"))
                                            put("stars", resultStars)
                                            put("destructionPercent", percent)
                                            put("notes", resultNotes)
                                            put("recordedAt", System.currentTimeMillis())
                                        }
                                        val updated = (listOf(result.toString()) + attackResults).take(100)
                                        val array = JSONArray()
                                        updated.forEach { array.put(JSONObject(it)) }
                                        prefs.edit().putString(resultsKey, array.toString()).apply()
                                        attackResults = updated
                                        resultPlanIndex = -1
                                        resultStars = 0
                                        resultDestruction = ""
                                        resultNotes = ""
                                    }
                                },
                                enabled = resultPlanIndex in savedPlans.indices && ((resultDestruction.toIntOrNull() ?: -1) in 0..100),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color(0xFF10141D))
                            ) { Text("SAVE ATTACK RESULT") }
                        }
                        Divider(color = Color(0xFF465365))
                        if (attackResults.isEmpty()) {
                            Text("No attack results recorded yet.", color = muted, fontSize = 11.sp)
                        } else {
                            Text("SEARCH AND FILTER", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            OutlinedTextField(
                                value = resultSearch,
                                onValueChange = { resultSearch = it },
                                label = { Text("Search mode, notes, target TH or stars") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = resultFromDate,
                                    onValueChange = { resultFromDate = it.take(10) },
                                    label = { Text("From (YYYY-MM-DD)") },
                                    placeholder = { Text("2026-11-01") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = resultToDate,
                                    onValueChange = { resultToDate = it.take(10) },
                                    label = { Text("To (YYYY-MM-DD)") },
                                    placeholder = { Text("2026-11-15") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            TextButton(onClick = { resultSearch = ""; resultFromDate = ""; resultToDate = "" }) { Text("CLEAR FILTERS") }
                            val dateFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { isLenient = false } }
                            val fromMillis = remember(resultFromDate) {
                                if (resultFromDate.isBlank()) null else runCatching { dateFormat.parse(resultFromDate)?.time }.getOrNull()
                            }
                            val toMillis = remember(resultToDate) {
                                if (resultToDate.isBlank()) null else runCatching { dateFormat.parse(resultToDate)?.let { it.time + 86_399_999L } }.getOrNull()
                            }
                            val filteredResults = attackResults.mapIndexedNotNull { index, raw ->
                                val result = runCatching { JSONObject(raw) }.getOrNull() ?: return@mapIndexedNotNull null
                                val recordedAt = result.optLong("recordedAt", 0L)
                                val query = resultSearch.trim()
                                val matchesSearch = query.isBlank() || listOf(
                                    result.optString("mode"),
                                    result.optString("targetTownHall"),
                                    result.optString("goal"),
                                    result.optString("stars"),
                                    result.optString("destructionPercent"),
                                    result.optString("notes")
                                ).any { it.contains(query, ignoreCase = true) }
                                val matchesFrom = fromMillis == null || recordedAt >= fromMillis
                                val matchesTo = toMillis == null || recordedAt <= toMillis
                                if (matchesSearch && matchesFrom && matchesTo) index to result else null
                            }
                            Text("${filteredResults.size} of ${attackResults.size} result(s)", color = muted, fontSize = 10.sp)
                            if (filteredResults.isEmpty()) {
                                Text("No results match these filters. Check the dates or clear the filters.", color = muted, fontSize = 11.sp)
                            }
                            filteredResults.forEach { (index, result) ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("${result.optString("mode")} • ${result.optInt("stars")} ★ • ${result.optInt("destructionPercent")}% ", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                            Text("Target TH${result.optString("targetTownHall")} • Goal ${result.optString("goal")}", color = muted, fontSize = 10.sp)
                                            if (result.optString("notes").isNotBlank()) Text(result.optString("notes"), color = muted, fontSize = 10.sp)
                                        }
                                        TextButton(onClick = {
                                            val updated = attackResults.filterIndexed { i, _ -> i != index }
                                            val array = JSONArray()
                                            updated.forEach { array.put(JSONObject(it)) }
                                            prefs.edit().putString(resultsKey, array.toString()).apply()
                                            attackResults = updated
                                        }) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222F40)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80643B))
                ) {
                    Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("ATTACK RESULTS ANALYTICS", color = gold, fontWeight = FontWeight.ExtraBold)
                        val parsedResults = attackResults.mapNotNull { raw -> runCatching { JSONObject(raw) }.getOrNull() }
                        val resultCount = parsedResults.size
                        val averageStars = if (resultCount > 0) parsedResults.map { it.optInt("stars") }.average() else 0.0
                        val averageDestruction = if (resultCount > 0) parsedResults.map { it.optInt("destructionPercent") }.average() else 0.0
                        val threeStarCount = parsedResults.count { it.optInt("stars") == 3 }
                        val twoStarCount = parsedResults.count { it.optInt("stars") == 2 }
                        val oneStarCount = parsedResults.count { it.optInt("stars") == 1 }
                        if (resultCount == 0) {
                            Text("Record real attack results to see your statistics here.", color = muted, fontSize = 11.sp)
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("RECORDED", color = muted, fontSize = 9.sp)
                                    Text("$resultCount", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("AVG STARS", color = muted, fontSize = 9.sp)
                                    Text(String.format(java.util.Locale.US, "%.2f / 3", averageStars), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("AVG DAMAGE", color = muted, fontSize = 9.sp)
                                    Text(String.format(java.util.Locale.US, "%.1f%%", averageDestruction), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }
                            }
                            Divider(color = Color(0xFF465365))
                            Text("STAR DISTRIBUTION", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            CapacityStatusRow("3 STAR", "$threeStarCount attacks", false, gold, muted)
                            CapacityStatusRow("2 STAR", "$twoStarCount attacks", false, gold, muted)
                            CapacityStatusRow("1 STAR", "$oneStarCount attacks", false, gold, muted)
                            CapacityStatusRow("0 STAR", "${parsedResults.count { it.optInt("stars") == 0 }} attacks", false, gold, muted)
                            Divider(color = Color(0xFF465365))
                            Text("BY ATTACK MODE", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            listOf("Clan War", "CWL", "Multiplayer", "Friendly").forEach { attackMode ->
                                val modeResults = parsedResults.filter { it.optString("mode") == attackMode }
                                if (modeResults.isNotEmpty()) {
                                    val modeAverage = modeResults.map { it.optInt("stars") }.average()
                                    val modeDamage = modeResults.map { it.optInt("destructionPercent") }.average()
                                    CapacityStatusRow(attackMode.uppercase(), "${modeResults.size} • ${String.format(java.util.Locale.US, "%.1f", modeAverage)} ★ • ${String.format(java.util.Locale.US, "%.1f", modeDamage)}%", false, gold, muted)
                                }
                            }
                            Text("Statistics use only the results you recorded on this device. They are not predictions of future attacks.", color = muted, fontSize = 10.sp)
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222F40)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80643B))
                ) {
                    Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("EXPORT ATTACK RESULTS", color = gold, fontWeight = FontWeight.ExtraBold)
                        Text("Share your recorded battle history as a CSV spreadsheet or JSON data.", color = muted, fontSize = 10.sp)
                        val exportableResults = attackResults.mapNotNull { raw -> runCatching { JSONObject(raw) }.getOrNull() }
                        val csvCell: (String) -> String = { value -> "\"" + value.replace("\"", "\"\"") + "\"" }
                        val csvContent = buildString {
                            append("Mode,Target Town Hall,Goal,Stars,Destruction Percent,Notes,Recorded At\n")
                            exportableResults.forEach { item ->
                                val values = listOf(
                                    item.optString("mode"),
                                    item.optString("targetTownHall"),
                                    item.optString("goal"),
                                    item.optInt("stars").toString(),
                                    item.optInt("destructionPercent").toString(),
                                    item.optString("notes"),
                                    item.optLong("recordedAt").toString()
                                )
                                append(values.joinToString(",") { csvCell(it) })
                                append('\n')
                            }
                        }
                        val jsonContent = JSONArray().apply {
                            exportableResults.forEach { put(it) }
                        }.toString(2)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "Clash Manager attack results")
                                        putExtra(Intent.EXTRA_TEXT, csvContent)
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share CSV results"))
                                },
                                enabled = exportableResults.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("EXPORT CSV") }
                            OutlinedButton(
                                onClick = {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "Clash Manager attack results")
                                        putExtra(Intent.EXTRA_TEXT, jsonContent)
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share JSON results"))
                                },
                                enabled = exportableResults.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("EXPORT JSON") }
                        }
                        Text(if (exportableResults.isEmpty()) "Record at least one attack result to enable export." else "${exportableResults.size} recorded result(s) ready to share. The export contains only attack results, not screenshots or login details.", color = muted, fontSize = 10.sp)
                    }
                }
                Text("FULL ANALYSIS ROADMAP", color = gold, fontWeight = FontWeight.Bold)
                Text(
                    listOf(
                        "• Recognise base layout, compartments and visible defences",
                        "• Match available troops, spells, heroes and siege machines to their levels",
                        "• Suggest attack entry, funnel and deployment order",
                        "• Estimate 1-star, 2-star and 3-star outcomes separately with confidence",
                        "• Save plans and results for Clan War, CWL, Multiplayer and Friendly attacks"
                    ).joinToString("\n"),
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text("The analyzer must show unknown information clearly instead of guessing.", color = muted, fontSize = 11.sp)
            }
        }
    }
