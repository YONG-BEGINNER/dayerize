# Dayerize 0.2.0 verification — 2026-10-19

## Delivered

Native Compose redesign from the approved six-screen board and supplied references. Dark default theme, bordered charcoal summary/cards, compact metadata, lime selection accents, floating four-item dock plus add button, persistent wishlist and purchase conversion, grouped entry, wheel date sheet, category/tag sheets, searchable 3D/2D icon grid, coordinated detail/analytics/settings. Existing explicit light/system theme preferences remain respected.

Original-currency records remain unchanged by reporting settings. Wishlist budgets are excluded from owned totals and cashflow. Existing Room JSON gains defaulted fields; no database reset is introduced. Editor drafts use saveable state. The camera entry currently uses Android's camera thumbnail capture; high-resolution originals can be imported from the gallery.

## Actual checks

`bash tools/build-cloud.sh :core:test :imaging:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

Final command completed successfully. 21 core tests, 1 real ONNX host inference test, 8 Android host/Robolectric tests: 30 passed, 0 failed, 0 skipped. Lint: 0 errors, 16 nonblocking warnings (unused strings, modern manifest/icon attributes, launcher resource placement, optional KTX usage).

Native Android render captures in `screenshots/` cover Collection, Wishlist, both editors, Detail, Analytics, Settings, the icon dialog, date sheet, category sheet, and Chinese collection/editor at 200% text size. These are native graphics under Robolectric API35, NOT physical-phone or completed emulator instrumentation tests. View-model/Room flows exercise creation, wishlist purchase conversion, older usage-mode editing, daily-target editing, database reopen and rejected malformed restore.

Earlier failing regression tests demonstrated: wishlist incorrectly included in owned totals; resale changed retirement-frozen gross cost; sale net cost used usage counts; overflowing sale rate could persist; empty invalid-timezone restore replaced good data; editing a legacy per-use item changed its mode; daily-target rounding changed duration. Final tests cover the corrections. Number-format preferences now apply to costs as well as asset values.

All four locales have matching 145 string keys. Catalog: 1,284 original 256px 3D PNGs and 1,284 flat WebPs; licensed artwork bundled offline. Application has no Internet permission.

Version code 2, version name 0.2.0, app ID app.dayerize. ARM64 testing APK built for modern Android phones; additional ARMv7, x86, x86_64 and universal outputs are available locally. APK signature matches the prior v0.1.0 testing build: certificate SHA256 8912651d3133725e41bf8fe0d34cd94b4b24c92fa58edc128915efcbffa06962.

## Limits

Physical-device testing, camera behavior on the user's device, and on-device segmentation quality still require testing. Native host renders do not validate OEM navigation/IME behavior. Robolectric emitted image-decode interruption/CloseGuard messages during teardown; assertions and process exited successfully. No account service, premium membership, cloud sync, attachments between assets, custom valuation checkpoints, or historical snapshot recorder is introduced by this redesign. The existing historical-series gap is explicitly shown as unavailable, not fabricated. This remains an early testing build; preserve a backup before upgrading.

## Alloy 0.3.0

See [Alloy delivery evidence and limitations](alloy-0.3.0-delivery.md). The final functional suite has 44 passing host tests; lint has no errors and 18 warnings. Artwork is now original, with 12 identities in two styles. Physical-device validation remains outstanding.

## Alloy 0.3.1

52 host tests pass; lint reports no errors and 18 warnings. Both prior deferred UI issues are fixed and covered in all supported locales, with delayed-success/failure and superseded-preview checks. Catalog coverage is 36 identities / 72 original icons. See [0.3.1 delivery evidence](alloy-0.3.1-delivery.md).
