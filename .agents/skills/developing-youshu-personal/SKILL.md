---
name: developing-youshu-personal
description: Use when researching, planning, implementing, or reviewing the personal internationally usable application inspired by 有数 from Build Dream, Inc., including feature parity and item icons.
---

# Developing the personal 有数-inspired app

## Project contract

Read `docs/project-brief.md` and `docs/reference-research.md` from the repository root first. They contain user requirements, evidence, and unresolved decisions. Update them as evidence or user decisions arrive. This skill is project guidance, not an approved application specification.

Deliver verified functionality first, a distinctive original item-icon system second, and enhancements third. “Global” means usable by the owner internationally; it does not imply a public service or multi-user product.

## Reference checklist

| Artifact | Required contents |
| --- | --- |
| Evidence ledger | Source URL or supplied artifact, publisher, app identity/package ID when known, version/platform, observation date, precise claim, confidence |
| Feature matrix | ID, user goal, evidence, observed inputs/actions/results, error states, status, acceptance test, implementation status |
| Decision log | Device/platform, languages, offline needs, storage, backup, sync; each marked proposed or user-approved |
| Delivery report | Changed behavior, commands and results, remaining gaps, next step |

Feature status is one of **observed**, **publisher-described**, **user-requested**, **proposed**, or **unknown**. Publisher descriptions do not prove runtime behavior. Proposed features stay outside the committed implementation backlog until approved. Unknowns are research tasks, not guessed requirements.

Example: a store header showing a mascot supports app identity and branding only. “Items have expiry reminders” remains unknown until supported by a source or requested by the user.

## Development sequence

1. Inspect repository instructions and current work. Cloud tasks are already isolated; use the existing checkout and do not create a worktree unless requested.
2. Research the exact app using first-party store listings, developer material, and supplied walkthroughs. Record blocked access honestly. Ask for the direct listing and in-app screenshots when identity or behavior cannot be established. Never claim to have operated the app from reading its listing.
3. Use **superpowers:brainstorming** for architecture and unresolved product choices, then **superpowers:writing-plans** once its prerequisites are satisfied. Reuse prior decisions; ask only unresolved questions that affect the next step.
4. Implement one approved end-to-end workflow at a time using **superpowers:test-driven-development**. Use **superpowers:systematic-debugging** for failures and **superpowers:verification-before-completion** before completion claims.
5. Verify persistence across restart, invalid inputs, and failure recovery. Before storing irreplaceable personal data, demonstrate backup/restore and migration safety. Check Unicode, selected locales, and timezone behavior where relevant. Validate required services from the intended region; local startup alone does not prove global availability.
6. After functional acceptance, study supplied item-icon examples. Establish original/licensed assets, a consistent style, stable icon identifiers, text alternatives, missing-icon fallbacks, and small-screen legibility. Replacing artwork must preserve item data.
7. Report parity against the agreed feature matrix, including untested and deferred features. A working subset is a milestone, not full parity. Keep later enhancements separately prioritized.

## Common mistakes

- Treating a generic inventory backlog as observed 有数 functionality.
- Choosing a stack before device, storage, and offline requirements are known.
- Confusing the launcher mascot with the desired in-app item artwork.
- Adding public accounts, billing, or cloud sync merely because the app should work internationally.
