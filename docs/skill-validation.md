# Project skill validation

Date: 2026-10-19

## Baseline before writing the skill

A separate agent received the project intent, store-header-only evidence, and blocked research context, then produced an implementer handoff without this skill.

It correctly said: “No functional parity claims are currently supportable.” However, it assigned P0 to “Item management, provisional” and “Browse and find items, provisional” with implementation acceptance criteria despite no confirmed functional scope. It also said icons could remain deferred “if explicitly excluded from the release scope.” That qualification is reasonable, but the project needs separate functional and icon milestones so later reports cannot accidentally equate partial delivery with full parity.

The skill addresses the baseline's ambiguous handoff with explicit evidence statuses, a feature-matrix schema, and a rule keeping proposals outside the committed implementation backlog until approved. Project-specific facts live in the linked brief and research ledger.

## Checks

- Metadata check: valid lowercase/hyphen name, description begins “Use when”, frontmatter under 1024 characters.
- File checks: both referenced project documents exist.
- Decision preservation: brief records the user's Android-first decision.
- Application test: a fresh agent reads the skill and linked references, then prepares a handoff under the same limited-evidence conditions, with the Android decision added.

Application-test result: passed the listed criteria. The agent preserved Android first, selected no stack, kept reference features as unknown research topics, sequenced evidence and scope before implementation, separated functional and icon gates, and found both linked documents accessible without contradictions. Its handoff explicitly stated: “No reference workflows are verified.”

This validates guidance retrieval and application in a narrow scenario. It does not validate an application, establish reference-app parity, or guarantee future agent compliance.
