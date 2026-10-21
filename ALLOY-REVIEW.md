# Dayerize 0.3.0 — Alloy testing delivery

The approved first implementation milestone introduces graphite/amber Alloy panels, labeled navigation, compact asset cards, draft filters and sorting, photo preview/outline controls, a settlement preview, and a dedicated backup page. Existing multi-currency accounting, four languages and legacy asset identifiers remain supported.

## Artwork scope

12 original identities are bundled in both realistic 3D and cute 2D: camera, laptop, sedan, headphones, watch, backpack, sneaker, phone, coffee machine, drill, guitar and bicycle. A neutral fallback is included separately. All 24 exports were generated individually, with transparent square canvases, fit rendering and bounded optical scale. These are generic illustrations, not verified branded models. The planned 600-identity catalog and Google sign-in/sync are separate, unfinished milestones.

`design/artwork/production.json` records provenance. PNG encoding is lossless: `tools/optimize-artwork.py` asserts byte-identical decoded RGBA pixels before replacing each export. Generated master paths are retained in provenance.

## Verification

The complete host suite passes 44 tests, including 36 locale/width/font combinations and a populated-gallery regression ensuring large purchase amounts retain every digit. Android lint reports 0 errors and 18 warnings. Native host screenshots cover core flows, both themes, Chinese large text and the artwork selector. The independent final review identified one Important issue (purchase-amount truncation), now covered by a failing-then-passing regression and fixed by wrapping.

Physical-device testing is outstanding. Host tests do not prove camera quality, cutout quality, cancellation during active inference, every invalid-sale path or interrupted restore recovery. Existing camera thumbnail capture and local cutout limitations remain. Backup exports contain personal data and are not encrypted. Back up data before installing this early testing build.

## Execution rulings

- Cloud skill helper scripts were unavailable; equivalent ledger, brief and verification records were maintained manually. Cost: bookkeeping differences.
- Independent tasks proceeded while artwork generated, without changing the catalog interface. Integrated verification was repeated after asset installation. Cost: possible rework at integration.
- Tasks sharing the UI were integrated before completion commits. Cost: less granular commit history.
- The independent review examined the functional working tree while the last images generated. Final asset validation, render inspection and full build checks cover the final batch; no second reviewer was dispatched.
- Native Robolectric graphics were used for clipped-shape interaction and rendering; legacy graphics produced false click failures. Fixture isolation was also retained. Host results are not presented as device results.
- The source branch remains in the cloud workspace. APK publication follows the previously authorized delivery route; no source merge or source publication is implied.
- GitHub release-asset upload returned HTTP 401. Lossless PNG compression allows delivery through the existing public repository APK route without changing image pixels.

## Deferred minor findings

1. In the filter sheet, a user-created category literally named “All” (or its localized equivalent) collides with the all-categories choice. The category tabs remain usable.
2. The photo workspace may briefly show an error message while its initial preview loads. A separate loading state remains a follow-up.

## Build identity

Application ID: `app.dayerize`; version code 4; version name 0.3.0; Android 8.0+; ARM64 testing APK.

Signing certificate SHA-256: `8912651d3133725e41bf8fe0d34cd94b4b24c92fa58edc128915efcbffa06962` (unchanged from prior testing build).
