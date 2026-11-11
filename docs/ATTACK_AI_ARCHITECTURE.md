# Attack AI Architecture

## Current status

The Android app currently collects an attack scenario, attaches a base screenshot, and saves attack-plan metadata per Player Tag. It does **not** yet run image recognition, recommend an army, or calculate star probabilities.

AttackAnalysisModels.kt defines the typed contract for a future analysis provider. It is an integration boundary, not an AI implementation.

## Proposed hybrid design

1. **Android client**
   - Selects the player's own profile and enemy base screenshot.
   - Shows which profile facts are known, user-entered, imported, or unavailable.
   - Validates images before analysis and asks consent before any upload.
   - Displays progress, retry, cancellation, and clear error states.

2. **Analysis service**
   - Authenticates the app without embedding a provider secret in the APK.
   - Accepts a short-lived upload or protected image payload.
   - Runs visual recognition and returns structured observations with confidence.
   - Uses a versioned strategy engine to compare the observed base with verified troop, spell, hero, and siege levels.
   - Returns separate estimates for 1, 2, and 3 stars only when the model has a defensible calibrated basis.

3. **Report**
   - Lists detected defences and approximate locations.
   - Marks uncertain detections and asks the player to confirm them.
   - Gives army composition, spell plan, entry direction, funnel and deployment sequence.
   - Explains limitations such as hidden traps, defending Clan Castle troops, unseen levels, and execution variability.

## Data rules

- The official Clash of Clans API is read-only and does not expose a complete live enemy base layout.
- Never assume a Player Tag provides every troop level or live village detail.
- Unknown values remain unknown; do not silently substitute Town Hall defaults or invent levels.
- Never promise a guaranteed result or display made-up success percentages.
- Do not request Supercell ID passwords or verification codes.
- Any remote AI provider key belongs only on the backend, in server-side secret storage.
- Images should be transmitted only with user consent, protected in transit, retained only as long as needed, and deletable under a documented retention policy.
- Keep a model/version identifier and analysis limitations in every report.

## Implementation sequence

1. Collect a real user-provided sample of the player's available village export (if the game offers one) and document its schema before writing a parser.
2. Build the profile-data normalizer and mark each field with its source and freshness.
3. Select and evaluate a vision model against labelled Clash of Clans base screenshots.
4. Implement a secure backend endpoint and response validation.
5. Connect an Android provider implementation to AttackAnalysisProvider.
6. Add offline fallback for basic checks and saved reports; do not present fallback heuristics as AI.
7. Evaluate star estimates against a sufficiently large, labelled attack dataset before exposing numeric probabilities.

## Provider response requirements

A successful response must include model version, detected features and per-feature confidence, recommended units with quantities and known levels, attack sequence and rationale, calibrated 1/2/3-star estimates or explicit unavailable values, and limitations or missing information.

The UI must distinguish NeedsMoreInformation from service failure and from a valid analysis report.
