# Android Asset Manager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Deliver the approved personal Android asset manager with multilingual UI, correct multi-currency accounting, rich asset imagery, and a future account boundary.

**Architecture:** Native Compose UI backed by Room repositories and pure Kotlin calculations. A stable local workspace owns assets; reporting preferences and optional later account linking are independent. Image catalogs and cutout processing expose separate interfaces.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Gradle, JUnit, Android instrumentation tests. Proposed compatible baseline: JDK 21, Gradle 8.11.1, Android Gradle Plugin 8.9.2, Kotlin 2.1.20, Compose BOM 2025.04.01, Room 2.7.1, KSP 2.1.20-1.0.32, compile/target SDK 35, min SDK 26. Verify dependency resolution before locking the baseline; use verified official distribution checksums.

**Spec:** `docs/android-design-proposal.md`, superseded by `docs/prototype-interactions.md` and `docs/approved-scope-addendum.md` where they differ.

## Global Constraints

- Android first; Collection, Analytics, Settings; no separate Sold tab.
- Retirement freezes current cost; a later sale's calendar ownership cost is separately labeled.
- Preserve purchase/sale amounts in their original currencies; never sum currencies without conversion.
- Initial locales en, zh-CN, zh-TW, ms; all user-visible copy in resources.
- Bordered summaries, compact headings, custom 3D/flat assets and accessible controls.
- Personal local storage and user-controlled backups; no account needed now.
- No real-money feeds, cloud AI, authentication, or publication without implementing their explicit requirements.
- Use the existing isolated checkout. No worktree creation is required.
- Do not call a static mockup interactive, a simulated cutout AI, or a build a device test.

## Review Focus

- Different currency fraction digits and stale/missing FX must never corrupt source amounts: Tasks 1 and 4.
- Retirement, travel, leap days, and backdated resale must keep denominators deterministic: Tasks 1 and 4.
- Restoring an interrupted or malicious archive must preserve usable existing data: Task 7.
- Icon packs disappearing and app restarts must preserve selections and show a fallback: Tasks 2 and 3.
- Language changes, long translations, and large text must not hide save/confirm actions: Tasks 4 and 6.

## File structure

- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, verified Gradle wrapper: reproducible build.
- `core/`: JVM domain models, Money, FxRate, CostCalculator, AssetLifecycle and tests.
- `app/src/main/java/app/dayerize/data/`: Room entities, DAOs, migrations, repositories.
- `app/src/main/java/app/dayerize/images/`: icon manifest/catalog, downloads, photo storage, cutout boundary.
- `app/src/main/java/app/dayerize/features/{collection,editor,detail,analytics,settings}/`: each screen, state, and ViewModel.
- `app/src/main/java/app/dayerize/backup/`: archive schema, validation, staging, recovery.
- `app/src/main/java/app/dayerize/workspace/`: stable local workspace identity and future account interface.
- `app/src/main/res/{values,values-zh-rCN,values-zh-rTW,values-ms}/`: text resources.
- `app/src/test/` and `app/src/androidTest/`: application and device tests.
- `docs/verification.md`: actual commands, results, device limitations, catalog provenance and known gaps.

### Task 1: Reproducible build and exact financial domain

**Files:** build files; `core/src/main/kotlin/app/dayerize/core/{Money,FxRate,Asset,CostCalculator,AssetLifecycle}.kt`; matching tests under `core/src/test/`.

**Interfaces:** `Money(minor: Long, currency: String)`; `FxRate(from: String, to: String, rate: BigDecimal, date: LocalDate)`; `convert(money: Money, rate: FxRate): Money`; `CostCalculator.calculate(asset: Asset, asOf: LocalDate): CostSummary`; lifecycle transitions return a validated Asset or a typed error.

- [ ] Resolve and verify the official Gradle/Android tools and pinned dependencies. Keep caches under writable workspace locations; never disable TLS/checksum verification. Add only the test harness and model declarations required to compile failing tests.
- [ ] Add tests `moneyUsesCurrencyScale` (JPY 123, MYR 1.23, KWD 1.234), `conversionUsesExplicitRate` (USD 100.00 × 4.50 = MYR 450.00), `missingRateIsNotOneToOne`, and `conversionRejectsWrongPairOrNonpositiveRate`.
- [ ] Add `costFreezesAtRetirement`: purchase 2026-01-01 for 1,000, retire 2026-01-10 → 100/day even on January 20. Add `saleUsesCalendarHoldingDays`: sell January 20 for 400 → 600/20 = 30/day. Add same-day, leap-day, profitable-sale, zero-use and future-date rejection cases.
- [ ] Run `./gradlew :core:test`; observe expected assertion failures before implementing calculations.
- [ ] Implement exact decimal ratios, positive rate validation, minor-unit rounding, date rules, and lifecycle transitions. No UI formatting or network access in core.
- [ ] Run `./gradlew :core:test`; require all tests to execute and pass. Commit this tested deliverable.

### Task 2: Durable local workspace and asset persistence

**Files:** `data/{AssetEntity,UsageEntity,FxRateEntity,AssetDao,AppDatabase,RoomAssetRepository}.kt`, `workspace/LocalWorkspace.kt`; Room tests.

