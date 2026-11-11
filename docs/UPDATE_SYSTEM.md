# App and Game Data Update System

## Goal
Users should be able to check, download, and install app updates from inside Clash Manager, and refresh game data independently without reinstalling the APK.

## Two independent update channels

### 1. Android application release
- Publish a versioned APK as a GitHub Release.
- Publish a small JSON manifest with version code/name, minimum supported version, APK HTTPS URL, and SHA-256 checksum.
- The Update Center fetches the manifest when the user taps Check for updates. If a newer version is found, the app posts one notification per version when Android notification permission is available. Tapping it opens Clash Manager; the user can then review the Updates tab.
- Download to app-controlled cache storage, follow only approved HTTPS GitHub Release asset redirects, verify SHA-256, then open Android's package installer through a FileProvider content URI.
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

## Current implementation status
- `UpdateManifest` validates app/data versions, HTTPS URLs, approved repository hosts/paths, and SHA-256 values.
- `UpdateManifestClient` fetches `updates/manifest.json` from the repository's `main` branch.
- `GameDataPackParser` validates schema version 1, item IDs, names, bases, categories, duplicates and optional maximum levels. `GameDataPackInstaller` downloads a bounded file, verifies SHA-256, and keeps a previous known-good copy.
- The in-app catalog uses exact matching data-pack entries as overrides; unknown IDs remain unknown.
- `ApkUpdateInstaller` downloads a bounded APK, follows only approved GitHub release-asset HTTPS redirects, verifies SHA-256, and hands the file to Android through FileProvider. Android's package installer handles signature/version checks and user confirmation.
- Update Center supports manual manifest checks and data-pack/APK download actions. A newer app version triggers a once-per-version notification when notification permission is granted.
- The current repository does not yet contain a published `updates/manifest.json`, a verified game data pack, or a signed public release. Therefore end-to-end public updates are not active. CI currently builds a debug APK; it is not suitable as a public update release.

## Release prerequisites
1. Create and securely back up a stable Android release keystore. Never commit it or send it in chat.
2. In GitHub repository Settings → Secrets and variables → Actions, add `ANDROID_KEYSTORE_BASE64` (base64-encoded keystore), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`.
3. Push a version tag such as `v1.0.1` to trigger `.github/workflows/android-release.yml`. The workflow refuses to run without all four secrets, runs unit tests, builds the signed release APK, verifies its signature, calculates SHA-256, and publishes a GitHub Release.
4. Increase `versionCode` and `versionName` in `app/build.gradle.kts` before every new release. Keep the same signing key for all updates.
5. Publish `updates/manifest.json` and a verified game data pack only after the release assets are available and their checksums are confirmed.
6. Test upgrades from an installed previous release on supported Android versions before public rollout.

## Game data pack format (schema version 1)

The downloader accepts a JSON object with:
- `schemaVersion`: integer, currently `1`
- `dataPackVersion`: non-negative integer matching the manifest
- `generatedAt`: ISO-8601 timestamp string
- `source`: source attribution and license metadata
- `items`: array of game data entries

Each item must be added only from a verified source. Unknown or missing levels and values must remain unknown; do not generate guessed values. The parser and installer are connected to the in-app catalog. Only entries supplied in a valid, verified pack can override built-in catalog names; upgrade costs, durations and unlocks still require independently verified records.
