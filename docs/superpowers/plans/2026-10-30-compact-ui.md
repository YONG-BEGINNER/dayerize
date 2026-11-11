# Compact Android UI Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan.

**Goal:** Implement the user's requested compact home, analytics and settings UI, with visible Scrapped status.
**Architecture:** Keep existing Compose screens and persistence. Give status filters their own full-width scroll row; retain category filtering and all four states. Use a two-column summary with large-text wrapping, inline decimal choices, smaller chart and spacing, and 48dp minimum actions.
**Tech Stack:** Kotlin, Compose, Robolectric.
**Spec:** User-approved compact UI request in this conversation; retain dark/amber branding, all existing locales, original icons and data.

## Constraints and review focus
No schema or financial logic changes. Preserve large text, long money values, category names equal to All, all four statuses, and existing density preferences. Keep a horizontally scrollable fallback for status and decimal controls. Local debug signing differs from distributed builds.

### Task 1: Compact screens and exposed status
- [x] Add regression tests: home includes Scrapped count and filtering excludes active assets; inline decimal choice persists. Empty analytics behavior implemented; dedicated automated coverage deferred as a minor.
- [x] Run focused tests and confirm missing behavior fails.
- [x] Modify CollectionScreen, AnalyticsScreen, SettingsScreen, Components, AlloyTokens and AppRoot for the agreed layout. Keep existing touch minimums and font scaling. Bump testing version to 0.4.2/code 8.
- [x] Run focused tests, full host suite, lint and build. Inspect new screenshots in English and Chinese; retain original tracked screenshot files.
- [x] Obtain independent code review and address important findings. Leave reviewable working-tree changes; do not publish or push.

Pre-flight: one task; no inter-task interfaces. Execution uses the existing isolated cloud checkout. User's request to implement the previously described changes supplies authorization; no repeat design approval is needed.

Final: 84/84 tests, 0 lint errors / 28 warnings, APK verified. Independent review: two important large-text findings fixed with RED→GREEN regressions. Deferred minor and execution decisions recorded in docs/ENHANCEMENTS-0.4.2.md.
