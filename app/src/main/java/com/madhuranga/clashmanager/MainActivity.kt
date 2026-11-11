package com.madhuranga.clashmanager

import android.os.Bundle
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

private val Page = Color(0xFF10141D)
private val Panel = Color(0xFF1A2230)
private val Gold = Color(0xFFFFC857)
private val Blue = Color(0xFF54B8F5)
private val Muted = Color(0xFF9AA8BA)

data class UpgradeTask(
    val id: Long,
    val title: String,
    val fromLevel: Int,
    val toLevel: Int,
    val startedAt: Long,
    val durationMs: Long,
    val category: String = "Builder"
) {
    fun remaining(now: Long) = (startedAt + durationMs - now).coerceAtLeast(0L)
    fun progress(now: Long) = if (durationMs <= 0) 1f else
        ((now - startedAt).toFloat() / durationMs).coerceIn(0f, 1f)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ClashManagerApp() }
    }
}

@Composable
fun ClashManagerApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("clash_manager", 0) }
    val installedGameDataPack = remember {
        runCatching {
            val packFile = java.io.File(context.filesDir, "game-data/active-pack.json")
            if (packFile.isFile) {
                val rawPack = packFile.readText(Charsets.UTF_8)
                val version = JSONObject(rawPack).getInt("dataPackVersion")
                GameDataPackParser.parse(rawPack, version)
            } else null
        }.getOrNull()
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    var playerTag by remember { mutableStateOf(prefs.getString("player_tag", "") ?: "") }
    val taskStorageKey = "tasks_${playerTag.ifBlank { "unselected" }}"
    var tasks by remember(taskStorageKey) {
        mutableStateOf(
            loadTasks(
                prefs.getString(
                    taskStorageKey,
                    if (playerTag.isBlank()) prefs.getString("tasks", "[]") ?: "[]" else "[]"
                ) ?: "[]"
            )
        )
    }
    var tagInput by remember { mutableStateOf(playerTag) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showAdd by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf("Dashboard") }
    val profileKey = playerTag.ifBlank { "unselected" }
    var thLevel by remember(profileKey) {
        mutableStateOf(prefs.getString("town_hall_level_$profileKey", "") ?: "")
    }
    var freeBuilders by remember(profileKey) {
        mutableStateOf(prefs.getString("available_builders_$profileKey", "0") ?: "0")
    }
    var rushedBuildings by remember(profileKey) {
        mutableStateOf(prefs.getString("rushed_buildings_$profileKey", "") ?: "")
    }
    var buildingTargets by remember(profileKey) {
        mutableStateOf(prefs.getString("building_targets_$profileKey", "") ?: "")
    }
    var exportSummary by remember { mutableStateOf(prefs.getString("village_export_summary_${playerTag.ifBlank { "unselected" }}", "") ?: "") }
    var exportDetails by remember { mutableStateOf(prefs.getString("village_export_details_${playerTag.ifBlank { "unselected" }}", "") ?: "") }
    var exportCollections by remember(profileKey) { mutableStateOf(prefs.getString("village_export_collections_$profileKey", "") ?: "") }
    var exportError by remember { mutableStateOf("") }
    var attackScreenshot by remember(profileKey) {
        mutableStateOf(prefs.getString("attack_screenshot_uri_$profileKey", null)?.let(Uri::parse))
    }
    val pickAttackScreenshot = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val permissionPersisted = runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }.isSuccess
            attackScreenshot = uri
            if (permissionPersisted) {
                prefs.edit().putString("attack_screenshot_uri_$profileKey", uri.toString()).apply()
            } else {
                prefs.edit().remove("attack_screenshot_uri_$profileKey").apply()
            }
        }
    }
    LaunchedEffect(playerTag) {
        exportSummary = prefs.getString("village_export_summary_${playerTag.ifBlank { "unselected" }}", "") ?: ""
        exportDetails = prefs.getString("village_export_details_${playerTag.ifBlank { "unselected" }}", "") ?: ""
        exportCollections = prefs.getString("village_export_collections_${playerTag.ifBlank { "unselected" }}", "") ?: ""
        exportError = ""
    }
    val importVillageExport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.use { readUtf8Limited(it, 10 * 1024 * 1024) }
                    ?: error("Could not read the selected file")
                require(raw.isNotBlank()) { "The selected file is empty" }
                require(raw.toByteArray(Charsets.UTF_8).size <= 10 * 1024 * 1024) { "File is too large. Maximum supported size is 10 MB" }
                val parsedExport = VillageExportParser.parse(raw)
                val exportedTag = parsedExport.playerTag?.let(PlayerTagValidator::normalize)
                val selectedTag = PlayerTagValidator.normalize(playerTag)
                require(exportedTag == null || exportedTag == selectedTag) {
                    "This export belongs to ${parsedExport.playerTag}, not the selected profile $playerTag"
                }
                val summary = "Village export parsed • ${parsedExport.categories.size} collections • " + parsedExport.summary()
                val coverage = VillageExportCoverageAnalyzer.analyze(parsedExport)
                val collectionLines = "COVERAGE REPORT\n${coverage.asMessage()}\n\n" + parsedExport.sections.joinToString("\n") { section ->
                    "${section.key}: ${section.entryCount} ${section.shape}"
                } + "\n" + parsedExport.categories.joinToString("\n") { category ->
                    val itemLabels = category.items.map { item ->
                        val name = GameDataCatalog.nameFor(item.dataId, category.key, installedGameDataPack) ?: "ID ${item.dataId}"
                        val level = item.level?.let { " Lv.$it" } ?: ""
                        "$name$level ×${item.count}"
                    }
                    "  ${category.key}: ${category.items.size} unique IDs • ${category.totalCount} total items" +
                        if (itemLabels.isEmpty()) "" else "\n    " + itemLabels.joinToString(", ")
                }
                val details = "Imported ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date())} • ${(raw.toByteArray(Charsets.UTF_8).size / 1024).coerceAtLeast(1)} KB"
                prefs.edit()
                    .putString("village_export_json_${playerTag.ifBlank { "unselected" }}", raw)
                    .putString("village_export_summary_${playerTag.ifBlank { "unselected" }}", summary)
                    .putString("village_export_details_${playerTag.ifBlank { "unselected" }}", details)
                    .putString("village_export_collections_${playerTag.ifBlank { "unselected" }}", collectionLines)
                    .apply()
                exportSummary = summary
                exportDetails = details
                exportCollections = collectionLines
                exportError = ""
            }.onFailure { exportError = "Import failed: ${it.message ?: "Invalid JSON file"}" }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    LaunchedEffect(taskStorageKey, tasks) {
        prefs.edit().putString(taskStorageKey, saveTasks(tasks)).apply()
    }
    LaunchedEffect(profileKey, thLevel, freeBuilders, rushedBuildings, buildingTargets) {
        prefs.edit()
            .putString("town_hall_level_$profileKey", thLevel)
            .putString("available_builders_$profileKey", freeBuilders)
            .putString("rushed_buildings_$profileKey", rushedBuildings)
            .putString("building_targets_$profileKey", buildingTargets)
            .apply()
        VillagePlannerWidgetProvider.refreshAll(context)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            secondary = Blue,
            background = Page,
            surface = Panel
        )
    ) {
        Scaffold(containerColor = Page, bottomBar = {
            NavigationBar(containerColor = Panel) {
                listOf("Dashboard", "Planner", "Attack", "Village", "Help").forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(Icons.Default.Construction, contentDescription = null) },
                        label = { Text(item) }
                    )
                }
            }
        }, floatingActionButton = {
            if (tab == "Dashboard" || tab == "Planner") {
                FloatingActionButton(onClick = { showAdd = true }, containerColor = Gold, contentColor = Page) {
                    Icon(Icons.Default.Add, contentDescription = "Add upgrade")
                }
            }
        }) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("CLASH MANAGER", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text("Village Command Center", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Designed & Developed by Madhuranga Yasanath", color = Muted, fontSize = 11.sp)
                        }
                        Surface(shape = CircleShape, color = Panel) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Gold, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("YOUR VILLAGE PROFILE", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                            Text("Enter any player's tag to personalise this device's village planner.", color = Muted, fontSize = 12.sp)
                            OutlinedTextField(
                                value = tagInput,
                                onValueChange = { value -> tagInput = PlayerTagValidator.normalize(value) },
                                label = { Text("Player Tag (e.g. #ABC123)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { playerTag = tagInput.trim(); prefs.edit().putString("player_tag", playerTag).apply() },
                                enabled = tagInput.isBlank() || PlayerTagValidator.isValid(tagInput)
                            ) { Text("Save player profile") }
                            Text(if (playerTag.isBlank()) "No player selected • manual tracker mode" else "Saved locally: $playerTag • live sync not connected yet", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
                if (tab == "Dashboard" && (playerTag.isBlank() || tasks.isEmpty())) {
                    item {
                        QuickStartCard(
                            hasPlayerTag = playerTag.isNotBlank(),
                            hasTasks = tasks.isNotEmpty(),
                            onOpenVillage = { tab = "Village" },
                            onOpenPlanner = { tab = "Planner" },
                            onOpenHelp = { tab = "Help" }
                        )
                    }
                }
                if (tab == "Attack") {
                    item {
                        Text("ATTACK PLANNING", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Add the target details and a screenshot to prepare your own attack notes. Automated image analysis is not active yet.", color = Muted, fontSize = 13.sp)
                        AttackAnalyzerScreen(playerTag, attackScreenshot) { pickAttackScreenshot.launch(arrayOf("image/*")) }
                    }
                }
                if (tab == "Village") {
                    item {
                        VillageSetupGuide(
                            hasPlayerTag = playerTag.isNotBlank(),
                            hasVillageExport = exportSummary.isNotBlank(),
                            hasTownHall = (thLevel.toIntOrNull() ?: 0) in 1..18,
                            hasBuilderCount = (freeBuilders.toIntOrNull() ?: -1) in 0..6,
                            hasUpgradeTargets = buildingTargets.isNotBlank(),
                            hasAttackScreenshot = attackScreenshot != null
                        )
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("HOME SCREEN WIDGET", color = Gold, fontWeight = FontWeight.Bold)
                                Text("Add your village upgrade summary to your Android home screen.", color = Muted, fontSize = 12.sp)
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val manager = AppWidgetManager.getInstance(context)
                                            val provider = ComponentName(context, VillagePlannerWidgetProvider::class.java)
                                            if (manager.isRequestPinAppWidgetSupported) {
                                                manager.requestPinAppWidget(provider, null, null)
                                            } else {
                                                android.widget.Toast.makeText(context, "Add Clash Manager from your launcher's Widgets menu.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("ADD TO HOME SCREEN") }
                            }
                        }
                    }
                    item {
                        VillageRecommendationsScreen(
                            playerTag = playerTag,
                            townHallLevel = thLevel,
                            onTownHallLevelChange = { thLevel = it },
                            availableBuilders = freeBuilders,
                            onAvailableBuildersChange = { freeBuilders = it },
                            rushedBuildings = rushedBuildings,
                            onRushedBuildingsChange = { rushedBuildings = it },
                            buildingTargets = buildingTargets,
                            onBuildingTargetsChange = { buildingTargets = it }
                        )
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Construction, contentDescription = null, tint = Gold, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(9.dp))
                                    Column {
                                        Text("VILLAGE DATA IMPORT", color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                        Text("Import your own in-game village export", color = Muted, fontSize = 12.sp)
                                    }
                                }
                                Text("In Clash of Clans, open Settings → More Settings → Export village data. Then select the exported JSON file below.", color = Color.White, fontSize = 13.sp)
                                if (playerTag.isBlank()) Text("Save a Player Tag first to keep this export linked to the correct profile.", color = Gold, fontSize = 12.sp)
                                Button(
                                    onClick = { importVillageExport.launch(arrayOf("application/json", "text/json", "text/plain")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = playerTag.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Page),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("IMPORT VILLAGE JSON", fontWeight = FontWeight.Bold)
                                }
                                if (exportSummary.isNotBlank()) {
                                    Surface(color = Color(0xFF18372D), shape = RoundedCornerShape(12.dp)) {
                                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text("EXPORT IMPORTED", color = Color(0xFF8DE0A8), fontWeight = FontWeight.Bold)
                                            if (exportDetails.isNotBlank()) Text(exportDetails, color = Muted, fontSize = 10.sp)
                                            Text(exportSummary, color = Color.White, fontSize = 12.sp)
                                            if (exportCollections.isNotBlank()) {
                                                Divider(color = Color(0xFF315545))
                                                Text("DETECTED VILLAGE DATA", color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                exportCollections.lines().forEach { line -> Text(line, color = Color.White, fontSize = 11.sp) }
                                            }
                                        }
                                    }
                                }
                                if (exportError.isNotBlank()) Text(exportError, color = Color(0xFFFF8A80), fontSize = 12.sp)
                                Text("The original export is kept locally on this device and linked to the selected player profile. The app will only use fields it can recognise; unknown fields are preserved for later parser improvements.", color = Muted, fontSize = 11.sp)
                            }
                        }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("AUTOMATIC ONLINE PROFILE", color = Blue, fontWeight = FontWeight.Bold)
                                Text(if (playerTag.isBlank()) "Add a Player Tag above to prepare online profile sync." else "Selected profile: $playerTag")
                                Text("Online sync is being built. Official API data and imported village-export data will be combined; fields not supplied by either source will remain marked unavailable.", color = Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (tab == "Help") {
                    item { HelpGuide() }
                }
                if (tab != "Village" && tab != "Help" && tab != "Attack") {
item {
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("BUILDER LOG", color = Blue, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            val active = tasks.count { it.remaining(now) > 0 && it.category == "Builder" }
                            Text("$active ACTIVE TASKS", color = Muted, fontSize = 12.sp)
                            if (tasks.isEmpty()) {
                                Text("No upgrades added yet. Tap + to add your first builder task.", color = Color.White)
                            }
                        }
                    }
                }
                val builders = tasks.filter { it.category == "Builder" }
                items(builders, key = { it.id }) { task ->
                    UpgradeCard(task, now) {
                        WorkManager.getInstance(context).cancelUniqueWork("upgrade-${task.id}")
                        tasks = tasks.filterNot { it.id == task.id }
                    }
                }
                item {
                    Text("LABORATORY", color = Blue, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
                items(tasks.filter { it.category == "Laboratory" }, key = { it.id }) { task ->
                    UpgradeCard(task, now) {
                        WorkManager.getInstance(context).cancelUniqueWork("upgrade-${task.id}")
                        tasks = tasks.filterNot { it.id == task.id }
                    }
                }
                item {
                    Text("QUICK PLANNER", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("Add your current upgrades to track countdowns. Recommendations will be enabled after your village data is connected.", color = Muted, fontSize = 13.sp)
                }
                }
            }
        }
    }

    if (showAdd) {
        AddUpgradeDialog(
            onDismiss = { showAdd = false },
            onAdd = { title, level, hours, category ->
                val duration = hours * 60L * 60L * 1000L
                val taskId = System.currentTimeMillis()
                val task = UpgradeTask(
                    id = taskId, title = title, fromLevel = level, toLevel = level + 1,
                    startedAt = taskId, durationMs = duration, category = category
                )
                tasks = tasks + task
                val reminder = OneTimeWorkRequestBuilder<UpgradeReminderWorker>()
                    .setInitialDelay(duration, TimeUnit.MILLISECONDS)
                    .setInputData(Data.Builder().putLong("task_id", taskId).putString("task_title", title).build())
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "upgrade-$taskId", ExistingWorkPolicy.REPLACE, reminder
                )
                showAdd = false
            }
        )
    }
}

@Composable
private fun QuickStartCard(
    hasPlayerTag: Boolean,
    hasTasks: Boolean,
    onOpenVillage: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenHelp: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("WELCOME TO CLASH MANAGER", color = Gold, fontWeight = FontWeight.ExtraBold)
            Text("Start with these simple steps. You can change your details later.", color = Muted, fontSize = 13.sp)
            SetupGuideRow("1. Save your Player Tag", hasPlayerTag, "Use the profile box above.")
            SetupGuideRow("2. Set up your village", false, "Add Town Hall, builders and optional village data.")
            SetupGuideRow("3. Track your first upgrade", hasTasks, "Add a task and enter the remaining time.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenVillage, modifier = Modifier.weight(1f)) { Text("Village setup") }
                Button(onClick = onOpenPlanner, modifier = Modifier.weight(1f)) { Text("Add upgrade") }
            }
            TextButton(onClick = onOpenHelp) { Text("Need help? Open the simple guide") }
        }
    }
}

@Composable
private fun VillageSetupGuide(
    hasPlayerTag: Boolean,
    hasVillageExport: Boolean,
    hasTownHall: Boolean,
    hasBuilderCount: Boolean,
    hasUpgradeTargets: Boolean,
    hasAttackScreenshot: Boolean
) {
    val completed = listOf(hasPlayerTag, hasVillageExport, hasTownHall, hasBuilderCount, hasUpgradeTargets).count { it }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("COMPLETE YOUR VILLAGE SETUP", color = Gold, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text("Add the information below to unlock the most useful planning and analysis this version supports.", color = Muted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$completed / 5 core steps complete", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${completed * 20}%", color = Gold, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { completed / 5f },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = Gold,
                    trackColor = Color(0xFF354052)
                )
                SetupGuideRow("1. Save your Player Tag", hasPlayerTag, "Enter it in the profile card on Dashboard.")
                SetupGuideRow("2. Import your village JSON", hasVillageExport, "Use Village Data Import if your game provides an export.")
                SetupGuideRow("3. Enter Town Hall level", hasTownHall, "Open Village Planner and enter your current Town Hall.")
                SetupGuideRow("4. Enter available builders", hasBuilderCount, "Set how many builders are currently available.")
                SetupGuideRow("5. Add upgrade target levels", hasUpgradeTargets, "Enter the buildings and target levels you want to plan.")
                Divider(color = Color(0xFF354052))
                SetupGuideRow("Optional: add an attack screenshot", hasAttackScreenshot, "Open Attack tab and attach a clear base screenshot.")
                Text("Only data actually present in your JSON or entered by you can be used. Missing values stay unknown; Clash Manager does not need your Supercell ID password or verification codes.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun SetupGuideRow(title: String, complete: Boolean, detail: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(if (complete) "✓" else "○", color = if (complete) Color(0xFF8DE0A8) else Gold, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(if (complete) "Saved or detected on this device" else detail, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun HelpGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PLAYER USER GUIDE", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("A step-by-step guide to setting up and using the features currently available in Clash Manager.", color = Muted, fontSize = 12.sp)
        HelpSection("1. First setup", "Open Dashboard and find YOUR VILLAGE PROFILE. Enter your Clash of Clans Player Tag, then tap Save player profile. You can find your tag in the game by opening your player profile and copying the tag shown below your player name. Enter the tag with or without #. The saved tag is stored on this device. You can use the app's manual planner even before connecting online services.")
        HelpSection("2. What information should I prepare?", "Have your Player Tag ready. For accurate manual planning, know your Town Hall level, how many builders are free, which buildings are behind, and the names, current levels and remaining durations of upgrades you want to track. If you want to test village import, prepare the JSON export created by the game, if that export option is available in your game version. Never share your Supercell ID password or verification codes.")
        HelpSection("3. Export village data from the game", "In Clash of Clans, open Settings and look under More Settings for the village data export option. Follow the game's own export/save flow and remember where the JSON file is saved. The exact menu wording and availability may vary by game version and device. If you cannot find an export option, do not download an unknown file or give another app your account login; use manual tracking for now.")
        HelpSection("4. Import your JSON file", "First save a Player Tag in the profile card. Open the Village tab, read the import note, and tap IMPORT VILLAGE JSON. In the Android file picker, browse to the folder where you saved the game's export and select the JSON or text file. A successful import shows a summary, import date and file size. Empty files, invalid JSON and files larger than 10 MB are rejected. Each saved Player Tag has its own local import record. Selecting a different tag changes which profile's import summary is shown.")
        HelpSection("5. Understand the import result", "The importer recognises selected collections and extracts item IDs, levels and counts when those fields are present. Other recognised sections are shown as a structure preview. Exact coverage depends on the export schema and its contents. Missing values and unknown IDs are not guessed, and the import is not yet a complete live village model. Keep the original export so it can be imported again if needed.")
        HelpSection("6. Dashboard and upgrade cards", "Dashboard shows the builder task list and laboratory task list. Tap the + button to add an upgrade. Enter a building, troop or research name, its current level, the duration in hours, and choose Builder or Laboratory. Tap Add task. The card shows the level transition, remaining time and estimated progress. Tap Mark done when the task is finished to remove it from the active list.")
        HelpSection("7. Planner tab", "The Planner tab currently shares the manual upgrade tracker view. Add tasks with the + button and use the Builder and Laboratory categories to keep construction and research separate. Enter the duration remaining from the moment you add the task; the countdown starts when you tap Add task. If the real in-game timer differs, remove the task and add it again with the corrected duration.")
        HelpSection("8. Reminders and notifications", "The app schedules a reminder for the duration entered when a task is created. On Android 13 and newer, allow notification permission when asked so reminders can be shown. Check Android Settings → Apps → Clash Manager → Notifications if alerts are disabled. Battery saver or device restrictions may delay notifications.")
        HelpSection("9. Saving and privacy", "Player Tag, upgrade tasks and imported JSON are stored locally in this app's private device preferences. Upgrade task lists and imported exports are kept separately for each saved Player Tag, so switching profiles loads that profile's own tasks. They are not currently synced to a server or another phone. Uninstalling the app or clearing its storage may remove this local data. Keep your original JSON export separately. Do not enter passwords, Supercell ID codes or other account secrets.")
        HelpSection("10. Online profile sync and analysis", "The online profile panel is a future integration placeholder. Live profile sync and automatic recommendations are not connected in this version. The official game API is read-only and does not provide every live village detail, such as all building positions or active builder countdowns. The app must label unavailable information rather than guess.")
        HelpSection("11. Common problems", "Player Tag not accepted: check the tag from your in-game profile and remove spaces. Import button disabled: save a Player Tag first. Import failed: make sure the file is a real, non-empty JSON export and is no larger than 10 MB. No reminder: check notification permission and device battery restrictions. Wrong countdown: recreate the task with the correct remaining hours.")
        HelpSection("12. Current feature status", "Available now: local Player Tag profile, manual Builder and Laboratory task tracking, countdowns, progress display, local saving, reminder scheduling foundation, and JSON file validation/structure preview. Not available yet: live API sync, automatic village parsing, rush detection from imported data, resource calculations, AI upgrade recommendations, and base-layout generation.")
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(body, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun UpgradeCard(task: UpgradeTask, now: Long, onComplete: () -> Unit) {
    val remaining = task.remaining(now)
    val progress = task.progress(now)
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (task.category == "Laboratory") Icons.Default.FlashOn else Icons.Default.Construction,
                    contentDescription = null, tint = Gold)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(task.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Level ${task.fromLevel} → ${task.toLevel}", color = Muted, fontSize = 12.sp)
                }
                Text(if (remaining == 0L) "READY" else formatTime(remaining), color = Gold, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = if (task.category == "Laboratory") Blue else Gold,
                trackColor = Color(0xFF354052)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${(progress * 100).toInt()}% complete", color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Text(" Mark done")
                }
            }
        }
    }
}

@Composable
private fun AddUpgradeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }
    var hours by remember { mutableStateOf("24") }
    var category by remember { mutableStateOf("Builder") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add upgrade task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the upgrade details exactly as shown in your game. The timer starts when you tap Add task.", color = Muted, fontSize = 12.sp)
                OutlinedTextField(name, { name = it }, label = { Text("Building / troop / spell name") }, supportingText = { Text("Example: Archer Tower or Laboratory research") }, singleLine = true)
                OutlinedTextField(level, { level = it.filter(Char::isDigit).take(3) }, label = { Text("Current level") }, supportingText = { Text("The task will be recorded as the next level") }, singleLine = true)
                OutlinedTextField(hours, { hours = it.filter(Char::isDigit).take(5) }, label = { Text("Remaining duration (hours)") }, supportingText = { Text("Use the time remaining, not the original full duration") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = category == "Builder", onClick = { category = "Builder" }, label = { Text("Builder") })
                    FilterChip(selected = category == "Laboratory", onClick = { category = "Laboratory" }, label = { Text("Laboratory") })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && (level.toIntOrNull() ?: 0) > 0 && (hours.toIntOrNull() ?: 0) in 1..87600,
                onClick = { onAdd(name.trim(), level.toIntOrNull() ?: 1, hours.toIntOrNull() ?: 24, category) }
            ) { Text("Add task") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return if (days > 0) "${days}d ${hours}h" else "${hours}h ${minutes}m"
}

private fun loadTasks(raw: String): List<UpgradeTask> = runCatching {
    val array = JSONArray(raw)
    (0 until array.length()).map { i ->
        val o = array.getJSONObject(i)
        UpgradeTask(
            id = o.getLong("id"), title = o.getString("title"),
            fromLevel = o.getInt("from"), toLevel = o.getInt("to"),
            startedAt = o.getLong("started"), durationMs = o.getLong("duration"),
            category = o.optString("category", "Builder")
        )
    }
}.getOrDefault(emptyList())

private fun saveTasks(tasks: List<UpgradeTask>): String {
    val array = JSONArray()
    tasks.forEach { t ->
        array.put(JSONObject().apply {
            put("id", t.id); put("title", t.title)
            put("from", t.fromLevel); put("to", t.toLevel)
            put("started", t.startedAt); put("duration", t.durationMs)
            put("category", t.category)
        })
    }
    return array.toString()
}
private fun summarizeVillageExport(root: JSONObject): String {
    val fields = root.keys().asSequence().toList()
    val structure = mutableListOf<String>()
    collectVillageStructure(root, "\\$", structure, 0)
    val preview = structure.take(10).joinToString(" • ")
    return "Valid JSON object imported • ${fields.size} root fields" +
        if (preview.isBlank()) "" else " • $preview"
}

private fun summarizeVillageExport(root: JSONArray): String {
    val structure = mutableListOf<String>()
    collectVillageStructure(root, "$", structure, 0)
    val preview = structure.take(10).joinToString(" • ")
    return "Valid JSON array imported • ${root.length()} entries" +
        if (preview.isBlank()) "" else " • $preview"
}

/** Read-only structural inspection. It does not assume undocumented game-export field names. */
private fun collectVillageStructure(
    value: Any?,
    path: String,
    output: MutableList<String>,
    depth: Int
) {
    if (depth > 4 || output.size >= 20) return
    when (value) {
        is JSONObject -> {
            val keys = value.keys().asSequence().toList()
            if (keys.isNotEmpty()) output += "$path: ${keys.size} fields"
            keys.take(20).forEach { key ->
                if (output.size < 20) collectVillageStructure(value.opt(key), "$path.$key", output, depth + 1)
            }
        }
        is JSONArray -> {
            output += "$path: ${value.length()} entries"
            val firstObject = (0 until minOf(value.length(), 5))
                .firstNotNullOfOrNull { value.optJSONObject(it) }
            if (firstObject != null) collectVillageStructure(firstObject, "$path[]", output, depth + 1)
        }
    }
}
