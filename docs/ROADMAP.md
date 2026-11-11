# Clash Manager Roadmap

Developer: **Madhuranga Yasanath**

## Phase 1 — Android tracker foundation
- [x] Native Compose dashboard
- [x] Local builder and laboratory task countdowns
- [x] Progress bars and task completion
- [x] Local task persistence
- [x] CI debug APK workflow
- [ ] Device notification scheduling and rescheduling after reboot
- [ ] Unit and UI tests

## Phase 2 — Village profile and recovery engine
- [ ] Player tag validation and profile entry
- [ ] Secure backend for Supercell API requests
- [ ] Store API token only as a backend secret; never in the APK or repository
- [ ] Parse available player/village data
- [ ] Clearly label fields not provided by the official API
- [ ] Recovery scoring and upgrade priority rules
- [ ] Resource cost and duration catalog with game-version updates

## Phase 3 — Smart planning
- [ ] Builder availability and assignment planner
- [ ] Laboratory and hero upgrade planner
- [ ] Resource targets and daily task checklist
- [ ] User-editable recommendations
- [ ] Notifications for task completion

## Phase 4 — Base design studio
- [ ] Import supported base links / layouts where technically possible
- [ ] Visual grid editor and building inventory
- [ ] Layout templates for war, farming, trophy, and hybrid
- [ ] Validate available building counts against the player's town hall
- [ ] Export a supported in-game layout link only where the game format allows it
- [ ] Otherwise export a visual placement guide

## Phase 5 — Quality and release
- [ ] Backup and restore
- [ ] Accessibility and localization
- [ ] Automated tests
- [ ] Signed release APK / AAB
- [ ] Privacy policy and API compliance review

## API and security notes
The official Clash of Clans API is read-only. Do not automate gameplay, modify game memory, or claim that the app can place buildings or start upgrades in the game. API availability and returned fields must be checked against current official documentation. Never request or store a user's game login password.