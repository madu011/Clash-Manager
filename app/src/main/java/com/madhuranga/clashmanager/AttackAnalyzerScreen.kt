package com.madhuranga.clashmanager

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AttackAnalyzerScreen(playerTag: String, screenshotUri: Uri?, onPickScreenshot: () -> Unit) {
    var mode by remember { mutableStateOf("Clan War") }
    var ownTownHall by remember { mutableStateOf("") }
    var targetTownHall by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("3-star") }
    var notes by remember { mutableStateOf("") }
    var prepared by remember { mutableStateOf(false) }
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
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Clan War", "CWL", "Multiplayer", "Friendly").forEach {
                        FilterChip(selected = mode == it, onClick = { mode = it; prepared = false }, label = { Text(it, fontSize = 10.sp) })
                    }
                }
                OutlinedTextField(ownTownHall, { ownTownHall = it.filter(Char::isDigit).take(2) }, label = { Text("Your Town Hall level") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(targetTownHall, { targetTownHall = it.filter(Char::isDigit).take(2) }, label = { Text("Target Town Hall level") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("ATTACK GOAL", color = gold, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("1-star", "2-star", "3-star").forEach {
                        FilterChip(selected = goal == it, onClick = { goal = it; prepared = false }, label = { Text(it) })
                    }
                }
                OutlinedTextField(notes, { notes = it; prepared = false }, label = { Text("Base notes (optional)") }, placeholder = { Text("Defence positions, compartments…") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { onPickScreenshot(); prepared = false }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (screenshotUri == null) "ATTACH BASE SCREENSHOT" else "CHANGE BASE SCREENSHOT")
                }
                Text(if (screenshotUri == null) "No target base image attached." else "Target base screenshot attached.", color = muted, fontSize = 11.sp)
                Button(onClick = { prepared = true }, enabled = ownTownHall.toIntOrNull() in 1..18 && targetTownHall.toIntOrNull() in 1..18 && screenshotUri != null, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color(0xFF10141D))) {
                    Text("PREPARE ANALYSIS", fontWeight = FontWeight.Bold)
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
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("FULL ANALYSIS ROADMAP", color = gold, fontWeight = FontWeight.Bold)
                Text("• Recognise base layout, compartments and visible defences\n• Match available troops, spells, heroes and siege machines to their levels\n• Suggest attack entry, funnel and deployment order\n• Estimate 1-star, 2-star and 3-star outcomes separately with confidence\n• Save plans and results for Clan War, CWL, Multiplayer and Friendly attacks", color = Color.White, fontSize = 12.sp)
                Text("The analyzer must show unknown information clearly instead of guessing.", color = muted, fontSize = 11.sp)
            }
        }
    }
}
