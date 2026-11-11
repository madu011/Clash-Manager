# Clash Manager Roadmap

Developer: **Madhuranga Yasanath**

## Product direction
Clash Manager is an **online-first, automatic village analysis app** for any Clash of Clans player. A player enters their own Player Tag; the app fetches the public profile data available through an approved API, then generates and refreshes upgrade and recovery recommendations. Do not present manually entered village fields as a substitute for automatic syncing.

## Phase 1 — Android foundation
- [x] Native Compose dashboard
- [x] Player tag profile entry (local profile selection only; not live sync)
- [x] Builder and laboratory countdown UI prototype
- [x] GitHub Actions debug APK workflow
- [ ] Replace the current offline Village Analyzer screen with online loading, sync, error, and data-coverage states
- [ ] Account/profile data refresh and caching policy
- [ ] Unit and UI tests

## Phase 2 — Online player data
- [ ] Create a secure backend service for Supercell API requests
- [ ] Configure the official API token as a backend secret; never embed it in the APK or commit it
- [ ] Validate and normalize player tags on the backend
- [ ] Fetch player profile automatically by tag
- [ ] Fetch and map API-provided Town Hall, heroes, troops, spells, hero equipment, achievements, and other supported fields
- [ ] Show last sync time, refresh state, and clear errors for invalid tags, rate limits, or service outages
- [ ] Keep player data isolated by profile; do not hard-code any player's tag
- [ ] Clearly disclose data the official API does not expose

## Phase 3 — Automatic analysis and planning
- [ ] Build a versioned game-data catalogue for upgrade levels, Town Hall caps, costs, and durations
- [ ] Analyze API-provided hero, troop, spell, and equipment progression automatically
- [ ] Generate rush/recovery scoring with explainable priorities
- [ ] Build resource and upgrade plans using verified game data
- [ ] Recalculate recommendations after each successful sync or game-data update
- [ ] Mark unavailable building/base fields as unknown rather than guessing
- [ ] Support optional in-game village-data export import for building levels and running upgrades, if the player chooses to provide it

## Phase 4 — Timers and notifications
- [ ] Builder, laboratory, and hero timer planning where reliable start/duration data is available
- [ ] Reminders and completion notifications
- [ ] Clearly distinguish user-confirmed timers from API-sourced data
- [ ] Reconcile timers after device restart and app reinstall

## Phase 5 — Base design studio
- [ ] Import supported base links / layouts where technically possible
- [ ] Visual grid editor and building inventory
- [ ] Layout templates for war, farming, trophy, and hybrid
- [ ] Validate building inventory only when the required base data is available
- [ ] Export a supported in-game layout link only where the game format allows it
- [ ] Otherwise export a visual placement guide

## Phase 6 — Quality and release
- [ ] Backup and restore
- [ ] Accessibility and localization
- [ ] Automated tests
- [ ] Signed release APK / AAB
- [ ] Privacy policy, data retention rules, and API compliance review

## Important API limitations and security
The official Clash of Clans API is read-only. Player-tag lookups can provide profile information such as Town Hall level, heroes, troops, spells, and hero equipment, but do not provide a complete live village snapshot. In particular, building levels, walls, traps, current resources, and live builder/research countdowns are not all available from the public player endpoint. Do not fabricate missing values or claim full automatic base scanning from a tag alone.

If deeper base data is needed, investigate an optional player-initiated in-game village-data export/import flow and explain exactly which fields it supplies. Never ask for a Supercell ID password or account credentials. Never automate gameplay, modify game memory, or claim the app can place buildings or start upgrades. The official API token must stay on the backend and be restricted to the backend's allowed IP address.
