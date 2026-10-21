# Google Account and No-Billing Sync Readiness Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Resolve the concrete cost/configuration prerequisites for optional Google account and Drive synchronization before writing a production cloud adapter.

**Architecture:** Offline Room remains authoritative for local operation. Google identity and Drive authorization are distinct; the proposed remote store is app-specific Drive data, with per-record revisions and explicit conflict handling. This readiness plan does not pretend that unresolved OAuth and quota details are already implementation-ready.

**Tech Stack:** Existing Android app; candidate Credential Manager identity, Google Android authorization and Drive REST API. Dependency versions and scopes must be pinned from accessible official documentation before an adapter implementation plan is finalized.

**Spec:** `docs/superpowers/specs/2026-10-21-alloy-artwork-and-sync.md`, optional Google connection section; Board C in `docs/redesign/alloy-full-design.md`.

## Global Constraints

- No billing account, paid backend, paid AI calls, SMS authentication or automatic paid upgrade.
- Offline use works without an account. Connecting never implicitly uploads a local workspace.
- Logout stops remote work; switching accounts never silently uploads the previous account's data.
- Optional birthday is user-entered; do not infer it from Google identity.
- Google Drive limits and policy may change; do not promise unlimited or permanently free operation.
- Archive backup alone is not multi-device sync.

## Review Focus

1. Storage full or request quota reached: stop remote writes with local edits retained.
2. Cancelled/revoked Google authorization: remain usable offline without fake connected state.
3. Logout/account switching during uploads: isolate identities and stop old-account jobs.
4. Concurrent edit/delete from two devices: preserve conflicts instead of last-writer database replacement.
5. Interrupted photo upload or duplicate batch delivery: idempotent records with no missing-photo success claim.

## Task 1 — Verify authoritative cost and authorization requirements

**Files:** create `docs/sync/readiness.md` recording URLs, retrieval dates, exact applicable terms and unresolved items.

- [ ] Read official Drive limits, app-data storage, Android authorization and OAuth consent/testing policies. Earlier attempts and the current retry returned proxy 403 for `developers.google.com`; this is a real unresolved prerequisite.
- [ ] Confirm whether API usage and app-data storage can operate with no billing enrollment, what account storage is consumed, and what happens when limits are reached. Record exact scope and test-mode token implications.
- [ ] If source access stays blocked, request the specific network allowlist access or user-provided official documents. Continue the independent offline UI/catalog work; do not substitute unofficial recollection for verified free-tier terms.

## Task 2 — Check project configuration without creating charges

**Files:** document only non-secret configuration expectations in `docs/sync/readiness.md`; never commit token, keystore or credential contents.

- [ ] Inspect existing configuration names/presence and authorized project availability; do not print secret values.
- [ ] Identify Google Cloud project, Drive API enablement, OAuth Android client and consent configuration bound to `app.dayerize` and the correct signing fingerprint.
- [ ] Prepare exact configuration steps for the owner if no project/client is available. No service deployment, billing activation or external project creation is implied by this plan.

## Task 3 — Finalize executable sync plan from verified evidence

**Proposed files:** `features/account/AccountScreen.kt`, `account/GoogleAccountGateway.kt`, `sync/DriveSyncTransport.kt`, `sync/SyncCoordinator.kt`; core `sync/Revision.kt` and `sync/MergeEngine.kt`; add explicit Room migration for outbox/tombstones only in the subsequent implementation plan.

**Proposed boundaries:** account gateway supplies identity and scoped authorization; transport reads/writes immutable per-device change batches and content-hashed blobs; coordinator handles retries, per-account workspaces and user-visible state; merge engine is deterministic and preserves concurrent conflicts. Credentials remain outside archives and normal preferences.

- [ ] After Tasks 1–2 succeed, specify exact SDK versions/scopes, schema/migration and callable interfaces in the implementation plan. No placeholder interface should reach coding.
- [ ] Define test fixtures for the five Review Focus conditions, explicit opt-in upload, first sync, account deletion versus local deletion, restart recovery and local profile editing.
- [ ] Verify a no-billing personal test project and two-device synchronization before enabling the feature in a released APK. Until then, no fake Google button or success state is shipped as working functionality.

## Current state

Blocked at Task 1 by official-document access, and project/client configuration has not been established. The visual mock is approved; cloud functionality is not complete. The offline redesign and catalog plans are independently executable once their implementation plan is reviewed.
