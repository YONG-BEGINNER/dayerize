# Dayerize 0.4.1 — local receipt assistance

Keeps the Alloy navigation and minimalist structure. New owned-asset entry has one **Scan receipt** action. Choose a photo and script, read it on-device, then review editable price, currency, date and retailer before applying to the unsaved draft. Raw recognized text is collapsed under **Show details**. Cancel leaves the draft unchanged; applying does not save the asset.

## Included

- Bundled Tesseract OCR with English/Latin and simplified/traditional Chinese model choices; no account, paid API, billing or network permission.
- Ambiguous currency symbols/dates require review; subtotal, tender, change and quantity labels are not selected as purchase totals. Negative/refund notation is rejected rather than changed into a positive purchase.
- Bounded photo decoding with EXIF orientation, model integrity checks, serialized scans and cancellation/deadline handling. Receipts/text are not retained by the scanner or sent to an external AI service.
- Same 100 identities / 200 original 3D and cute 2D icons. Lossless WebP packaging of 1,284 older compatibility images saves 15,703,337 bytes; every decoded RGBA pixel was checked against its PNG original and saved icon IDs are preserved. New original artwork is unchanged.

## Verification

- 79 host tests passed: 43 core, 1 imaging, 35 Android host tests; zero failures/errors/skips. Full suite, lint and APK build successful. Lint has zero errors and 28 warnings; three new recommendations concern platform EXIF, timer scheduling and a KTX convenience function.
- Independent review: no Critical findings; all three Important parser findings reproduced RED and fixed GREEN, then full suite passed. No deferred reviewer findings.
- Real bundled-model OCR passed on Android 15 x86_64 software emulator: synthetic receipt → MYR 106.00 and 2026-10-23. Initial attempts were killed by the emulator's process-start ANR before test execution; precompiling packages resolved that. This is emulator evidence, not a physical-phone result or a real-world receipt accuracy benchmark.
- Receipt review screenshot inspected at 390dp width; raw text hidden and action buttons remain separate from scrolling fields.
- ARM64 testing APK: version 0.4.1, code 7, Android 8.0+, 98,028,963 bytes. Signature matches previous testing builds.
- SHA-256: `47b0a9eeb0fe8e8213ae69a34fa90727cce0680f3be7e8827fd4b4129f507b31`.

## Limits and remaining roadmap

Recognition may be wrong, especially with blur, handwriting, long receipts, unusual layouts or mixed scripts. Enter only this asset's price if a receipt covers multiple items. Latin mode uses an English model, not a dedicated Malay dictionary. Chinese models are bundled and integrity-checked; language-specific accuracy has not been benchmarked on physical phones. Cancellation depends on the native engine reaching an interruptible work unit.

Google login/Drive sync still need verified provider requirements and owner OAuth configuration. Photo classification, runtime icon generation, verified model-specific icons and the eventual 600-item catalog remain future work. Existing encrypted backups retain the 8 MiB limit. Physical-device testing of OCR, reminders, widgets and upgrade/data retention remains outstanding.

Build log: `/workspace/downloads/041-final-verification.log`. Native recognition evidence: `/workspace/downloads/041-native-recognition.log` (final 0.4.1 installed over the earlier build; 1 native test passed in 12.547 seconds). Dependencies/build/license details: `docs/ocr/README.md`. Back up your assets before updating.
