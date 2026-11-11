# App and Game Data Update System

## Goal
Users should be able to check, download, and install app updates from inside Clash Manager, and refresh game data independently without reinstalling the APK.

## Two independent update channels

### 1. Android application release
- Publish a versioned APK as a GitHub Release.
- Publish a small JSON manifest with version code/name, minimum supported version, APK HTTPS URL, and SHA-256 checksum.
- App checks the manifest on launch and from Settings; use a notification only when a newer compatible release exists.
- Download to app-controlled storage, verify SHA-256, then open Android's package installer using a content URI.
- Android must show the install confirmation. Clash Manager must never silently install an APK.
- Android package signature matching is mandatory. Every release must be signed with the same stable private release keystore. Never distribute an update signed with a random CI debug key.
- Store the signing keystore only in protected GitHub Actions secrets. Never commit it or place it in the APK/repository.
- If the app was installed from Google Play in the future, prefer Play In-App Updates rather than a separate APK installer.

### 2. Game data pack
- Publish a separate versioned JSON data pack for buildings, troops, heroes, spells, pets, equipment, levels, Town Hall unlocks, upgrade costs and durations.
- Include source/version metadata and SHA-256 checksum.
- Download and validate data pack independently; keep the last known-good pack if a new pack fails validation.
- Merge/override catalog data only after schema and value validation. Unknown IDs remain unknown; never infer names or levels.
- Show pack version, last checked time, and an in-app changelog.
- A game content update must not require a new APK unless parser/schema or UI code itself changes.

## Current implementation foundation
- `UpdateManifest` parses and validates app/data versions, HTTPS URLs, allowed repository hosts/paths, and SHA-256 values.
- Unit tests cover valid manifests, untrusted URLs, non-HTTPS APK links, and malformed checksums.
- This is validation groundwork, not yet a working downloader, notification, installer, or remote data-pack loader.

## Release prerequisites
1. Create and securely back up a stable Android release keystore.
2. Add its values as protected GitHub Actions secrets.
3. Build and publish signed release APKs with monotonically increasing versionCode.
4. Publish the manifest and checksums only after the APK/data pack is uploaded and verified.
5. Test upgrades from an installed previous release on Android 8 through current Android versions.
