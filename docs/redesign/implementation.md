# Screenshot-led redesign — approved 2026-10-19

Visual target: user-supplied screenshots and approved six-screen board at /workspace/generated_images/exec-de89cc7e-26bb-47af-8fd9-9600bba2cba9.png.

Implementation sequence:
1. Shared near-black theme (#111111), charcoal panels (#202020), subtle borders, 24dp corners, white actions and lime selection accents. Four-item floating dock plus separate add control.
2. Collection: compact title, bordered two-column value/cost overview, status counts, horizontally scrolling categories/status filters, two-column integrated artwork/metadata/cost cards.
3. Wishlist: separate persisted flag, excluded from owned totals and cashflow. Entry toggles Asset/Wish; mark purchased opens the asset editor for date/price confirmation.
4. Entry: image hero with edit chooser; grouped rows and bottom sheets for dates/categories/tags; target cost modes; notes/advanced fields; saveable draft. Icon picker dense five-column grid with style tabs/search. Retain licensed Fluent artwork.
5. Detail/analytics/settings: carry same surfaces and density; preserve currencies, languages, local backup and editable data. No premium advertising, pretend login, or independent Sold tab.
6. Regression tests, native rendered screenshots, review, versioned directly downloadable APK.

Ruling: use native Compose rendering rather than the Product Design web-template workflow because the user is updating an installed Android app. The approved board is a visual reference, not a claim of runtime behavior.
Ruling: new Wishlist data is additive JSON with a default false, preserving existing Room records. Wish prices are budgets and never enter owned value or purchase cashflow.
Ruling: preserve existing notes up to 10000 characters rather than truncate older records to the reference screenshot's 200-character label.

Verification in progress. Existing review findings involving retirement and sale cost, unsafe restore preferences, photo API compatibility and draft loss are addressed where this redesign touches those flows.

Completed milestone: 0.2.0 redesign implemented; verification and screenshots recorded in verification.md and design-qa.md. All 30 tests passed; lint 0 errors/16 warnings. Independent review's three P2 findings were corrected and their edit regressions passed. Existing sale-rate overflow and empty-restore preference defects were also fixed with failing-then-passing tests.

Ruling: deliver an ARM64 APK while retaining other ABI builds locally, to keep the direct download small enough for the available GitHub delivery route. Cost if wrong: devices without ARM64 need the separately built ARMv7/universal package.
