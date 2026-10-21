# Dayerize Alloy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the approved compact Alloy interface and original artwork in a working Android testing APK, then complete the full catalog and separately integrate optional no-billing Google sync.

**Architecture:** Retain Kotlin/Compose/Room and the existing accounting domain. Introduce a versioned artwork manifest and a resolver that preserves legacy identifiers; native screen components share Alloy tokens. Keep catalog production and cloud synchronization as separate subprojects so neither blocks the offline app.

**Tech Stack:** Kotlin 2.1.20, Compose BOM 2025.04.01, Room 2.7.1, API 26 minimum / SDK 35, JDK 21, Gradle 8.11.1, existing Coil and local ONNX cutout.

**Spec:** `docs/redesign/alloy-full-design.md`; `docs/superpowers/specs/2026-10-21-alloy-artwork-and-sync.md`; approved cute artwork `/workspace/generated_images/exec-868360df-3e0e-4d91-863c-a98693214cbd.png`.

## Global Constraints

- Preserve application ID `app.dayerize`, existing debug signing certificate, saved records, original currencies and local exports.
- Baseline 390dp viewport, 16dp margins, 12dp gaps, 22sp headings, 14sp body and supporting 12sp labels. Minimum 48dp interactive targets.
- Preserve system font scaling; check 360/390/411dp, 100/130/200% text and English, Simplified Chinese, Traditional Chinese and Malay.
- Four main destinations only: Collection, Wishlist, Analytics, Settings. Sale remains inside detail/status filters.
- Fit artwork proportionally; no image stretching or silent cropping. Cute 2D and realistic 3D remain separate approved styles.
- The complete catalog target is 600 identities / up to 1,200 exports. Initial reviewed batches may ship sooner but must never be represented as the completed catalog.
- No paid cloud/AI endpoint, billing enrollment or automatic paid upgrade. No mock authentication presented as working login.
- Cloud tasks already run in an isolated checkout; use `/workspace/dayerize` branch `work`, not another worktree.

## Review Focus

1. Restored legacy/unknown artwork IDs retain asset data and render safely: Task 2.
2. Mixed currencies and per-use costs never produce misleading daily-cost sorting: Task 3.
3. Large fonts, long localized labels and large amounts remain readable and actionable: Tasks 1, 3, 4 and 8.
4. Cancellation/failure during photo processing retains the original and never writes a broken image reference: Task 4.
5. Failed/cancelled exports and invalid restores never report success or alter existing assets: Task 7.

## Delivery and execution

Recommended execution: native/inline in this session, with one independent whole-change reviewer after tests. The tasks share state and artwork interfaces; parallel implementation would create avoidable coordination overhead.

Milestone A (this plan): installable Alloy offline app with an explicitly counted, reviewed initial original-artwork batch. Milestone B: complete the 600-identity catalog under `2026-10-21-original-catalog-production.md`. Milestone C: optional account/sync under `2026-10-21-google-sync-readiness.md`, blocked on current documentation and project configuration, not on visual development.

## Task 1 — Shared Alloy layout and navigation

**Files:** modify `app/src/main/java/app/dayerize/ui/Components.kt`, `ui/AppRoot.kt`; create `ui/AlloyTokens.kt`; extend `app/src/test/java/app/dayerize/RedesignRenderTest.kt` and `LargeTextChineseTest.kt`.

**Interfaces:** existing screen signatures remain unchanged. New `AlloyTokens` provides graphite dark background `#171A1C`, surface `#202528`, silver outline `#596066`, amber accent `#FFAA3C`, 16dp page margins and 12dp content spacing. `AlloyPanelShape` uses restrained 8dp clipped corners on feature panels; form fields use rounded 12dp corners.

- [ ] Change shared theme tokens, typography, panel spacing and selected states. Light mode uses light surfaces with dark text and darker amber for meaningful text contrast.
- [ ] Replace the oversized floating dock with the compact four-destination bar shown in Board A; put add action in Collection/Wishlist headers. Keep tab semantics, back behavior and scroll clearances intact.
- [ ] Run existing navigation/save tests and capture dark/light renders. Inspect both before proceeding. Visual-only changes use render inspection rather than tests that assert private color constants.
- [ ] Commit the visual foundation without changing accounting/storage behavior.

## Task 2 — Original artwork manifest, resolver and picker

**Files:** create `app/src/main/java/app/dayerize/artwork/ArtworkCatalog.kt`, `ArtworkResolver.kt`, `app/src/main/assets/artwork/catalog.json`; modify `ui/Components.kt`, `features/editor/IconPicker.kt`, `features/editor/EditorScreen.kt`, `features/settings/SettingsScreen.kt`, `backup/FileBackup.kt`; create `app/src/test/java/app/dayerize/ArtworkCatalogTest.kt` and `ArtworkFlowTest.kt`.

