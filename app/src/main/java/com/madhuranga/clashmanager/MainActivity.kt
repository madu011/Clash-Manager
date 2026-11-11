package com.madhuranga.clashmanager

import android.os.Bundle
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
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    var tasks by remember {
        mutableStateOf(loadTasks(prefs.getString("tasks", "[]") ?: "[]"))
    }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showAdd by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf("Dashboard") }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    LaunchedEffect(tasks) {
        prefs.edit().putString("tasks", saveTasks(tasks)).apply()
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
                listOf("Dashboard", "Planner", "Village").forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(Icons.Default.Construction, contentDescription = null) },
                        label = { Text(item) }
                    )
                }
            }
        }, floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Gold, contentColor = Page) {
                Icon(Icons.Default.Add, contentDescription = "Add upgrade")
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
                item {
                    Text("Data source: device tracker • Player tag: not connected", color = Muted, fontSize = 11.sp)
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
                OutlinedTextField(name, { name = it }, label = { Text("Building / troop name") }, singleLine = true)
                OutlinedTextField(level, { level = it.filter(Char::isDigit) }, label = { Text("Current level") }, singleLine = true)
                OutlinedTextField(hours, { hours = it.filter(Char::isDigit) }, label = { Text("Duration in hours") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = category == "Builder", onClick = { category = "Builder" }, label = { Text("Builder") })
                    FilterChip(selected = category == "Laboratory", onClick = { category = "Laboratory" }, label = { Text("Laboratory") })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && (hours.toIntOrNull() ?: 0) > 0,
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