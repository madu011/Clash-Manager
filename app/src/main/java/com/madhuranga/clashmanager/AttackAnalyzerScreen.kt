package com.madhuranga.clashmanager

import android.net.Uri
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
    var validationErrors by remember { mutableStateOf(emptyList<String>()) }
    val historyKey = "attack_plans_${playerTag.ifBlank { "unselected" }}"
    var savedPlans by remember(historyKey) {
        mutableStateOf(runCatching {
            val array = JSONArray(prefs.getString(historyKey, "[]") ?: "[]")
            (0 until array.length()).map { array.getJSONObject(it).toString() }
        }.getOrDefault(emptyList()))
    }
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
                            Text("🎖️ HERO EQUIPMENT", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(heroEquipment.ifBlank { "Not entered" }, color = Color.White, fontSize = 11.sp)
                            Text("🏰 SIEGE MACHINE", color = gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(siegeSetup.ifBlank { "Not selected" }, color = Color.White, fontSize = 11.sp)
                            Divider(color = Color(0xFF465365))
                            CapacityStatusRow("TROOP CAPACITY", if (troopCapacity.isBlank()) "Not entered" else "$troopCapacity available • $usedTroops listed", false, gold, muted)
                            CapacityStatusRow("SPELL CAPACITY", if (spellCapacity.isBlank()) "Not entered" else "$spellCapacity available • $usedSpells listed", false, gold, muted)
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
                            put("heroes", heroSetup); put("heroEquipment", heroEquipment); put("siegeMachine", siegeSetup)
                            put("troopCapacity", troopCapacity); put("spellCapacity", spellCapacity); put("siegeCapacity", siegeCapacity)
                            put("createdAt", System.currentTimeMillis())
                            put("screenshotUri", screenshotUri?.toString() ?: "")
                        }
                        val updated = (listOf(item.toString()) + savedPlans).take(30)
                        val array = JSONArray()
                        updated.forEach { array.put(JSONObject(it)) }
                        prefs.edit().putString(historyKey, array.toString()).apply()
                        savedPlans = updated
                    }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color(0xFF10141D))
                    ) { Text("SAVE ATTACK PLAN") }
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
                            TextButton(onClick = {
                                val updated = savedPlans.filterIndexed { i, _ -> i != index }
                                val array = JSONArray()
                                updated.forEach { array.put(JSONObject(it)) }
                                prefs.edit().putString(historyKey, array.toString()).apply()
                                savedPlans = updated
                            }) { Text("Delete") }
                        }
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
}