**Interfaces:** `AssetRepository.observeAssets(workspaceId: String): Flow<List<Asset>>`; `save(asset: Asset)`; `delete(id: String)`; `LocalWorkspace.id: String`. Asset stores image reference, original amounts, dates, tags, notes, serial, warranty, purchase channel, disposal channel and FX snapshots.

- [ ] Add device tests saving an asset with non-Latin text, different purchase/sale currencies, usage and icon reference; close/reopen database and assert exact round-trip equality.
- [ ] Add transactional transition failure test: invalid disposal leaves all existing rows unchanged. Add stable workspace ID test across relaunch.
- [ ] Run relevant instrumentation tests and observe failure with unimplemented repository; emulator availability is required to claim results.
- [ ] Implement Room repositories, versioned schema exports and workspace preferences. Keep account/user fields separate from local ownership.
- [ ] Run `./gradlew :app:connectedDebugAndroidTest` and core tests. Commit only after reporting actual outcomes; never substitute JVM mocks for Room/device verification.

### Task 3: Searchable 3D/flat imagery and photo import

**Files:** `images/{IconManifest,IconCatalog,IconPackStore,PhotoStore}.kt`, `features/editor/IconPicker.kt`, assets manifest and license notices; catalog tests.

**Interfaces:** `IconRef(pack: String, id: String, style: String)`; `IconCatalog.search(query: String, style: String?): List<IconEntry>`; pack installation exposes progress/failure and retains existing packs atomically.

- [ ] Verify a licensed upstream catalog, exact revision, file counts, categories and attribution. Target ≥500 distinct 3D and ≥500 flat choices; report actual delivered counts rather than counting color variants as distinct objects.
- [ ] Add failing tests for style/category/search filtering, stable IDs after manifest reorder, missing-pack fallback, invalid paths, and interrupted download preserving installed assets.
- [ ] Implement a curated bundled starter pack and optional full packs with digest verification. Support original photos and transparent custom images through system pickers and app-private copies.
- [ ] Add the searchable style-switching icon picker, loading/empty/offline states and explicit download sizes. A 3D bitmap is artwork, not a rotatable 3D mesh.
- [ ] Run catalog tests, actual manifest/count validation, and a device import/relaunch test. Commit with licenses and catalog provenance.

### Task 4: Collection, entry, detail, retirement and resale

**Files:** Collection/Editor/Detail screen, ViewModel and state files; `ui/theme/`; navigation; resource files; flow tests.

**Interfaces:** ViewModels use AssetRepository and core calculations. `AssetEditorState` retains drafts, original currency, image choice, target mode, advanced fields and validation errors. Save returns a persisted asset ID.

- [ ] Write failing UI tests for create → detail → edit → search notes/serial → filter → grid/list → retire → resell. Assert the collection summary updates after each committed action.
- [ ] Add invalid amount/date, unsaved-back, denied photo access, zero matches, missing exchange-rate and profitable-sale assertions. No mutation on Cancel.
- [ ] Implement the approved image sheets faithfully with original/licensed asset images, bordered summary, compact headings, purchase amounts and cost badges. Root navigation uses Collection/Analytics/Settings.
- [ ] Implement currency selection per asset and sale; require conversion snapshot for cross-currency settlement. Show signed gain/loss and separately labeled frozen versus final costs.
- [ ] Add accessible alternatives to long-press, color and animated feedback. Reduced motion suppresses flip, parallax, rolling digits and confetti.
- [ ] Run core tests and device flows in English and Simplified Chinese with enlarged fonts. Capture screens for visual comparison and fix layout failures. Commit the working vertical slice.

### Task 5: Depreciation and real cutout processing

**Files:** `core/.../ValuationPolicy.kt`, `images/{CutoutProcessor,CutoutResult}.kt`, depreciation editor and photo-processing state; tests.

**Interfaces:** `ValuationPolicy` supports manual value, straight-line residual value and dated custom value checkpoints; `estimate(asset: Asset, date: LocalDate): Money`. `CutoutProcessor.process(input: Uri): CutoutResult` returns a real transparent image or typed failure.

- [ ] Add valuation tests: cost 1,000, residual 100, 100-day plan → 550 at 50 elapsed depreciation intervals; clamp to residual after plan. Make the depreciation interval convention explicit rather than reusing inclusive holding days blindly.
- [ ] Test category defaults copied to new assets, per-item overrides, invalid future checkpoints and preservation of manual estimates.
- [ ] Select and license-check an on-device segmentation model using representative electronics, furniture and clothing photos. Record model size, dependencies, quality and device support. A person-only segmenter is insufficient.
- [ ] Write processing tests for success with transparent background, failure, cancel and original-image preservation before wiring the UI. Keep algorithm tests and actual device inference evidence separate.
- [ ] Implement processing and comparison states. Show a scan effect only while actual work runs; fallback to original is explicit. Do not silently drop AI cutouts if model evaluation fails—report the blocker.
- [ ] Run tests and inspect real device outputs. Commit after image provenance and model licensing are documented.

