# Clash Manager Roadmap

Developer: **Madhuranga Yasanath**

## Product direction
Clash Manager is an online-first companion app for any Clash of Clans player. The app combines official public player-profile data with an optional player-initiated in-game village JSON export. It must never imply that a Player Tag alone reveals the complete village.

## Phase 1 — Android foundation
- [x] Native Compose dashboard and player tag entry
- [x] Builder and laboratory countdown UI prototype
- [x] GitHub Actions debug APK workflow
- [x] Initial in-game village JSON file picker and local raw-file preservation
- [x] Separate imported export storage keys per selected player tag
- [ ] Validate the actual exported JSON schema using sample exports from different game versions
- [ ] Parse all supported export fields into typed building, trap, wall, upgrade, hero, troop, spell, pet, and equipment models
- [ ] Add export replacement, clear, and import history controls

## Phase 2 — Online player profile
- [ ] Secure backend service for Supercell API requests
- [ ] Keep official API token only as backend secret; never in APK or repository
- [ ] Fetch player profile by tag and refresh on demand / scheduled cadence
- [ ] Map supported Town Hall, heroes, troops, spells, equipment, achievements, and profile fields
- [ ] Show last successful sync, loading, invalid-tag, rate-limit, and service-error states
- [ ] Keep profiles and imported data isolated by player tag

## Phase 3 — Automatic village analysis
- [ ] Merge official API data with the player's imported village export
- [ ] Parse building inventory and levels, walls, traps, active upgrades, and other fields only when present in the export schema
- [ ] Track provenance per field: Official API, In-game Export, or unavailable
- [ ] Detect rush gaps using versioned Town Hall maximum-level data
- [ ] Generate explainable upgrade priorities, builder/lab plans, resource targets, and recovery progress
- [ ] Recalculate after a successful sync or new export import
- [ ] Never invent missing values or claim the app can read live timers that neither source provides

## Phase 4 — Game-inspired interface
- [ ] Create an original fantasy strategy UI inspired by the visual language of village-building games: stone/wood panels, bright resource accents, bold upgrade buttons, shield/building/unit category icons, progress bars, and compact status badges
- [ ] Design original icons and illustrations for Dashboard, Village, Army, Laboratory, Builders, Resources, Base Layouts, and Settings
- [ ] Use consistent button states, touch targets, accessible contrast, and clear labels
- [ ] Do not copy Supercell logos, proprietary artwork, exact game screenshots, or extracted game assets; use original assets and respect the Fan Content Policy

## Phase 5 — Base design studio
- [ ] Visual grid editor and building inventory driven by imported base data
- [ ] War, farming, trophy, and hybrid layout guides
- [ ] Export a supported in-game layout link only where the game format allows it
- [ ] Otherwise provide a visual placement guide

## Phase 6 — Beginner-friendly experience
- [x] Add a first-use dashboard quick-start card with direct links to Village setup, upgrade tracking, and Help
- [x] Explain Attack planning limitations before users enter attack details
- [ ] Replace technical labels and unexplained abbreviations with plain-language labels throughout every screen
- [ ] Add a first-run guided walkthrough that can be skipped and reopened from Help
- [ ] Add consistent empty states, inline examples, validation messages, and recovery actions to every form
- [ ] Add accessible touch targets, screen-reader descriptions, scalable text, and contrast checks
- [ ] Localize the complete interface and help content (English, Sinhala, Serbian first)
- [ ] Run usability checks with new players and revise confusing flows

## Phase 7 — In-app updates and game data packs
- [x] Define and validate a signed-release/data-pack manifest contract
- [x] Add tests for trusted update URLs and SHA-256 metadata
- [ ] Set up a stable protected Android release signing key in GitHub Actions secrets
- [ ] Publish signed APKs and checksums as versioned GitHub Releases
- [ ] Check update manifest on launch and on-demand from Settings
- [ ] Notify only when a newer compatible app version is available
- [ ] Download APK in-app, verify checksum, and launch Android's install confirmation
- [ ] Publish versioned game-data packs independently from APK releases
- [ ] Download, validate, safely activate, and roll back game-data packs
- [ ] Show update history and a simple changelog in the app

## Phase 6 — Quality and release
- [ ] Unit/UI tests including malformed and older export files
- [ ] Backup and restore
- [ ] Accessibility and localization
- [ ] Signed release APK/AAB
- [ ] Privacy policy, data retention, and API compliance review

## Data and API limitations
The official Clash of Clans Player API returns profile progression including Town Hall, troops, heroes, spells, and hero equipment, but does not provide a complete live base snapshot. Public references describe the in-game **Settings → More Settings → Export village data** JSON export as the route for additional village state, including buildings, traps, walls, and ongoing upgrades. The exact export schema must be verified against real files and supported game versions before enabling automated scoring.

The current import UI only selects a JSON file, validates that it is a JSON object, stores the raw content locally under the selected player profile, and shows a structural summary. It does **not yet parse building levels or feed the analyzer**. Do not claim that full import-based analysis is complete until schema-specific parsing and tests are implemented.

The official API is read-only. Never request Supercell ID passwords or account credentials. Never automate gameplay, modify game memory, or claim the app can place buildings or start upgrades. Keep API tokens on the backend and restrict them to the backend's allowed IP address.