**Interfaces:** `ArtworkStyle { REALISTIC_3D, CUTE_2D }`; `ArtworkEntry(id: String, category: String, names: Map<String,String>, aliases: List<String>, files: Map<ArtworkStyle,String>, opticalScale: Float)`; `ArtworkCatalog.search(query: String, category: String?, style: ArtworkStyle, localeTag: String): List<ArtworkEntry>`; `ArtworkResolver.resolve(icon: String): ArtworkSource`. New persisted icon strings use `original/<id>:3d` or `original/<id>:cute2d`. Keep existing `Asset.icon` String; validate manifest IDs with an allowlist rather than interpolate arbitrary backup strings into paths.

- [ ] Write tests: unknown ID returns original fallback; `camera:3d` resolves legacy artwork; new ID resolves bundled original file; category/style search cannot return unavailable artwork; an asset edited without changing artwork retains its legacy ID. Run and observe failure before implementation.
- [ ] Produce a first paired batch of 24 original exports for 12 approved shared subjects plus an original fallback; use image-generation edits for independent exports, not unchecked atlas slices. Source generation, transparent edge checks and manifests follow the catalog plan. No blanket official-pack 2D choices in the new picker.
- [ ] Implement catalog loading, constrained resolution and selection preview/Apply. Support 3D/Cute 2D, category chips, localized query aliases and accurate available count. Legacy images remain renderable for saved records, with licenses retained.
- [ ] Add `artworkStyle` default preference (`3d` or `cute2d`) to Settings and backup validation; it applies only to new choices and never silently replaces existing artwork.
- [ ] Test select → save → reopen → backup/restore for each style, and a legacy selection. Run `bash tools/build-cloud.sh :app:testDebugUnitTest --tests '*Artwork*'` and expect zero failures.
- [ ] Commit original catalog integration and provenance with exact shipped counts.

## Task 3 — Collection, Wishlist and filter/sort sheet

**Files:** modify `features/collection/CollectionScreen.kt`; create `core/src/main/kotlin/app/dayerize/core/CollectionQuery.kt`, `core/src/test/kotlin/app/dayerize/core/CollectionQueryTest.kt`; add resource strings in all four locale directories.

**Interfaces:** `CollectionSort { PURCHASE_DATE, NAME, DAILY_COST }`; `CollectionQuery.apply(assets: List<Asset>, category: String?, status: Status?, tags: Set<String>, query: String, sort: CollectionSort, ascending: Boolean, today: LocalDate, reportingCurrency: String, rates: List<FxRate>): List<Asset>`. Stable tie-break by asset ID. Do not offer Recently added until a real creation timestamp exists.

- [ ] Test combined filters, deterministic ties, retired/sold cost behavior, Unicode name search, and unavailable currency conversion. DAILY_COST sorts only comparable per-day values; missing FX and per-use assets remain in an explicitly labeled unavailable group last, in either direction.
- [ ] Implement Board A compact summary/cards and Board D draft-then-Apply filter sheet with Reset. At 200% text use a single column when two columns cannot display complete amounts.
- [ ] Wishlist retains its own budget and purchase conversion, with no inclusion in owned totals. Preserve actual daily-cost calculation rules; do not copy mockup arithmetic as code.
- [ ] Run `:core:test` and the existing wishlist regression test, then capture collection/list/filter/wish states. Commit.

## Task 4 — Editor and photo-cutout workspace

**Files:** modify `features/editor/EditorScreen.kt`, `images/CutoutProcessor.kt`; create `features/editor/PhotoEditorSheet.kt`, `images/PhotoOutline.kt`; add `app/src/test/java/app/dayerize/PhotoEditingTest.kt`; extend `RedesignFlowTest.kt`.

**Interfaces:** `PhotoEditResult(originalName: String, renderedName: String)`; `PhotoOutline.render(source: Bitmap, widthPx: Int): Bitmap` accepts 0..4px and expands transparent bounds so the outline cannot clip. `PhotoEditorSheet` returns a result only on Apply; cancellation preserves the current asset photo.

- [ ] Test missing/invalid input, zero-width outline, non-square aspect ratio and cancellation with unchanged asset references; run failing tests first.
- [ ] Use compact groups, centered editable artwork, existing Asset/Wish switch, target fields and pinned Save. Surface validation next to Save and make the erroneous field reachable.
- [ ] Implement Original/Cutout preview, processing/error state, white outline switch/0–4px slider, proportional Fit and explicit Apply. Do not ship decorative Fill/Custom choices without behavior. Retain existing local processing; no network call or new paid service.
- [ ] Test Apply → save → reload preserves original and output, and a failed cutout leaves the original usable. Verify big-font Save tests, then commit.

