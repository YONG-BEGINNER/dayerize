# Dayerize 0.4.4 — smoother navigation and collection tools

## Changes
- 180ms primary-tab fade, search reveal and card placement animations. Reduce Motion remains enabled by default; turn it off in Settings to see animations.
- Removable active-filter chips, including hidden search queries, and a clear-search-and-filters action that preserves sort and layout.
- Collection sort, direction and layout saved in existing settings. Search and filters retained separately for Collection and Wishlist during navigation.
- Filtered empty results offer reset; a genuinely empty collection offers Add.
- Monthly cash-flow navigation from the earliest recorded transaction through the current month, with a current-month reset. Purchases and sales remain separated by original currency. Current ownership valuation is unchanged. Empty months and arithmetic overflow have explicit messages.
- New labels localized for English, Malay, Simplified Chinese and Traditional Chinese.

## Delivery
Version 0.4.4 / code 10. Local debug build; no publication or push. Existing local debug key differs from the previously published app, so it cannot directly upgrade that installation. Physical-device testing remains outstanding.

## Implementation
Parallel collection, analytics and motion work, followed by independent static review and integrated verification. Review identified arithmetic overflow in historical cash flow; the UI now reports unavailable totals rather than crashing or displaying partial totals.

## Verification
97 host tests passed (43 core, 1 imaging, 53 app), no failures/errors/skips. Lint: 0 errors, 30 warnings (two newly unused legacy strings after replacing analytics copy). Debug APK build and signature/version verification passed. Fresh analytics render inspected; screenshots preserved outside tracked references.

APK: `/workspace/downloads/Dayerize-0.4.4-arm64-debug.apk`
SHA-256: `4409b16b485d23a0bcc2485f3fd38f3aae5db7859894b42240c3a92dd9d49755`