### Task 6: Analytics and international settings

**Files:** `core/.../PortfolioSummary.kt`, Analytics/Settings screens and state; locale resources and tests.

**Interfaces:** `summarize(assets: List<Asset>, reportingCurrency: String, rates: List<FxRate>, date: LocalDate): PortfolioSummary` includes converted subtotal, missing currencies, per-currency totals and rate dates. Historical series uses versioned dated financial events, not fabricated back-projections of today's data.

- [ ] Write missing-rate test: MYR100 plus USD10 without USD/MYR rate shows separate subtotals and incomplete aggregate; with rate4.50 shows MYR145. Reporting-currency change never mutates source records.
- [ ] Add tests for monthly net flow using purchase/sale dates rather than record creation, sold exclusion from retained value, count-versus-value chart labels, and historical edits invalidating affected derived series.
- [ ] Implement estimated-value/category/status charts with readable text alternatives and honest unknown-history states. Avoid treating cumulative average ownership cost as a daily cash expense.
- [ ] Implement currency/rate settings, locale en/zh-CN/zh-TW/ms/system, theme light/dark/system, reporting timezone and reduced motion. Keep foreign amounts visibly identified.
- [ ] Verify every resource key across all locales, English fallback, JPY/MYR/KWD formatting, long translations, midnight refresh and 200% font scaling. Run domain/device tests and commit.

### Task 7: Backup, recovery and future account boundary

**Files:** backup schema/export/import/validator/recovery files; settings backup controls; workspace account-boundary documentation; backup tests.

**Interfaces:** `export(workspaceId: String, destination: Uri)`; `validate(source: Uri): ImportPreview`; `restore(preview: ImportPreview): RestoreResult`. Archive contains versioned records, workspace metadata, images, icon references, rate snapshots and preferences, never future authentication tokens.

- [ ] Add failing round-trip tests preserving original currency amounts, FX, all locales, tags and images; reject incompatible schemas, duplicate IDs, path traversal, oversized extraction, missing images and corrupt data.
- [ ] Add interruption tests at staging, pre-commit and post-commit; restarting must recover either complete old data or complete new data, not a mixture.
- [ ] Implement archive preview, explicit replacement confirmation, staged files, transactional database restore and recovery journal. Explain unencrypted export and user-selected cloud-folder implications.
- [ ] Document future `AccountSession`/profile extension points: login links rather than replaces local workspace; logout policy must preserve local ownership; birthday is optional profile data collected only when implemented. No authentication UI or network service in this release.
- [ ] Run round-trip and recovery tests on device; commit with documented schema and restore limitations.

### Task 8: Integration, review and APK handoff

**Files:** `docs/verification.md`, setup/startup instructions and regression tests.

- [ ] Run `./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and fix failures.
- [ ] Run `./gradlew :app:connectedDebugAndroidTest`; record device/API and completed tests. Run full create → relaunch → foreign-currency resale → analytics → export → restore flow, including offline icon fallback.
- [ ] Compare app screens with the approved review sheets at matched viewport/state. Check compact type, border hierarchy, icon quality, no Sold tab and no clipped translated controls.
- [ ] Complete an independent code review, resolve correctness/privacy/data-loss issues, then rerun affected tests.
- [ ] Record APK path and checksum, actual icon counts, supported locales, build/test evidence, model limitations and any remaining blockers. Do not describe unresolved required features as complete.

## Plan self-review and current environment

Coverage includes initial proposal workflows plus the latest currency/language/catalog/future-account requirements. Account deployment remains future scope by user request. The six static visual screens do not prove any runtime behavior.

Inspection found JDK 21, but no SDK/Gradle executables in the checked paths and ANDROID_HOME/ANDROID_SDK_ROOT unset. A read-only GitHub catalog query failed because the configured proxy at port 8080 was unreachable. Dependency/model/catalog installation and Android verification therefore remain unverified. Do not disable the proxy or integrity checks to bypass this.

Execution approach confirmed: direct implementation in this session, task by task, with an independent whole-application review. Tasks share domain and persistence interfaces, so one implementer keeps the changes coherent. Existing design approval stands; no repeated design approval is required.

## Execution review — 2026-10-19

The architecture and eight-task sequence cover the approved scope. The environment paragraph above records initial inspection, not current readiness: toolchains and implementation now exist, while complete device verification remains outstanding.

Before expanding functionality, resolve the independent review's correctness findings with regression tests: validate restore preferences before any replacement (including empty archives); reject unrepresentable FX conversions; preserve retirement-frozen gross cost separately from calendar-day resale settlement; use API-26-compatible image reads; retain editor drafts across recreation; make usage increments atomic; and provide missing-image fallback.

Then complete remaining planned workflows, including camera capture, tag management/filtering, valuation checkpoints and category defaults, historical recording, and recovery validation. Do not treat partial implementation or a successful APK build as completion of these tasks.

Release gate: financial and persistence tests, Android lint/build, completed device flows, multilingual/large-text visual checks, backup interruption checks, and documented model quality/device limitations. Future login/logout/profile/birthday remain extension points, not current authentication features. No publishing or cloud service rollout is included.
