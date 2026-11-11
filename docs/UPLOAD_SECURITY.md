# Attack Screenshot Upload Security

## Status

Design only. Clash Manager does not currently upload screenshots or operate a remote analysis backend. The Android screenshot picker and local validation remain on-device. Do not configure a production endpoint until the controls below are implemented and tested.

## User consent

Before the first network transfer for an analysis, show a clear consent screen that explains:
- the selected enemy-base screenshot and the attack scenario will be sent to the Clash Manager analysis service;
- the image may be processed by a separately disclosed AI service provider if one is configured;
- why the image is needed, how long it is retained, and how to request deletion;
- upload is optional and cancelling keeps the image on the device.

Consent must be explicit and tied to the specific analysis. Do not infer consent from selecting a screenshot or pressing a generic Prepare button. Record the consent version and timestamp, not the image itself.

## Authentication and secrets

- Never ask for Supercell ID passwords, verification codes, or game account credentials.
- Never put a Supercell API token, AI provider key, backend signing secret, or permanent service credential in the Android APK or repository.
- The backend must issue short-lived, revocable app session tokens after authenticating a Clash Manager user.
- Use HTTPS only. Reject cleartext HTTP and invalid TLS certificates.
- Keep provider credentials in server-side secret storage and rotate them independently of app releases.
- Apply per-user and per-IP rate limits, request IDs, and abuse monitoring without logging image contents.

## Upload validation

The Android client performs an early MIME and size check, but the server must repeat all checks:
- maximum request body and image size: 10 MiB;
- allow only explicitly supported raster formats (initially JPEG, PNG, WebP);
- verify actual file signatures and decode the image; do not trust the filename or Content-Type alone;
- reject malformed, oversized-dimension, animated, or unsupported files;
- generate a random server-side object name; never use a client filename as a storage path;
- store uploads outside the web root with private access and encryption at rest;
- protect multipart endpoints against resource exhaustion and enforce request timeouts.

## Retention and deletion

- Process images in temporary private storage.
- Delete the original image after analysis completes unless the user separately opts into saving it.
- Set a strict maximum retention TTL for failed and abandoned uploads; run a scheduled cleanup job.
- Provide a deletion path for any retained image and its derived data.
- Do not include image bytes, base64, or private storage URLs in logs, analytics, crash reports, or saved attack-plan metadata.
- Document any third-party processor, data region, retention period, and deletion behavior before enabling it.

## API behavior

- Require an authenticated short-lived bearer token and an X-Request-Id.
- Enforce idempotency or duplicate-request protection for retried uploads.
- Return structured errors for invalid files, expired sessions, rate limits, and unavailable analysis.
- Never return fabricated troop levels or star probabilities. Unknown values must be null or explicitly unavailable.
- Include model version and limitations in every successful analysis response.

## Release checklist

- [ ] Backend authentication and token revocation tested.
- [ ] TLS-only deployment and secret storage verified.
- [ ] Server-side file signature, decode, dimension, and 10 MiB checks tested.
- [ ] Consent UI and cancellation path tested.
- [ ] Retention TTL and deletion job tested.
- [ ] Rate limiting, request size limits, timeouts, and safe logging tested.
- [ ] Third-party AI processor and data handling disclosed.
- [ ] Android upload, retry, cancellation, and error states tested.
- [ ] No secrets or production endpoints committed to the APK.
