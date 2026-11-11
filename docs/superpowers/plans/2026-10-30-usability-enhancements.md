# Usability enhancements implementation plan

User requested all five proposed enhancements and authorized parallel execution. Recommended defaults apply after unanswered preference questions.

## Design
Preserve rounded 0.4.3 styling. Add subtle motion gated by existing Reduce Motion default (enabled), visible removable filters and clear search/filter action, persistent sort/order/layout, navigation-session filter retention, useful no-results recovery, and selectable monthly cash flow. Keep current valuation and ownership analytics independent of the selected cash-flow month. No database schema migration or new dependencies. All new UI labels support English, Malay, Simplified and Traditional Chinese.

## Parallel implementation
- Collection owner: CollectionScreen and focused tests/resources. Persist sort/order/layout through existing settings; make filters/search saveable; distinguish genuine empty collections from zero matching results. Clear filters preserves sort/layout.
- Motion owner: AppRoot and shared motion helper/tests. SaveableStateHolder retains separate collection/wishlist state. Short primary-tab fade, search reveal and item placement respect Reduce Motion. Avoid editor/detail duplicated lifecycle effects.
- Analytics owner: AnalyticsScreen and focused tests/resources. Current month default, previous/next month navigation bounded by earliest recorded transaction and current month, current-month reset, per-currency cash-flow arithmetic.
- Root: integrate, review, run full host tests/lint/build, version 0.4.4/code 10 and deliver local APK.

## Validation
Focused tests cover removable and reset filters, true-empty/no-results distinction, preference persistence, navigation restoration, motion disabled/enabled, month boundaries and independent currency totals. Full host suite and lint run via screenshot-preserving helper. Inspect fresh native renders and APK metadata/signature. Existing signing limitation remains; no public release or push.
