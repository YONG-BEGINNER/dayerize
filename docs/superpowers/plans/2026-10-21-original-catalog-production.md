# Original Artwork Catalog Production Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce and verify 600 distinct item identities, each with approved realistic 3D and cute 2D artwork, without inflating counts with recolors or shipping unchecked sheets.

**Architecture:** The versioned runtime manifest defined in `2026-10-21-alloy-implementation.md` consumes individually reviewed exports. Source references, generation prompts, model verification and production status live in a separate manifest so planned designs never appear as available assets.

**Tech Stack:** Image generation/editing tools, PNG/WebP alpha-capable exports, JSON manifests, existing Android/Coil renderer and native screenshot tests.

**Spec:** `docs/superpowers/specs/2026-10-21-catalog-research-and-2d-directions.md`; `docs/superpowers/specs/2026-10-21-expanded-item-catalog.md` (model rules, with count superseded).

## Global Constraints

- Keep approved realistic 3D and approved cute sticker 2D; do not substitute official item packs.
- 600 identities, up to 1,200 exports. Recolors do not count as new identities.
- Model-specific entries require source photos, generation/year and distinguishing geometry.
- Source masters remain outside the APK. Existing legacy artwork and required licenses stay available for old saved IDs.
- Use image-generation tooling for original imagery and edits. Do not replace this with programmatically drawn lookalikes or blindly slice approved sheets into production assets.
- Check transparent edges, no cropping/stretching, and optical scale at 48/64/96dp in light/dark themes.

## Review Focus

1. Generated image counts/objects differ from the request: Task 2 checks actual files and subjects.
2. Similar cameras/cars are mislabeled as exact models: Task 1 requires reference confidence and Task 2 rejects unverified names.
3. Oversized artwork silently increases the APK beyond reliable download limits: Task 3 measures packaged bytes.
4. Tall/thin objects look tiny or have clipped borders: Task 2 owns per-item optical review.
5. New catalog releases break old backup IDs: Task 3 verifies stable identifiers and restoration.

## Task 1 — Concrete production inventory and provenance

**Files:** create `design/artwork/production.json`, `docs/asset-provenance-original.md`; runtime destination `app/src/main/assets/artwork/catalog.json`.

**Interfaces:** production entries have `id`, `category`, `names`, `modelFamily`, `referenceConfidence` (`generic`, `family`, `verified-model`), `sourceRefs`, `threeDStatus`, `cute2DStatus`, `sourceMasterPaths`, `exportPaths`, `reviewNotes`. Status values: `planned`, `generated`, `reviewed`, `exported`, `shipped`. Runtime includes only reviewed, exported files that exist.

- [ ] Enumerate exact identities against the 20 category budgets (60/50/40/40/30/50/45/35/25/30/35/25/35/25/25/15/15/10/5/5 = 600).
- [ ] Assign stable semantic IDs and model-family provenance; reference photos are required before labeling a specific Canon/Sony/Nikon/car generation as verified.
- [ ] Validate 600 unique IDs and both-style status fields; duplicate recolors cannot satisfy coverage. Commit the inventory without claiming artwork completion.

## Task 2 — Produce and review themed batches

**Files:** source masters outside APK under `design/artwork/masters/`; optimized exports under `app/src/main/assets/artwork/`; update production manifest and provenance.

**Interfaces:** consume inventory from Task 1; produce individual alpha-capable files for `ArtworkEntry.files`, with conservative per-item `opticalScale` and measured dimensions. New-style filenames use manifest paths, not assumptions about file extensions.

- [ ] Start with the 12 shared subjects needed by the initial UI milestone: camera, laptop, sedan, headphones, watch, backpack, shoe, phone, coffee machine, drill, guitar, bicycle; produce both styles and an original fallback.
- [ ] For each subsequent category, generate the specified identities in manageable review batches, using approved style references. Generate/edit one production object per exported file. Preserve model-family silhouette differences and avoid hallucinated logos/text.
- [ ] Inspect each output at native size, 48/64/96dp and dark/light backgrounds. Reject clipping, stray pixels, opaque backgrounds, distorted object geometry or inconsistent sticker borders. Log actual acceptance; do not accept prompt compliance as evidence.
- [ ] Finish all 600 identities in both styles. Confirm 1,200 actual reviewed exports before marking the complete catalog done. Commit each reviewed category batch with its exact completed count.

## Task 3 — Package, search and compatibility verification

**Files:** create `tools/validate-artwork.py` (read-only inspection, not image editing); extend `app/src/test/java/app/dayerize/ArtworkCatalogTest.kt`; update `artwork/ArtworkCatalog.kt` and runtime JSON.

**Interfaces:** validator accepts manifest path and asset root; returns nonzero for duplicate IDs, missing files, invalid dimensions, opaque-background artwork, invalid optical bounds or unreadable file formats. The runtime catalog search API is defined by the UI plan.

- [ ] Write failing validator fixtures for duplicate ID, missing file and fully opaque export; implement inspection and prove the fixtures are rejected.
- [ ] Verify all exported entries appear in the correct category/style, model aliases match supported metadata and unknown legacy backups retain a fallback rather than lose assets.
- [ ] Measure source/export/APK sizes. Optimize via appropriate export settings while preserving alpha and inspected readability. Keep legacy compatibility files; do not delete them just to improve size.
- [ ] Run manifest validator, artwork tests and final APK build. If full bundled distribution cannot stay within the chosen delivery host's limits, design a versioned optional-pack distribution before shipping; do not silently add a network dependency.
- [ ] Deliver a final catalog report of 600 identities/1,200 exports only when those counts are verified; report partial batches honestly beforehand.

## 0.3.1 everyday batch

The user-requested follow-up adds 24 everyday identities in both styles, bringing the verified runtime catalog to 36 identities / 72 exports. All were checked at 48/64/96px on light/dark backgrounds; generic provenance, individual prompts and master/export hashes are recorded in `design/artwork/`. Runtime derivatives are 512×512 with unchanged source masters and no crop/stretch. This is a partial catalog milestone, not completion of the 600-identity target. See `docs/redesign/alloy-0.3.1-delivery.md` for tests and delivery evidence.