## Task 5 — Detail and sale settlement

**Files:** modify `features/detail/DetailScreen.kt`; extract `features/detail/SaleScreen.kt`; extend `app/src/test/java/app/dayerize/RedesignFlowTest.kt`.

**Interfaces:** reuse `Asset`, `Money`, `CostCalculator`; `SaleScreen(asset: Asset, vm: AppViewModel, onBack: () -> Unit)` stores through the existing validated save path.

- [ ] Test purchase MYR2400, sale MYR1800, 400 inclusive calendar days produces net MYR600 and MYR1.50/day; existing per-use/retired regression tests continue to pass.
- [ ] Apply Board A detail proportions and Board D sale layout. Negative gain uses neutral text with explicit sign, never green profit styling. Preserve retirement/reactivation confirmations and delete confirmation.
- [ ] Test invalid amount/date and cancelled settlement do not mutate records; run core/domain and relevant UI tests, then commit.

## Task 6 — Analytics and settings

**Files:** modify `features/analytics/AnalyticsScreen.kt`, `features/settings/SettingsScreen.kt`; extend render tests and localized resources.

**Interfaces:** existing `summarize`, `AppState`, `vm.setting`, `DisplayOptions` remain authoritative. No new historical recorder in this visual milestone.

- [ ] Apply Board C summary/category chart/status/monthly flow hierarchy, retain per-currency subtotals and missing-FX indication.
- [ ] Keep a truthful unavailable-history state; distinguish wishes from owned holdings.
- [ ] Apply compact grouped settings and the artwork default introduced in Task 2. Do not display a working Google connection or sync success until the cloud plan is implemented and configured.
- [ ] Capture light/dark settings, analytics empty/mixed-currency states and all supported locale labels. Commit.

## Task 7 — Backup/restore screen

**Files:** create `features/settings/BackupScreen.kt`; modify `ui/AppRoot.kt`, `AppViewModel.kt`, `backup/FileBackup.kt`; extend `RestoreSafetyTest.kt` and add `BackupFlowTest.kt`.

**Interfaces:** `BackupScreen(assetCount: Int, wishCount: Int, onExport: () -> Unit, onRestore: () -> Unit, onBack: () -> Unit, lastExport: String?)`; SAF launching remains in AppRoot. Record last successful export only after output stream completion. Export the actual archive, not the visual example filename alone.

- [ ] Test cancelled chooser leaves lastExport unchanged; export failure shows error and no success date; invalid restore leaves assets/photos/preferences intact. Run failing tests first.
- [ ] Implement Board D, archive preview/count confirmation, existing replacement semantics, unencrypted-file notice and no-file/never-exported states.
- [ ] Verify export → restore retains original artwork identifiers, files and preferences; commit.

## Task 8 — Native verification, review and testing APK

**Files:** modify `app/build.gradle.kts`, `docs/redesign/verification.md`, `docs/redesign/design-qa.md`; extend native render fixtures without leaking personal data.

- [ ] Set versionCode 4 / versionName 0.3.0 for the first Alloy testing release, preserving the existing signing key.
- [ ] Run `bash tools/build-cloud.sh :core:test :imaging:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`. Expected: all actual tests pass, lint has zero errors, APK builds. Investigate new warnings; retain explicit known limitations.
- [ ] Compare native captures against Boards A–D at compact widths and large fonts. Exercise navigation, create/edit, both artwork styles, wish purchase, filters, cutout and backup. Correct overlap, inaccessible actions and amount truncation before handoff.
- [ ] Dispatch one fresh whole-change reviewer; fix important findings with regression evidence. State physical-device verification separately from host tests.
- [ ] Verify ARM64 signature/version/checksum, publish through the existing authorized GitHub APK delivery path, download the public URL and match its hash. Do not use workspace links or ZIP for APK delivery.
- [ ] Report actual original-icon count and remaining catalog/sync work. No claim of full feature parity or completed 1,200-export catalog at this milestone.

## Self-review

Coverage: Boards A–D map to Tasks 1–7. Artwork breadth is carried explicitly to the separate catalog plan. Cloud/profile/birthday fields are carried explicitly to the separate readiness plan; they are not silently dropped or faked. Historical recording and decorative crop modes are excluded consistently with the approved design notes. Every Review Focus condition has a task/test owner. No product source has been changed while preparing this plan.
