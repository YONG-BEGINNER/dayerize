# Dayerize 0.4.0 testing milestone

This release extends the existing Alloy interface and local asset store. It does not complete the full enhancement roadmap.

## Included

- Original artwork expansion to 100 item identities / 200 selectable icons in 3D and cute 2D. Generic illustrations, not verified branded models. Existing artwork IDs are preserved.
- Picker favourites, recent selections and category/text search; compact, comfortable and large layout settings; entry templates and duplicate drafts.
- Usage logging and corrections; ownership expenses, event history and correction; separate purchase-only and expense-inclusive metrics, including sale recovery in final ownership cost per use.
- Collections and locations; wishlist savings goals, comparisons and conversion; local usage/target insights. Original currency amounts and existing dated manual reporting rates remain authoritative.
- Opt-in local reminders, backup reminders, and an Android overview/favourites/quick-entry widget.
- Password-encrypted backup and restore preview, including new persistent fields and picker preferences. Encrypted archives have an explicit 8 MiB limit; standard ZIP backups remain available for larger archives.
- Optional local name and manually entered birthday. No login is needed.
- Local interpreted category, date, price and status search with visible filters. This is deterministic parsing, not a hosted language model.

## Remaining roadmap work

- 600 item identities / 1,200 icons and visually verified model-specific artwork.
- Google account login and explicitly activated Drive sync, including conflict preservation, account isolation and quota recovery. Provider requirements and OAuth setup are unresolved; no cloud upload or account placeholder is enabled.
- Receipt OCR: the maintained free local Tesseract4Android option needs build prerequisites and device validation. Google documentation and the alternative's JitPack distribution were inaccessible in this environment.
- Photo classification/model suggestions and runtime custom icon generation need suitable local models and phone benchmarks. Custom image import and bundled artwork remain available.
- Physical-device validation of Android reminders, widgets, upgrade installation, enlarged text and performance. Automated renders cannot establish these results.

## Privacy and cost

No billing account, subscription, paid runtime API or automatic paid upgrade is introduced. Asset records, receipts and photos are not sent to an external AI service. Account and sync work remains separate from the offline store.

Back up existing data before installing this testing release. Retain the backup password: encrypted archives cannot be recovered without it.

## Verified 2026-10-23

- Full core, imaging and Android host suite: 67 tests passed (35 + 1 + 31), zero failures/errors/skips.
- Android lint: zero errors, 25 warnings. Debug ARM64 APK assembled successfully.
- All 100 identities / 200 exports passed artwork validation; light/dark contact sheets at 48/64/96 pixels and updated native detail render visually reviewed.
- APK: app.dayerize, version 0.4.0, code 6, Android 8.0+, ARM64; 103,874,303 bytes.
- Signing certificate SHA-256 matches 0.3.1: `8912651d3133725e41bf8fe0d34cd94b4b24c92fa58edc128915efcbffa06962`. This verifies signing continuity, not an actual installed-app upgrade.
- APK SHA-256: `51fae5489c4231bfb70a5ec87690c64c28699d4c21540a76b8c79082be019306`.

Commands: `bash tools/build-cloud.sh :core:test :imaging:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`; `python3 tools/validate-artwork.py`; Android `apksigner verify --print-certs` and `aapt dump badging`. Logs retained in `/workspace/downloads/040-final-verification.log` and `040-artwork-validation.log`.
