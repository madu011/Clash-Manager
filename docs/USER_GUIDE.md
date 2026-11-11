# Clash Manager — Complete User Guide

**Designed & Developed by Madhuranga Yasanath**

This guide describes the current Android app, how to prepare the information it asks for, and what each visible control does. Features marked **Planned** are not active in the current build.

## 1. Before you begin

Prepare:
- Your Clash of Clans Player Tag.
- Your current Town Hall level.
- The number of builders currently free.
- Names and current levels of upgrades you want to track.
- The remaining duration of each upgrade.
- Optionally, a village JSON export created by the game, if your game version offers that export option.

You do not need to provide a Supercell ID password, email password, verification code, or other login secret. Clash Manager does not need those credentials.

## 2. Find your Player Tag

1. Open Clash of Clans.
2. Open your player profile.
3. Find the player tag displayed with your profile details.
4. Copy it exactly. It normally begins with a #.
5. Return to Clash Manager and paste or type it into **Player Tag**.

The app accepts the tag with or without the leading # and converts letters to uppercase. Check the tag carefully before saving.

## 3. Dashboard and profile card

The profile card is shown at the top of the main screen.

| Control | What it does |
|---|---|
| Player Tag field | Enter or edit the selected player's tag. |
| Save player profile | Stores the tag locally on this device. |
| Profile status | Shows whether a tag is selected and whether online sync is connected. |

A saved tag currently personalises local records only. Upgrade task lists and village-planning inputs are stored separately for each Player Tag on this device, and switching tags loads that profile's saved records. It does not yet fetch a live profile from Supercell.

## 4. Village tab — collect and import a game export

### 4.1 Create the export in Clash of Clans

1. Open Clash of Clans.
2. Open the in-game **Settings**.
3. Look in **More Settings** for a village data export option.
4. Follow the game's own export/save instructions.
5. Note the filename and folder where the game saves the JSON file.

Menu labels and export availability can change between game versions and devices. If the option is absent, do not install an unknown exporter or provide your account credentials to another service. Continue with manual planning.

### 4.2 Import into Clash Manager

1. Save the correct Player Tag in the profile card first.
2. Open the **Village** tab at the bottom.
3. Read the import instructions.
4. Tap **IMPORT VILLAGE JSON**.
5. In Android's file picker, navigate to the saved export.
6. Select the JSON file (the picker may also show text files).
7. Wait for the result summary.

### 4.3 Import rules and results

- A Player Tag must be saved before the import button becomes active.
- The file must be non-empty and valid JSON.
- The maximum accepted file size is 10 MB.
- The current parser expects a JSON object at the top level with at least one recognised section; a top-level JSON array is rejected.
- The app shows a structure preview, import date and approximate file size.
- Imported raw JSON and its summary are stored locally under the selected Player Tag.
- Selecting another Player Tag shows that profile's saved import summary.

**Important:** The parser reads recognised collections and extracts IDs, levels and counts when those fields are present. It also previews other recognised top-level sections. Coverage depends on the exact export schema and the fields included in that file. It does not infer missing levels, positions, resources, or active upgrade timers, and the imported data is not yet a complete live village model. An “imported” message does not mean every village detail was available.

## 5. Complete the Village setup checklist

Open the **Village** tab and follow the **Complete Your Village Setup** checklist:

1. Save your Player Tag in the Dashboard profile card.
2. Import your own village JSON export if your game version offers one.
3. Enter your current Town Hall level in the Village Planner.
4. Enter the number of builders currently available.
5. Add building names and target levels for upgrades you want to plan.
6. Optionally, open the Attack tab and attach a clear base screenshot for your own attack-planning reference.

The checklist tracks the five core setup items locally. The attack screenshot is optional. A completed checklist means the required inputs were supplied; it does not mean online sync or automated image analysis is active.

## 6. Dashboard and Planner — add an upgrade

The Dashboard and Planner currently display the manual upgrade tracker.

1. Tap the **+** floating action button.
2. In **Building / troop name**, type a clear name, such as Archer Tower or a troop name.
3. In **Current level**, enter the level before the upgrade.
4. In **Duration in hours**, enter the remaining duration you want the app to count from now.
5. Select **Builder** for a construction upgrade or **Laboratory** for troop/research work.
6. Tap **Add task**.

The timer starts when you tap Add task. Enter the remaining time, not the time that has already elapsed.

### Upgrade card fields

