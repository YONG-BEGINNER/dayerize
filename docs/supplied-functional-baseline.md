# Supplied functional baseline

Source: Chinese whitepaper supplied by the user in this conversation on 2026-10-19. All reference-app claims below are user-supplied and independently unverified. This is a research/design input, not an approved implementation specification.

## Identity discrepancy

The initial Google Play-style screenshot names Build Dream, Inc. The whitepaper names Transmedia Creative Lab, iPhone/iOS 15.5+, and approximately 84.6 MB. The relationship between these listings is unknown. Do not merge their metadata or assume the same product/version. Neither establishes that the reference is available only in China. The user's target remains Android first.

## Product intent

Manage personal possessions across their lifecycle as potentially realizable assets. Show ownership cost over time or usage to help evaluate purchases and long-term value. The whitepaper describes “everything that can be sold is an asset” and emphasizes service delivered rather than sticker price.

## Candidate functionality

| ID | Supplied capability | Details and open questions |
| --- | --- | --- |
| F01 | Asset entry across categories | Electronics, gold/liquor, collectibles, luxury goods, game items; exact fields and quantity model unspecified |
| F02 | Photo background removal | Photograph an item and automatically produce a transparent cutout; processing location, model, offline capability, and manual correction unspecified |
| F03 | Tags and depreciation rules | Custom tags and category-specific depreciation; rule types and valuation formulas unspecified |
| F04 | Target daily/use cost | Purchase price and expected service days or uses; time and usage are distinct modes, not interchangeable denominators |
| F05 | Service progress | Elapsed days, remaining days, percentage, current daily cost, expected end date; day-count and retirement semantics unspecified |
| F06 | Resale and retrospective costs | Record sale proceeds and purchase-minus-sale cost, holding-period daily cost, profit/loss; fees, refunds, and partial sales unspecified |
| F07 | Asset dashboard | Current aggregate asset value, breakdown by status/category; current valuation method unspecified |
| F08 | Lifecycle status | In service, retired, sold; whitepaper also mentions scrapped. Transition rules, reactivation, and deletion unspecified |

No candidate is marked implemented or runtime-verified. Error states and acceptance tests require design decisions.

## Four supplied views

1. Gallery/item list: clean modern cards, transparent item photos, item name, purchase price, current daily cost.
2. Item detail/cost tracking: large image, service progress percentage, elapsed/remaining days, current daily cost, expected end date.
3. Asset dashboard: aggregate current value, category charts, in-service/retired/sold breakdowns.
4. Disposal/resale ledger: purchase price, realized sale price, gain/loss, final ownership-cost analysis. Use signed values and labels as well as red/green.

The whitepaper suggests the attractive “icons” may be cutout photographs of individual possessions. This is an interpretation, not confirmed visual evidence; compare actual in-app examples before choosing an illustration or photography system. Preserve the user's functionality-first priority, then focus on artwork and gallery polish. Automatic cutouts remain an explicit candidate feature, not silently discarded or relabeled an optional enhancement.

## Calculation decisions to settle before coding

- Proposed target cost: purchase cost / planned days (or planned uses). Denominator must be positive.
- Proposed current gross daily cost: purchase cost / elapsed service days. Define same-day display, date inclusivity, retirement pauses, timezone changes, and future purchase dates.
- Proposed realized net ownership cost: purchase cost minus sale proceeds; divide by agreed holding/service days for final daily cost. Define fee handling. Allow negative cost when an item sells above its purchase price.
- Realized gain/loss has the opposite sign to net ownership cost; use explicit labels to prevent sign confusion.
- Remaining days and progress require defined behavior after the target end date. Expected end date describes planned service, not product expiry.
- Do not equate purchase cost, depreciated book value, estimated resale value, and realized proceeds. Choose a valuation basis before computing the dashboard.
- “Personal net worth” normally includes liabilities and other assets. A possessions-only total should be labeled managed-asset value unless broader accounting is explicitly included.
- Never sum different currencies without a conversion policy. Choose a base-currency-only first version or define exchange-rate sources, dates, precision, and offline behavior.

These formulas and edge cases are design proposals, not formulas verified from the original app.

## Privacy and international use

The whitepaper claims no collection, no ads, and “purely local or secure sync.” Treat those as unverified publisher/privacy assertions. Local storage and remote synchronization are different architectures. Cloud photo processing also changes data exposure. Decide storage, image processing, backup, and sync explicitly; never promise no data leaves the device while using a remote service.

Recommendation for discussion: offline-first personal Android application with local storage and user-controlled backup. Compare local-only, local-plus-optional-sync, and cloud-account approaches before selecting architecture. No choice has yet been approved.
