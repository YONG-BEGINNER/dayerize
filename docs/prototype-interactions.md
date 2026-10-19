# Android prototype: revised interaction specification

Status: design review only. Application proposal remains unapproved. No executable prototype or Figma file is delivered by this document.

## Source and priority

The user's latest supplied Chinese UI/interaction whitepapers are the source for these requirements. Their account of 有数 is not independently verified. Explicit latest requests supersede earlier design proposals: bordered summary, more useful information, compact heading, futuristic minimal styling, 3D asset icons, and no separate Sold destination.

Use three primary tabs: Collection, Analytics, Settings. Resale remains an item-level workflow. Prior generated Collection/Overview/Sold navigation is obsolete. Retirement freezes the running daily-cost calculation, superseding the earlier proposal's continuing-cost rule. Reopening retirement and sale-after-retirement calculation need explicit treatment before implementation.

## Visual system for review

Use a cool off-white surface, near-black text, lime active/primary accents, and muted gray outlines. Heading approximately 24sp; no marketing slogan. A clearly bordered summary panel contains estimated managed-asset value and secondary counts/cash-flow metrics. Show estimates as estimates, rather than implying verified net worth.

Assets use rounded, stylized 3D illustrations with a consistent camera angle and soft white outline; uploaded cutout photos are another supported image source. Both are rendered on quiet surfaces. Keep category/color differences intentional. Use readable text, 48dp minimum touch targets, labels alongside navigation icons, and signed/labeled financial amounts rather than color alone.

Mock currency is MYR and interface language English for design review only. Example date anchor: October 19, 2026, Asia/Kuala_Lumpur.

## Screen and interaction coverage

| Screen | Content | Actions and destinations | States to prototype |
| --- | --- | --- | --- |
| Collection | Compact title; bordered summary; search; category/tag filter; grid/list toggle; asset names, purchase prices, current cost/day | Plus opens Add; tap asset opens Detail; long-press opens Edit/Resell/Delete menu; tabs switch primary destinations | Populated, empty, no search results, filter applied, list mode, menu open |
| Filter sheet | Categories, tags, lifecycle status; visible active selections | Apply updates collection; Reset clears selections; dismiss preserves current applied filter | Applied/cleared; zero matches |
| Add/Edit | Image; name; price; purchase date; planned days/end date; depreciation selection; category; colored tags; expandable channel/warranty/serial/notes | Save validates and returns to saved item; Cancel prompts if dirty; image opens camera/library sheet | Empty, populated, invalid price/date/name, dirty cancellation, saving, saved, image failure |
| Photo selection | Original square image; AI cutout toggle; original/cutout comparison | Camera/library → selected image → processing → result; retain original; retry or use original on failure | Permission denied, picker cancelled, processing, success, failure, switch comparison |
| Detail | Large object art; status; purchase price/date; cost/day; elapsed/remaining days; planned end; progress; category/tags | Edit; Retire with date/reason confirmation; Resell opens settlement sheet; overflow Delete | In service, near planned end, beyond planned end, retired/frozen, sold, no image |
| Resale sheet | Sale price/date/channel; purchase versus proceeds; signed gain/loss; final ownership cost | Preview updates as values change; Confirm commits; Cancel changes nothing | Invalid/date mismatch, gain, loss, break-even, saved; no separate Sold tab |
| Analytics | Bordered estimated-value summary; monthly sale proceeds minus purchases; status distribution; category distribution; cost trend | Status/category selections filter drill-down to Collection | Populated, empty, data unavailable; labeled chart values |
| Settings | Appearance; categories/tags; backup/restore; currency/timezone; About | Theme light/dark/system; tag management; export; choose restore file then preview and confirm | Export success/failure; invalid archive; restore cancellation/success/failure; tag in use |

## Primary walkthroughs

1. Collection → Add → choose photo → simulated cutout → enter required values → Save → Detail → Collection.
2. Collection → search name/notes/serial → filter category/tag → toggle grid/list → Detail → Edit → Save.
3. Detail → Retire → confirm date/reason → frozen daily cost and retired status. Retirement does not delete the asset.
4. Detail → Resell → enter price/date/channel → inspect gain/loss → Confirm → updated detail and analytics. Access sold records using Collection's status filter.
5. Analytics → category/status → filtered Collection → item.
6. Settings → export backup; restore → choose file → validation summary → explicit replacement confirmation. Prototype simulates this; real persistence/restore needs separate implementation and tests.

## Motion specification

- Photo chooser: bottom sheet with light translucent scrim. Camera/library cancellation preserves the form.
- Cutout: restrained clockwise edge scan and optional short haptic; background fades to a checkerboard, then transparent preview with approximately 2dp soft white outline. Processing status must be textual too.
- Comparison: short original/cutout flip; reduced-motion uses an instant swap or fade. A 2D image with tilt is not a true 3D reconstruction.
- Detail art: subtle bounded tilt only with permission/capability; touch/scroll alternative; disabled with reduced motion.
- Cost figure: brief rolling transition to the exact value; screen readers receive final text once. No artificial decline message when value did not decrease.
- Planned end: optional gentle amber emphasis in final 30 days, backed by a text label; no flashing.
- Sale gain: optional brief confetti and positive label; break-even stated plainly. Loss: neutral language with exact signed amount. Do not disguise a loss or label it a profit.
- Reduced motion disables tilt, flip, number rolling, breathing effects, and confetti. All actions work without animations or haptics.

## Numerical and data issues before implementation

- Latest brief requires retirement to freeze cost. Proposed convention for review: show frozen gross daily cost at retirement; on a later sale separately label final net cost over calendar days owned through sale. Do not conflate these denominators. Reactivation remains a pending decision, not an implemented behavior.
- Gold or appreciating assets do not automatically have zero ownership cost. Display zero only when the defined calculation produces it; distinguish estimated value from actual proceeds.
- Monthly net flow uses dated purchases and actual sales, not record-creation timestamps. Label the measure precisely.
- Status charts need an explicit count/value basis. Proposed review visuals use item counts; category charts use estimated retained value. Sold assets are excluded from retained-asset value.
- Cost trend needs a defined historical series and treatment of edits/retirement/disposal. Until specified, charts are illustrative mock data, not validated calculations.
- No actual AI service, camera, gyro, haptic, backup, or durable database is demonstrated by static screens. A clickable prototype must label simulated processing accurately.

## Delivery status

Static visual review sheets saved in this workspace:

- Collection + Detail: `/workspace/generated_images/exec-40f58301-af18-40b3-84c5-369590872b68.png`
- Entry + Resale: `/workspace/generated_images/exec-967703c0-94e0-449b-87d3-9bf0483945d1.png`
- Analytics + Settings: `/workspace/generated_images/exec-7e41523f-550b-405f-9e0f-66dc9cfe26c5.png`

Remaining visual corrections: Analytics uses a headphones thumbnail for Accessories while the sample category sum corresponds to the watch; use the watch thumbnail. Settings export/restore uses cloud-shaped icons despite local-file semantics; use file export/import icons. Entry needs the depreciation-rule field and use-based target variant in the next form iteration. Dedicated filter, compact-list, retirement, validation, backup confirmation, cutout-processing, and empty-state frames are specified but not visually rendered yet. The generated review sheets are not pixel-measured or accessibility-verified implementations.

Figma tool discovery found no Figma tool. The plugin-install tool rejected the requested identifier, so no Figma file or share URL could be created. Current deliverable is a static visual review plus this interaction map. It does not complete the requested clickable prototype or approve application development.