| Display | Meaning |
|---|---|
| Task name | Building, troop, or research label you entered. |
| Level X → Y | Current level and the next level being tracked. |
| Countdown | Estimated time remaining from the duration entered. |
| Progress bar | Time elapsed divided by the entered duration. |
| Percentage | Approximate timer progress. |
| Mark done | Removes the task from the active list. |

The app does not start or finish an upgrade inside Clash of Clans. It only tracks the timer you entered.

## 7. Builder and Laboratory categories

- **Builder:** Use for buildings, walls, traps, and other construction tasks that use a builder.
- **Laboratory:** Use for troop or spell research tasks.

Choose the category that best represents the in-game task. The category controls which list the task appears in and the card's icon/accent.

## 8. Reminders and Android permissions

When a task is added, the app schedules a reminder for the duration entered.

- Android 13 and newer may ask for notification permission.
- Choose Allow if you want task completion notifications.
- If notifications do not appear, open Android Settings → Apps → Clash Manager → Notifications and check that they are enabled.
- Battery saver, background restrictions, device restart behaviour, or manufacturer-specific power management can affect delivery.
- A reminder is a local estimate based on the duration entered. It is not confirmed by the game server.

## 9. Navigation and visible controls

| Screen/control | Purpose |
|---|---|
| Dashboard | Main manual upgrade overview and Builder/Laboratory lists. |
| Planner | Manual task planning view. |
| Village | Player-linked JSON import and import status. |
| Help | Opens this in-app guide. |
| + button | Opens the Add upgrade task form. |
| Add task | Saves a new timer and schedules its reminder. |
| Cancel | Closes the add-task form without saving. |
| Mark done | Removes a task from the active list. |
| Notifications icon | Visual notification entry point; it is not currently a full notification history screen. |

## 10. Local data, backups, and changing phones

The current version stores the Player Tag, upgrade tasks, and imported JSON locally in app-private preferences on the device. There is no account login or cloud sync yet.

- Keep a separate copy of your original village JSON export.
- Uninstalling the app or clearing its storage may erase local records.
- Moving to another phone does not automatically transfer these records.
- Do not treat the current JSON import as a complete backup/restore feature.

## 11. Troubleshooting

| Problem | What to check |
|---|---|
| Player Tag will not save | Copy it again from your in-game profile; remove spaces and check characters. |
| Import button is disabled | Save a Player Tag first. |
| File picker does not show the file | Check the folder and file extension; copy the export into Downloads if needed. |
| Import failed | Check that the file is non-empty, valid JSON, and at most 10 MB. |
| Import succeeds but some buildings are missing | Only recognised collections and fields present in the export can be parsed. Unknown IDs and missing fields are not guessed. |
| Countdown is incorrect | Remove the task and recreate it using the correct remaining hours. |
| Reminder is missing | Check notification permission and Android battery/background restrictions. |
| Online sync says not connected | Live API sync is planned and is not connected in this build. |

## 12. Current feature status

### Available in this build
- Native Android interface built with Kotlin and Jetpack Compose.
- Local Player Tag selection with separate local planning records per profile.
- Manual Builder and Laboratory upgrade tasks, stored separately by Player Tag.
- Countdown, progress display, and task completion removal.
- Local persistence of tasks.
- Reminder scheduling foundation.
- JSON object validation, recognised collection parsing, nested structure preview, and per-player local storage.
- Village setup checklist for Player Tag, export, Town Hall, builders and upgrade targets.
- Android home-screen village planner widget.

### Planned — not yet available
- Secure backend connection to the official Clash of Clans API.
- Automatic public profile synchronisation.
- Parsing imported village JSON into recognised building, wall, trap, troop, and upgrade records.
- Accurate Town Hall maximum-level catalogue and automatic rushed-village detection.
- Resource and builder optimisation recommendations.
- Automated upgrade priority recommendations.
- Base layout studio and visual base planning.

## 13. Data accuracy and account safety

The official Clash of Clans API is read-only and does not expose every live village detail or active countdown. The app must distinguish imported, API-sourced, and manually entered information. Missing values should be shown as unavailable rather than guessed.

Never share your Supercell ID password, email password, one-time verification codes, or API credentials. Any future API token must be held securely on a backend and must never be placed in the Android app or public repository.

---

**Support note:** If a control or screen in your installed version differs from this guide, note the app version and the exact screen title when reporting the issue.
