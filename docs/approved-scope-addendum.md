# Approved design and expanded product scope

Date: 2026-10-19

The user approved the design and proposal and requested proceeding with Superpowers. The current six-screen design and `prototype-interactions.md` supersede earlier navigation and retirement behavior. Approval includes bordered summaries, compact headings, 3D asset artwork, Collection/Analytics/Settings tabs, and resale as an item-level action.

The user additionally requires multiple currencies, multiple languages, a large selection of 3D/2D/other asset imagery, and future login/logout/profile/birthday support. The former single-currency restriction is superseded. Future account features are extension requirements, not an instruction to deploy authentication now.

## Implementation defaults for these requirements

- Monetary amounts retain ISO currency and integer minor units. Support currencies with 0, 2, and 3 fractional digits. Retain original purchase and sale currencies independently.
- A reporting currency is a view preference. Changing it never changes original amounts. Use explicit dated manual exchange rates for the offline first release. Missing conversions produce separate currency subtotals and an incomplete-total label, never an assumed 1:1 rate.
- For cross-currency sale calculations, require a dated conversion of proceeds into purchase currency and preserve that transaction's rate snapshot. Reporting rates must not silently rewrite realized historical results.
- Initial UI resources: English, Simplified Chinese, Traditional Chinese, and Malay; follow-system and manual language selection. Add languages through resources without changing domain logic. Treat translations as reviewable initial translations, not professionally certified localization.
- Image choices: searchable 3D catalog, flat/2D catalog, imported photos, generated cutouts, and custom transparent images. Use real licensed images and explicit attribution, not Unicode emoji pretending to be 3D artwork.
- Catalog acceptance target: at least 500 distinct 3D choices and 500 flat choices, including electronics, home, transport, fashion, hobbies, and food. Verify actual catalog coverage and license before promising availability. Microsoft Fluent Emoji is a candidate source, not yet downloaded or verified in this environment. Bundle a useful starter set; make additional downloadable packs optional and disclose offline availability.
- Assets belong to a stable local workspace, not a hard-coded account ID. Future accounts can link to this workspace through an explicit migration. Do not add fake login/logout buttons, collect birthdays, or upload local data in the current release.
- Retirement freezes gross cost at the retirement date. Sale settlement separately shows net cost over calendar days owned through sale. Reactivation resumes the holding-date denominator from original purchase and is labeled clearly; it does not invent active-use accounting.

These defaults make the new requirements concrete for the implementation plan. They do not imply that future authentication, an exchange-rate feed, the icon catalogs, or an Android application already exist.
