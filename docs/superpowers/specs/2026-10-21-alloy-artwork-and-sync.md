# Dayerize Alloy: original artwork and optional personal sync

Status: proposed design for review, not implemented. Selected visual identity: the third displayed concept (Alloy). User approved that direction and requested newly generated item artwork, more 3D variety, entirely custom 2D items, carefully controlled ratios and optional zero-cost Google login/sync.

## Visual and artwork contract

Retain Collection, Wishlist, Analytics, Settings and the existing editor/detail flows. No separate Sold tab. Use graphite, silver hairlines and small amber accents; preserve bordered overview. Prefer restrained chamfers rather than applying ornate cut corners everywhere. Retain light theme, locale support, original currencies and accessibility.

Reference images:
- Selected direction: /workspace/generated_images/exec-83bee996-d552-430f-a707-fa63e613b81f.png
- Newly generated 3D sample sheet: /workspace/generated_images/exec-def42b92-d09e-4929-bb27-0d3e53d19499.png
- Newly generated 2D sample sheet: /workspace/generated_images/exec-aeb9a69d-c175-415d-ae2a-ac6b02af4215.png
- Revised Collection, picker and account draft: /workspace/generated_images/exec-8e5f9488-11dd-42da-b0fe-be5ff04fe9ce.png

These are image-generated design references, not validated production icon exports or functioning screens. Generated sample sheets have unequal optical sizing and some shaded 2D details; individual production exports must correct these. Do not promise pixel-perfect results from the mockup alone.

Baseline target: 390dp-wide Android screen. 16dp side margins, 12dp grid gap, 22sp page title, 14sp body, 12sp supporting text, 48dp minimum interactive targets. Collection art slot about 90dp, object optical extent roughly 64–72dp; detail art about 144–160dp. Cards size to content. At large font scales or narrow widths prefer one column rather than truncating money. Preserve system font scaling. Fit source artwork proportionally, never stretch or crop; tune per-icon optical scale and offset rather than assuming identical pixels mean identical perceived size. Check at 360/390/411dp and 100/130/200% fonts, dark/light themes and all supported locales.

Custom art uses generic unbranded silhouettes, consistent light direction in 3D, and flat contour/fill language in 2D. No official item packs as the new selectable catalog. Existing third-party 3D selections may remain as legacy artwork to avoid breaking saved assets; retain required licenses. New 2D entries are all original generated artwork; explicit mapping can replace known legacy choices without changing asset data. Unknown restored IDs need an original fallback, never silent deletion.

Proposed first production collection: 48 object identities, each with original 3D and 2D artwork (96 exports), followed by thematic expansions. Categories: personal electronics, camera/audio, home/appliances, bags/wearables, transport/sport, hobby/collectibles. First samples cover 12 identities, not the entire catalog. Add meaningful shape/material variants, not only recolors. Every export needs stable ID, localized name/search terms, category, style, intrinsic dimensions, optical scale/offset and generation provenance. Bundle optimized artwork offline; no runtime AI generation API. Check transparent edges on dark/light backgrounds and readability at 48/64/96dp. Keep signed-app upgrade and old backup compatibility.

## Optional Google connection: recommended architecture to validate

Recommendation for personal use: local Room remains the primary store; Google account connection plus Google Drive app-specific data stores remote sync records and photo blobs. Credential Manager identity and Drive authorization are separate concerns. Request only necessary profile identity and app-data Drive permission. Do not infer birthdays from Google identity. No custom password server, email/SMS provider or additional social login in the first iteration.

Google Cloud project/API enablement, OAuth Android client, application ID and signing SHA registration are prerequisites. Use the correct release signing certificate; do not embed server secrets in the APK. Verify current OAuth testing/production, consent, token expiration and scope requirements before enabling sync. No cloud project or account has been created by this task.

Offline mode remains fully functional. Connecting an account does not upload existing records until the user chooses the local workspace to sync. Settings shows connected identity, sync enablement, last success, pending/error state, manual Sync now, disconnect/logout and separate remote-data deletion. Logout stops workers and clears/revokes access where supported; local assets remain unless the user explicitly deletes them. Switching accounts must never attach the previous account's dataset automatically.

Initial sync scope: assets, wishes, categories/tags, manual exchange rates and referenced photos. Locale/theme/device display preferences remain device-local. Record schema version, stable ID, per-device revision and tombstones. Use immutable per-device change batches and merge by record revisions; concurrent edits become explicit conflicts preserving both versions. Do not overwrite an entire database with last-writer-wins snapshots. Photos use content hashes; metadata must not claim complete sync before referenced blobs are available. Use resumable/idempotent retry, quota backoff and visible pending progress. Android background work is best-effort, not guaranteed instantaneous real-time sync.

Privacy: TLS and provider storage protections are not end-to-end encryption. If end-to-end encryption is selected later, specify recovery/key-sharing separately; do not invent a Google password-based key. Local export/restore remains available even when account access fails.

## Cost evidence and release gate

Constraint: no paid backend, no paid AI endpoint, no SMS authentication, no billing account and no automatic paid upgrade. Bundled icon use carries no per-use AI service cost; generation during development may consume the user's tool/subscription allowance.

Candidate comparisons:
- Google Drive app-data: recommended candidate for one-person multi-device use without a hosted database; consumes the user's Drive quota and requires a custom merge protocol.
- Supabase Free: potentially simpler managed identity/database but service quotas, project availability and photo limits require verification.
- Firebase Spark: potentially suitable for identity/database, but photo storage/billing eligibility must be checked; no billing upgrade authorized.

Official pages attempted on 2026-10-21:
- https://developers.google.com/workspace/drive/api/guides/limits
- https://developers.google.com/workspace/drive/api/guides/appdata
- https://supabase.com/pricing
- https://firebase.google.com/pricing

All four returned proxy HTTP 403. Current pricing, quota numbers and OAuth policy were NOT verified. A zero-cost guarantee cannot be made. Before cloud implementation/release, read the current official Drive usage/pricing, account storage and OAuth documentation; validate configuration without billing and test quota exhaustion. If limits are exceeded, pause sync and retain all local data; never auto-purchase storage. API availability globally, including Google-restricted regions, is not guaranteed. Offline use remains the fallback.

## Verification and implementation sequence

1. Review revised UI and artwork direction; independently resolve the current cloud cost evidence gap.
2. Create an implementation plan after this specification is approved. Implement visual/artwork changes without making the app dependent on cloud provisioning.
3. Validate individual artwork exports and native renders, legacy icon restores and full creation/edit/save flows before APK handoff.
4. Introduce an optional sync adapter only after project configuration and cost checks are complete. Test denial/cancellation, offline retry, repeated delivery, simultaneous edits, delete-versus-edit conflicts, photo interruption, account switch/logout, revoked consent and full storage. Never describe backup-only functionality as multi-device sync.

No application code or cloud settings were changed in this design task.
