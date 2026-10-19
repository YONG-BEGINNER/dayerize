# Personal 有数-inspired application

## User requirements

- Reference: 有数, shown as published by Build Dream, Inc.
- Intended user: the owner personally, with use outside China.
- First platform: Android (confirmed by the user).
- First milestone: reproduce the reference application's functions; enhance later.
- Item icons inside the reference app are a major attraction. Prioritize functionality before a dedicated icon/design phase.
- Use Superpowers and prepare a reusable skill to keep development consistent.

The repository name `dayerize` is existing workspace context, not an approved public product name.

## Current deliverable

The user has now approved the proposal and visual designs and requested implementation with multiple currencies, multiple languages, a large 3D/2D/custom icon library, and future account/profile support. See `approved-scope-addendum.md` for the new requirements and `superpowers/plans/2026-10-19-android-asset-manager.md` for the implementation plan. Earlier unapproved/single-currency statements below describe historical stages and are superseded by this approval.

Latest request: review a complete prototype before approving development. See `docs/prototype-interactions.md` for the new three-tab structure (Collection, Analytics, Settings), bordered summary, compact heading, richer item data, and retirement-freezes-cost correction. No separate Sold tab. The older Android design proposal is superseded on these points and remains unapproved. Figma delivery is currently unavailable in this session.

Research preparation and reusable project guidance. No application stack, product specification, feature list, or implementation plan has been approved. The repository was empty when this work began.

The user has now supplied a functional whitepaper, summarized in `docs/supplied-functional-baseline.md`. Use it as a candidate functional baseline for design discussion, not proof of the reference app's implemented behavior. Android first remains confirmed. Source identity discrepancies do not prevent designing from this supplied brief, but prevent a verified reference-parity claim.

## Decisions still needed

| Decision | Status |
| --- | --- |
| First device/platform | User-approved: Android first |
| Exact reference listing/package identifier | Unknown; screenshot supplies title and developer only |
| Reference workflows and item-icon examples | User-supplied functional description available; actual screens and item-art examples still needed |
| Interface languages | Unspecified |
| Offline use | Unspecified |
| Local storage versus hosted storage | Unspecified |
| Single device versus synchronization | Unspecified |
| Backup/restore expectations | Must be designed before use with irreplaceable personal data |

## Architecture options for later comparison

These are alternatives, not selected technologies:

- Mobile-first web application: convenient across devices; browser storage, installation, notifications, and background behavior must be evaluated against verified features.
- Cross-platform mobile application: shared mobile implementation and device integration; requires platform-specific builds and distribution.
- Single-platform native application: focused device experience; a second platform requires additional work.

Android is the first platform. Compare native Android and cross-platform mobile approaches against the verified workflows before selecting a stack; web support is not currently required. Avoid public SaaS features unless explicitly requested. International use does not itself require accounts or a server.

## Milestone acceptance

1. Evidence and scope: identify the exact app and agree a sourced feature matrix, including unknowns and intentional exclusions.
2. Functional foundation: approve architecture and implementation plan; ship tested vertical slices with durable storage and recoverable backups.
3. Functional parity: verify all agreed reference workflows and error states; list exclusions explicitly.
4. Item icons: assess actual in-app examples, then design a coherent original/licensed icon family; check readability and preservation of item data.
5. Enhancements: prioritize separately after the baseline works.

Use `.agents/skills/developing-youshu-personal/SKILL.md` for subsequent work. If skill discovery does not expose it automatically, read that file explicitly.
